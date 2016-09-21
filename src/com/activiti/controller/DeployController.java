package com.activiti.controller;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.activiti.service.ProcessDefinitionService;
import com.activiti.service.UploadService;
import com.activiti.util.Page;
import com.activiti.util.PageUtil;
import com.activiti.util.UserUtil;

@Controller
@RequestMapping("/")
public class DeployController extends AbstractController {
	
	
	private UploadService uploadService;
	
	@Autowired
    ProcessDefinitionService processDefinitionService;
	
	@RequestMapping(value="deploy",method=RequestMethod.POST)
	public String deploy(@RequestParam("file")CommonsMultipartFile file,HttpSession session) throws Exception{
		//String uploadPath = "/upload";
		//String realUploadPath = session.getServletContext().getRealPath(uploadPath);
		//String zipUrl = uploadService.uploadImage(file, uploadPath, realUploadPath);
		InputStream inputStream = null;
		try {
			inputStream = file.getInputStream();
			String extension = FilenameUtils.getExtension(file.getOriginalFilename());
			DeploymentBuilder builder = repositoryService.createDeployment();
			if("zip".equals(extension)||"bar".equals(extension)){
				ZipInputStream zip = new ZipInputStream(inputStream);
				builder.addZipInputStream(zip);
			}else{
				builder.addInputStream(file.getOriginalFilename(), inputStream);
			}
			Deployment deployment = builder.deploy();
			System.out.println("deployId:"+deployment.getId());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(null!=inputStream){
				inputStream.close();
			}
		}
		
		return "redirect:/processList";
	}
	
	@RequestMapping(value="processList")
	public ModelAndView processList(HttpServletRequest request) throws Exception{
		
		ModelAndView mv = new ModelAndView();
		
		Page<ProcessDefinition> page = new Page<ProcessDefinition>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        User user = UserUtil.getUserFromSession(request.getSession());
        //获取登陆用户被设置过有权限启动的流程定义
        //List<ProcessDefinition> processDefinitionList = processDefinitionQuery.startableByUser(user.getId()).latestVersion().listPage(pageParams[0], pageParams[1]);
        
        //processDefinitionQuery.suspended().active().latestVersion().listPage(pageParams[0], pageParams[1]);
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.latestVersion().listPage(pageParams[0], pageParams[1]);
        
        page.setResult(processDefinitionList);
        page.setTotalCount(processDefinitionQuery.count());
        mv.addObject("page", page);

        // 读取所有人员
        List<User> users = identityService.createUserQuery().list();
        mv.addObject("users", users);

        // 读取所有组
        List<Group> groups = identityService.createGroupQuery().list();
        mv.addObject("groups", groups);

        // 读取每个流程定义的候选属性
        Map<String, Map<String, List<? extends Object>>> linksMap = setCandidateUserAndGroups(processDefinitionList);
        mv.addObject("linksMap", linksMap);
		
		mv.setViewName("processList");
		return mv;
	}
	
	/**
     * 流程定义状态控制
     *
     * @param state               active|suspend
     * @param processDefinitionId 流程定义ID
     * @return
     */
    @RequestMapping(value = "processDefinitManage/{state}", method = RequestMethod.POST)
    public String changeState(@PathVariable(value = "state") String state,
                              @RequestParam(value = "processDefinitionId") String processDefinitionId,
                              @RequestParam(value = "cascade", required = false) boolean cascadeProcessInstances,
                              @RequestParam(value = "effectiveDate", required = false) String strEffectiveDate) {

        Date effectiveDate = null;

        if (StringUtils.isNotBlank(strEffectiveDate)) {
            try {
                effectiveDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(strEffectiveDate);
            } catch (ParseException e) {
                //e.printStackTrace();
            }
        }

        if (StringUtils.equals("active", state)) {
            repositoryService.activateProcessDefinitionById(processDefinitionId, cascadeProcessInstances, effectiveDate);
        } else if (StringUtils.equals("suspend", state)) {
            repositoryService.suspendProcessDefinitionById(processDefinitionId, cascadeProcessInstances, effectiveDate);
        }
        return "redirect:/processList";
    }
	
	
	@RequestMapping(value="readResource")
	public void readResource(@RequestParam("pdid")String pdid,@RequestParam("resourceName")String resourceName,HttpServletResponse response) throws Exception{
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		ProcessDefinition definition = query.processDefinitionId(pdid).singleResult();
		//通过接口读取资源
		InputStream resourceStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), resourceName);
		//输出资源
		byte[] b = new byte[1024];
		int len = -1;
		while((len=resourceStream.read(b,0,1024))!=-1){
			response.getOutputStream().write(b,0,len);
		}
	}
	
	@RequestMapping(value="deleteDeploy")
	public String deleteDeploy(@RequestParam("deploymentId")String deploymentId) throws Exception{
		//删除最新版本的流程定义部署,如果有旧版本的流程定义部署,就回复到旧版本,如果没有旧版本就删除整个流程定义
		repositoryService.deleteDeployment(deploymentId,true);
		return "redirect:processList";
	}
	
	public UploadService getUploadService() {
		return uploadService;
	}

	@Autowired
	public void setUploadService(UploadService uploadService) {
		this.uploadService = uploadService;
	}
	
	/**
     * 读取流程定义的相关候选启动人、组，根据link信息转换并封装为User、Group对象
     * @param processDefinitionList
     * @return
     */
    private Map<String, Map<String, List<? extends Object>>> setCandidateUserAndGroups(List<ProcessDefinition> processDefinitionList) {
        Map<String, Map<String, List<? extends Object>>> linksMap = new HashMap<String, Map<String, List<? extends Object>>>();
        for (ProcessDefinition processDefinition : processDefinitionList) {
        	//根据流程定义id获取对应流程定义中有权限操作该流程定义的用户或者组
            List<IdentityLink> identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());

            Map<String, List<? extends Object>> single = new Hashtable<String, List<? extends Object>>();
            List<User> linkUsers = new ArrayList<User>();
            List<Group> linkGroups = new ArrayList<Group>();

            for (IdentityLink link : identityLinks) {
                if (StringUtils.isNotBlank(link.getUserId())) {
                    linkUsers.add(identityService.createUserQuery().userId(link.getUserId()).singleResult());
                } else if (StringUtils.isNotBlank(link.getGroupId())) {
                    linkGroups.add(identityService.createGroupQuery().groupId(link.getGroupId()).singleResult());
                }
            }

            single.put("user", linkUsers);
            single.put("group", linkGroups);

            linksMap.put(processDefinition.getId(), single);

        }
        return linksMap;
    }
    
    /**
     * 设置流程定义对象的候选人、候选组
     * @return
     */
    @RequestMapping(value = "processStartableSet/{processDefinitionId}", method = RequestMethod.POST)
    @ResponseBody
    public String addStartables(@PathVariable("processDefinitionId") String processDefinitionId,
            @RequestParam(value = "users[]", required = false) String[] users, @RequestParam(value = "groups[]", required = false) String[] groups) {
        processDefinitionService.setStartables(processDefinitionId, users, groups);
        return "true";
    }

    /**
     * 读取已设置的候选启动人、组
     * @param processDefinitionId
     * @return
     */
    @RequestMapping(value = "processStartableRead/{processDefinitionId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<String>> readStartableData(@PathVariable("processDefinitionId") String processDefinitionId) {
        Map<String, List<String>> datas = new HashMap<String, List<String>>();
        ArrayList<String> users = new ArrayList<String>();
        ArrayList<String> groups = new ArrayList<String>();

        List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinitionId);
        for (IdentityLink link : links) {
            if (StringUtils.isNotBlank(link.getUserId())) {
                users.add(link.getUserId());
            }
            if (StringUtils.isNotBlank(link.getGroupId())) {
                groups.add(link.getGroupId());
            }
        }
        datas.put("users", users);
        datas.put("groups", groups);
        return datas;
    }

}
