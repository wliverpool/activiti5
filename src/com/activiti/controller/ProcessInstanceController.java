package com.activiti.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.activiti.util.UserUtil;

@Controller
@RequestMapping("/")
public class ProcessInstanceController extends AbstractController{
	
	private static Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);
	
	@RequestMapping(value = "toStartDefinition/{pdid}")
	public ModelAndView toStartDefinition(@PathVariable("pdid") String processDefinitionId,HttpSession session,RedirectAttributes redirectAttributes){
		ModelAndView mv = new ModelAndView();
		//判断流程是否使用外置表单
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
		
		User user = UserUtil.getUserFromSession(session);
        List<Group> groupList = (List<Group>) session.getAttribute("groups");

        // 权限拦截
        boolean startable = false;
        //获取已设置的流程定义的可启动用户或者组
        List<IdentityLink> identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
        if (identityLinks == null || identityLinks.isEmpty()) {
            startable = true;
        } else {
            for (IdentityLink identityLink : identityLinks) {
                if (StringUtils.isNotBlank(identityLink.getUserId()) && identityLink.getUserId().equals(user.getId())) {
                    startable = true;
                    break;
                }

                if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                    for (Group group : groupList) {
                        if (group.getId().equals(identityLink.getGroupId())) {
                            startable = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!startable) {
            redirectAttributes.addFlashAttribute("error", "您无权启动【" + processDefinition.getName() + "】流程！");
            return new ModelAndView("redirect:/processList");
        }

		boolean hasStartFormKey = processDefinition.hasStartFormKey();
		if(hasStartFormKey){//使用了外置表单
			Object renderedStartForm = formService.getRenderedStartForm(processDefinitionId);
			mv.addObject("startFormData", renderedStartForm);
			mv.addObject("processDefinition", processDefinition);
		}else{//动态表单
			StartFormData startFormData = formService.getStartFormData(processDefinitionId);
			mv.addObject("startFormData",startFormData);
			mv.addObject("pdid",processDefinitionId);
		}
		mv.addObject("hasStartFormKey", hasStartFormKey);
		mv.setViewName("toStartProcessDyForm");
		return mv;
	}
	
	/**
	 * 启动流程实例
	 * @param processDefinitionId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "startProcessDyForm/{pdid}")
	public String startProcessDyForm(@PathVariable("pdid") String processDefinitionId,HttpServletRequest request){
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
		boolean hasStartFormKey = processDefinition.hasStartFormKey();
		Map<String, String> formValues = new HashMap<String, String>();
		if(hasStartFormKey){
			//从提交的request请求中获取对应的外置表单的值
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
			for(Entry<String, String[]> entry : entrySet){
				String key = entry.getKey();
				formValues.put(key, entry.getValue()[0]);
			}
		}else {
			StartFormData formData = formService.getStartFormData(processDefinitionId);
			//从请求中获取流程动态表单的数据
			List<FormProperty> formProperties = formData.getFormProperties();
			for(FormProperty property : formProperties){
				String value = request.getParameter(property.getId());
				formValues.put(property.getId(), value);
			}
		}
		
		User user = UserUtil.getUserFromSession(request.getSession());
		identityService.setAuthenticatedUserId(user.getId());
		ProcessInstance instance = formService.submitStartFormData(processDefinitionId, formValues);
		return "redirect:/processList";
	}
	
	/**
	 * 待办列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "myTasks")
	public ModelAndView myTasks(HttpServletRequest request){
		ModelAndView mv = new ModelAndView("myTask");
		User user = UserUtil.getUserFromSession(request.getSession());
		//获取分配给当前人的任务列表
		//List<Task> myTasks = taskService.createTaskQuery().taskAssignee(user.getId()).list();
		//List<Task> waitingClaimTaks = taskService.createTaskQuery().taskCandidateUser(user.getId()).list();
		//List<Task> allTask = new ArrayList<Task>();
		//allTask.addAll(myTasks);
		//allTask.addAll(waitingClaimTaks);
		/*
		 * select distinct RES.* from ACT_RU_TASK RES left join ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_ 
		 * WHERE (RES.ASSIGNEE_ = ? or (RES.ASSIGNEE_ is null and (I.USER_ID_ = ? or I.GROUP_ID_ IN (select g.GROUP_ID_ from ACT_ID_MEMBERSHIP g where g.USER_ID_ = ? ) ) ) ) 
		 * order by RES.ID_ asc LIMIT ? OFFSET ?
		 */
		List<Task> allTask = taskService.createTaskQuery().taskCandidateOrAssigned(user.getId()).list();
		mv.addObject("tasks",allTask);
		return mv;
	}
	
	/**
	 * 签收任务
	 * @param taskid
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "claimTask/{taskid}")
	public String claim(@PathVariable("taskid") String taskid,@RequestParam(value = "nextDo",required=false) String nextDo,HttpServletRequest request,RedirectAttributes redirectAttributes){
		String userId = UserUtil.getUserFromSession(request.getSession()).getId();
		taskService.claim(taskid, userId);
		if (StringUtils.equals(nextDo, "handle")) {
            return "redirect:/doTask/" + taskid;
        } else {
        	redirectAttributes.addFlashAttribute("message","任务已签收");
    		return "redirect:/myTasks";
        }
	}
	
	/**
	 * 显示任务详细
	 * @param taskid
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value = "doTask/{taskid}")
	public ModelAndView doTask(@PathVariable("taskid") String taskid,HttpServletRequest request,RedirectAttributes redirectAttributes){
		ModelAndView mv = new ModelAndView("taskForm");
		TaskFormData taskFormData = formService.getTaskFormData(taskid);
		Task task = null;
		if(null != taskFormData&&null!=taskFormData.getFormKey()){//外置表单
			Object renderedTaskForm = formService.getRenderedTaskForm(taskid);
			task = taskService.createTaskQuery().taskId(taskid).singleResult();
			mv.addObject("taskFormData",renderedTaskForm);
			mv.addObject("hasFormKey",true);
		}else if(null != taskFormData){//动态表单
			mv.addObject("taskFormData",taskFormData);
			task = taskFormData.getTask();
		}else{
			task = taskService.createTaskQuery().taskId(taskid).singleResult();
            mv.addObject("manualTask", true);
		}
		mv.addObject("task", task);
		//获取流程实例数据
		String processInstanceId = task.getProcessInstanceId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		mv.addObject("processInstance",processInstance);
		
		// 读取任务参与人列表
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskid);
        mv.addObject("identityLinksForTask", identityLinksForTask);

        // 读取所有人员
        List<User> users = identityService.createUserQuery().list();
        mv.addObject("users", users);

        // 读取所有组
        List<Group> groups = identityService.createGroupQuery().list();
        mv.addObject("groups", groups);

        // 读取子任务
        List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery().taskParentTaskId(taskid).list();
        mv.addObject("subTasks", subTasks);

        // 读取上级任务
        if (task != null && task.getParentTaskId() != null) {
            HistoricTaskInstance parentTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getParentTaskId()).singleResult();
            mv.addObject("parentTask", parentTask);
        }

        // 读取附件
        List<Attachment> attachments = null;
        if (task != null && task.getTaskDefinitionKey() != null) {
            attachments = taskService.getTaskAttachments(taskid);
        } else {
            attachments = taskService.getProcessInstanceAttachments(task.getProcessInstanceId());
        }
        mv.addObject("attachments", attachments);
		
		return mv;
	}
	
	/**
     * 添加参与人
     */
    @RequestMapping("taskParticipantAdd/{taskId}")
    @ResponseBody
    public String addParticipants(@PathVariable("taskId") String taskId, @RequestParam("userId[]") String[] userIds, @RequestParam("type[]") String[] types,
                                  HttpServletRequest request) {
        // 设置当前操作人，对于调用活动可以获取到当前操作人
        String currentUserId = UserUtil.getUserFromSession(request.getSession()).getId();
        identityService.setAuthenticatedUserId(currentUserId);

        for (int i = 0; i < userIds.length; i++) {
            taskService.addUserIdentityLink(taskId, userIds[i], types[i]);
        }
        return "success";
    }
    
    
    /**
     * 删除参与人
     */
    @RequestMapping("taskParticipantDelete/{taskId}")
    @ResponseBody
    public String deleteParticipant(@PathVariable("taskId") String taskId, @RequestParam(value = "userId", required = false) String userId,@RequestParam(value = "groupId", required = false) String groupId, @RequestParam("type") String type) {
    	//区分用户、组，使用不同的处理方式
        if (StringUtils.isNotBlank(groupId)) {
            taskService.deleteCandidateGroup(taskId, groupId);
        } else {
            taskService.deleteUserIdentityLink(taskId, userId, type);
        }
        return "success";
    }
    
    /**
     * 添加候选人
     */
    @RequestMapping("taskCandidateAdd/{taskId}")
    @ResponseBody
    public String addCandidates(@PathVariable("taskId") String taskId, @RequestParam("userOrGroupIds[]") String[] userOrGroupIds,@RequestParam("type[]") String[] types, HttpServletRequest request) {
        // 设置当前操作人，对于调用活动可以获取到当前操作人
        String currentUserId = UserUtil.getUserFromSession(request.getSession()).getId();
        identityService.setAuthenticatedUserId(currentUserId);

        for (int i = 0; i < userOrGroupIds.length; i++) {
            String type = types[i];
            if (StringUtils.equals("user", type)) {
                taskService.addCandidateUser(taskId, userOrGroupIds[i]);
            } else if (StringUtils.equals("group", type)) {
                taskService.addCandidateGroup(taskId, userOrGroupIds[i]);
            }
        }
        return "success";
    }
	
    /**
     * 完成任务
     * @param taskid
     * @param request
     * @return
     */
	@RequestMapping(value = "completeTask/{taskid}")
	public String completeTask(@PathVariable("taskid") String taskid,HttpServletRequest request){
		TaskFormData taskFormData = formService.getTaskFormData(taskid);
		String formKey = taskFormData.getFormKey();
		Map<String, String> formValues = new HashMap<String, String>();
		
		if(StringUtils.isNotBlank(formKey)){//外置表单从request请求中获取填写的表单内容
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
			for(Entry<String, String[]> entry : entrySet){
				String key = entry.getKey();
				formValues.put(key, entry.getValue()[0]);
			}
		}else{
			//动态表单从请求中获取流程动态表单的数据
			List<FormProperty> formProperties = taskFormData.getFormProperties();
			for(FormProperty property : formProperties){
				if(property.isWritable()){//判断任务提交的表单中表单数据是否可写入,只有可以写入的数据在能提交
					String value = request.getParameter(property.getId());
					formValues.put(property.getId(), value);
				}
			}
		}
		
		formService.submitTaskFormData(taskid, formValues);
		return "redirect:/myTasks";
	}
	
	/**
	 * 获取审批人列表
	 * @return
	 */
	@RequestMapping(value = "auditUserList")
    @ResponseBody
    public Map<String, List<User>> usersByGroup() {
        List<Group> groups = identityService.createGroupQuery().list();
        Map<String, List<User>> usersByGroup = new HashMap<String, List<User>>(groups.size());
        for (Group group : groups) {
        	if("deptLeader".equals(group.getId())||"hr".equals(group.getId())){
        		List<User> users = identityService.createUserQuery().memberOfGroup(group.getId()).list();
                usersByGroup.put(group.getName(), users);
        	}
        }
        return usersByGroup;
    }
	
	/**
     * 读取意见
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
     */
    @RequestMapping(value = "readCommentList")
    @ResponseBody
    public Map<String, Object> list(@RequestParam(value = "processInstanceId",required = false) String processInstanceId,@RequestParam(value = "taskId", required = false) String taskId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, Object> commentAndEventsMap = new HashMap<String, Object>();
        //根据不同情况使用不同方式查询
        if (StringUtils.isNotBlank(processInstanceId)) {
        	//从流程实例的历史任务中读取意见
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            for (Comment comment : processInstanceComments) {
                String commentId = (String) PropertyUtils.getProperty(comment, "id");
                commentAndEventsMap.put(commentId, comment);
            }

            // //从流程实例的历史任务中获取每个任务的名称
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
            Map<String, String> taskNames = new HashMap<String, String>();
            for (HistoricTaskInstance historicTaskInstance : list) {
                taskNames.put(historicTaskInstance.getId(), historicTaskInstance.getName());
            }
            result.put("taskNames", taskNames);

        }
        //查询所有类型的事件
        if (StringUtils.isNotBlank(taskId)) { // 根据任务ID查询
            List<Event> taskEvents = taskService.getTaskEvents(taskId);
            for (Event event : taskEvents) {
                String eventId = (String) PropertyUtils.getProperty(event, "id");
                commentAndEventsMap.put(eventId, event);
            }
        }

        result.put("events", commentAndEventsMap.values());

        return result;
    }
    
    /**
     * 保存意见
     */
    @RequestMapping(value = "commentSave", method = RequestMethod.POST)
    @ResponseBody
    public Boolean addComment(@RequestParam("taskId") String taskId, @RequestParam("processInstanceId") String processInstanceId,@RequestParam("message") String message, HttpSession session) {
        identityService.setAuthenticatedUserId(UserUtil.getUserFromSession(session).getId());
        taskService.addComment(taskId, processInstanceId, message);
        return true;
    }
    
    /**
     * 更改任务属性
     *
     * @throws ParseException
     */
    @RequestMapping("taskProperty/{taskId}")
    @ResponseBody
    public String changeTaskProperty(@PathVariable("taskId") String taskId, @RequestParam("propertyName") String propertyName, @RequestParam("value") String value)
            throws ParseException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 更改到期日
        if (StringUtils.equals(propertyName, "dueDate")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parse = sdf.parse(value);
            task.setDueDate(parse);
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "priority")) {
            // 更改任务优先级
            task.setPriority(Integer.parseInt(value));
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "owner")) {
            // 更改拥有人
            task.setOwner(value);
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "assignee")) {
            // 更改办理人
            task.setAssignee(value);
            taskService.saveTask(task);
        } else {
            return "不支持[" + propertyName + "]属性！";
        }
        return "success";
    }
    
    /**
     * 添加子任务
     */
    @RequestMapping("taskSubtaskAdd/{taskId}")
    public String addSubTask(@PathVariable("taskId") String parentTaskId, @RequestParam("taskName") String taskName,
                             @RequestParam(value = "description", required = false) String description, HttpSession session) {
        Task newTask = taskService.newTask();
        newTask.setParentTaskId(parentTaskId);
        String userId = UserUtil.getUserFromSession(session).getId();
        newTask.setOwner(userId);
        newTask.setAssignee(userId);
        newTask.setName(taskName);
        newTask.setDescription(description);

        taskService.saveTask(newTask);
        return "redirect:/doTask/" + parentTaskId;
    }

    /**
     * 删除子任务
     */
    @RequestMapping("taskSubTaskDelete/{taskId}")
    public String deleteSubTask(@PathVariable("taskId") String taskId, HttpSession session) {
        String userId = UserUtil.getUserFromSession(session).getId();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String parentTaskId = task.getParentTaskId();
        taskService.deleteTask(taskId, "deleteByUser" + userId);
        return "redirect:/doTask/" + parentTaskId;
    }
    
    /**
     * 查看已结束任务
     */
    @RequestMapping(value = "taskArchived/{taskId}")
    public ModelAndView viewHistoryTask(@PathVariable("taskId") String taskId) throws Exception {
        String viewName = "taskArchived";
        ModelAndView mav = new ModelAndView(viewName);
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (task.getParentTaskId() != null) {
            HistoricTaskInstance parentTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getParentTaskId()).singleResult();
            mav.addObject("parentTask", parentTask);
        }
        mav.addObject("task", task);

        // 读取子任务
        List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery().taskParentTaskId(taskId).list();
        mav.addObject("subTasks", subTasks);

        // 读取附件
        List<Attachment> attachments = null;
        if (task.getTaskDefinitionKey() != null) {
            attachments = taskService.getTaskAttachments(taskId);
        } else {
            attachments = taskService.getProcessInstanceAttachments(task.getProcessInstanceId());
        }
        mav.addObject("attachments", attachments);

        return mav;
    }
    
    /**
     * 文件类型的附件
     */
    @RequestMapping(value = "attachmentAddFile")
    public String newFile(@RequestParam("taskId") String taskId, @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
                          @RequestParam("attachmentName") String attachmentName, @RequestParam(value = "attachmentDescription", required = false) String attachmentDescription,
                          @RequestParam("file") MultipartFile file, HttpSession session) {
        try {
            String attachmentType = file.getContentType() + ";" + FilenameUtils.getExtension(file.getOriginalFilename());
            identityService.setAuthenticatedUserId(UserUtil.getUserFromSession(session).getId());
            Attachment attachment = taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription,
                    file.getInputStream());
            taskService.saveAttachment(attachment);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/doTask/" + taskId;
    }

    /**
     * URL类型的附件
     */
    @RequestMapping(value = "attachmentAddUrl")
    public String newUrl(@RequestParam("taskId") String taskId, @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
                         @RequestParam("attachmentName") String attachmentName, @RequestParam(value = "attachmentDescription", required = false) String attachmentDescription,
                         @RequestParam("url") String url, HttpSession session) {
        String attachmentType = "url";
        identityService.setAuthenticatedUserId(UserUtil.getUserFromSession(session).getId());
        /*Attachment attachment = */
        taskService.createAttachment(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, url);
	    /*
	     * 如果要更新附件内容，先读取附件对象，然后设置属性（只能更新name和description），最后保存附件对象
	     */
        //taskService.saveAttachment(attachment);
        return "redirect:/doTask/" + taskId;
    }

    /**
     * 删除附件
     */
    @RequestMapping(value = "attachmentDelete/{attachmentId}")
    @ResponseBody
    public String delete(@PathVariable("attachmentId") String attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return "true";
    }

    /**
     * 下载附件
     *
     * @throws IOException
     */
    @RequestMapping(value = "attachmentDownload/{attachmentId}")
    public void downloadFile(@PathVariable("attachmentId") String attachmentId, HttpServletResponse response) throws IOException {
        Attachment attachment = taskService.getAttachment(attachmentId);
        InputStream attachmentContent = taskService.getAttachmentContent(attachmentId);
        String contentType = StringUtils.substringBefore(attachment.getType(), ";");
        response.addHeader("Content-Type", contentType + ";charset=UTF-8");
        String extensionFileName = StringUtils.substringAfter(attachment.getType(), ";");
        String fileName = attachment.getName() + "." + extensionFileName;
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        IOUtils.copy(new BufferedInputStream(attachmentContent), response.getOutputStream());
    }
    
    /**
     * 任务委派
     *
     * @param taskId
     * @param delegateUserId
     */
    @RequestMapping("taskDelegate/{taskId}")
    @ResponseBody
    public String delegate(@PathVariable("taskId") String taskId, @RequestParam("delegateUserId") String delegateUserId) {
        taskService.delegateTask(taskId, delegateUserId);
        return "success";
    }
    
    /**
     * 被委派任务者处理任务
     *
     * @param taskId
     */
    @RequestMapping("taskDelegateComplete/{taskId}")
    public String delegate(@PathVariable("taskId") String taskId) {
        taskService.resolveTask(taskId);
        return "redirect:/myTasks";
    }
 
	
}
