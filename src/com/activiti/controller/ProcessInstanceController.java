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
		//�ж������Ƿ�ʹ�����ñ�
		ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
		
		User user = UserUtil.getUserFromSession(session);
        List<Group> groupList = (List<Group>) session.getAttribute("groups");

        // Ȩ������
        boolean startable = false;
        //��ȡ�����õ����̶���Ŀ������û�������
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
            redirectAttributes.addFlashAttribute("error", "����Ȩ������" + processDefinition.getName() + "�����̣�");
            return new ModelAndView("redirect:/processList");
        }

		boolean hasStartFormKey = processDefinition.hasStartFormKey();
		if(hasStartFormKey){//ʹ�������ñ�
			Object renderedStartForm = formService.getRenderedStartForm(processDefinitionId);
			mv.addObject("startFormData", renderedStartForm);
			mv.addObject("processDefinition", processDefinition);
		}else{//��̬��
			StartFormData startFormData = formService.getStartFormData(processDefinitionId);
			mv.addObject("startFormData",startFormData);
			mv.addObject("pdid",processDefinitionId);
		}
		mv.addObject("hasStartFormKey", hasStartFormKey);
		mv.setViewName("toStartProcessDyForm");
		return mv;
	}
	
	/**
	 * ��������ʵ��
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
			//���ύ��request�����л�ȡ��Ӧ�����ñ���ֵ
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
			for(Entry<String, String[]> entry : entrySet){
				String key = entry.getKey();
				formValues.put(key, entry.getValue()[0]);
			}
		}else {
			StartFormData formData = formService.getStartFormData(processDefinitionId);
			//�������л�ȡ���̶�̬��������
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
	 * �����б�
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "myTasks")
	public ModelAndView myTasks(HttpServletRequest request){
		ModelAndView mv = new ModelAndView("myTask");
		User user = UserUtil.getUserFromSession(request.getSession());
		//��ȡ�������ǰ�˵������б�
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
	 * ǩ������
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
        	redirectAttributes.addFlashAttribute("message","������ǩ��");
    		return "redirect:/myTasks";
        }
	}
	
	/**
	 * ��ʾ������ϸ
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
		if(null != taskFormData&&null!=taskFormData.getFormKey()){//���ñ�
			Object renderedTaskForm = formService.getRenderedTaskForm(taskid);
			task = taskService.createTaskQuery().taskId(taskid).singleResult();
			mv.addObject("taskFormData",renderedTaskForm);
			mv.addObject("hasFormKey",true);
		}else if(null != taskFormData){//��̬��
			mv.addObject("taskFormData",taskFormData);
			task = taskFormData.getTask();
		}else{
			task = taskService.createTaskQuery().taskId(taskid).singleResult();
            mv.addObject("manualTask", true);
		}
		mv.addObject("task", task);
		//��ȡ����ʵ������
		String processInstanceId = task.getProcessInstanceId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		mv.addObject("processInstance",processInstance);
		
		// ��ȡ����������б�
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskid);
        mv.addObject("identityLinksForTask", identityLinksForTask);

        // ��ȡ������Ա
        List<User> users = identityService.createUserQuery().list();
        mv.addObject("users", users);

        // ��ȡ������
        List<Group> groups = identityService.createGroupQuery().list();
        mv.addObject("groups", groups);

        // ��ȡ������
        List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery().taskParentTaskId(taskid).list();
        mv.addObject("subTasks", subTasks);

        // ��ȡ�ϼ�����
        if (task != null && task.getParentTaskId() != null) {
            HistoricTaskInstance parentTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getParentTaskId()).singleResult();
            mv.addObject("parentTask", parentTask);
        }

        // ��ȡ����
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
     * ��Ӳ�����
     */
    @RequestMapping("taskParticipantAdd/{taskId}")
    @ResponseBody
    public String addParticipants(@PathVariable("taskId") String taskId, @RequestParam("userId[]") String[] userIds, @RequestParam("type[]") String[] types,
                                  HttpServletRequest request) {
        // ���õ�ǰ�����ˣ����ڵ��û���Ի�ȡ����ǰ������
        String currentUserId = UserUtil.getUserFromSession(request.getSession()).getId();
        identityService.setAuthenticatedUserId(currentUserId);

        for (int i = 0; i < userIds.length; i++) {
            taskService.addUserIdentityLink(taskId, userIds[i], types[i]);
        }
        return "success";
    }
    
    
    /**
     * ɾ��������
     */
    @RequestMapping("taskParticipantDelete/{taskId}")
    @ResponseBody
    public String deleteParticipant(@PathVariable("taskId") String taskId, @RequestParam(value = "userId", required = false) String userId,@RequestParam(value = "groupId", required = false) String groupId, @RequestParam("type") String type) {
    	//�����û����飬ʹ�ò�ͬ�Ĵ���ʽ
        if (StringUtils.isNotBlank(groupId)) {
            taskService.deleteCandidateGroup(taskId, groupId);
        } else {
            taskService.deleteUserIdentityLink(taskId, userId, type);
        }
        return "success";
    }
    
    /**
     * ��Ӻ�ѡ��
     */
    @RequestMapping("taskCandidateAdd/{taskId}")
    @ResponseBody
    public String addCandidates(@PathVariable("taskId") String taskId, @RequestParam("userOrGroupIds[]") String[] userOrGroupIds,@RequestParam("type[]") String[] types, HttpServletRequest request) {
        // ���õ�ǰ�����ˣ����ڵ��û���Ի�ȡ����ǰ������
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
     * �������
     * @param taskid
     * @param request
     * @return
     */
	@RequestMapping(value = "completeTask/{taskid}")
	public String completeTask(@PathVariable("taskid") String taskid,HttpServletRequest request){
		TaskFormData taskFormData = formService.getTaskFormData(taskid);
		String formKey = taskFormData.getFormKey();
		Map<String, String> formValues = new HashMap<String, String>();
		
		if(StringUtils.isNotBlank(formKey)){//���ñ���request�����л�ȡ��д�ı�����
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<Entry<String, String[]>> entrySet = parameterMap.entrySet();
			for(Entry<String, String[]> entry : entrySet){
				String key = entry.getKey();
				formValues.put(key, entry.getValue()[0]);
			}
		}else{
			//��̬���������л�ȡ���̶�̬��������
			List<FormProperty> formProperties = taskFormData.getFormProperties();
			for(FormProperty property : formProperties){
				if(property.isWritable()){//�ж������ύ�ı��б������Ƿ��д��,ֻ�п���д������������ύ
					String value = request.getParameter(property.getId());
					formValues.put(property.getId(), value);
				}
			}
		}
		
		formService.submitTaskFormData(taskid, formValues);
		return "redirect:/myTasks";
	}
	
	/**
	 * ��ȡ�������б�
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
     * ��ȡ���
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
     */
    @RequestMapping(value = "readCommentList")
    @ResponseBody
    public Map<String, Object> list(@RequestParam(value = "processInstanceId",required = false) String processInstanceId,@RequestParam(value = "taskId", required = false) String taskId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Map<String, Object> result = new HashMap<String, Object>();

        Map<String, Object> commentAndEventsMap = new HashMap<String, Object>();
        //���ݲ�ͬ���ʹ�ò�ͬ��ʽ��ѯ
        if (StringUtils.isNotBlank(processInstanceId)) {
        	//������ʵ������ʷ�����ж�ȡ���
            List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
            for (Comment comment : processInstanceComments) {
                String commentId = (String) PropertyUtils.getProperty(comment, "id");
                commentAndEventsMap.put(commentId, comment);
            }

            // //������ʵ������ʷ�����л�ȡÿ�����������
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
            Map<String, String> taskNames = new HashMap<String, String>();
            for (HistoricTaskInstance historicTaskInstance : list) {
                taskNames.put(historicTaskInstance.getId(), historicTaskInstance.getName());
            }
            result.put("taskNames", taskNames);

        }
        //��ѯ�������͵��¼�
        if (StringUtils.isNotBlank(taskId)) { // ��������ID��ѯ
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
     * �������
     */
    @RequestMapping(value = "commentSave", method = RequestMethod.POST)
    @ResponseBody
    public Boolean addComment(@RequestParam("taskId") String taskId, @RequestParam("processInstanceId") String processInstanceId,@RequestParam("message") String message, HttpSession session) {
        identityService.setAuthenticatedUserId(UserUtil.getUserFromSession(session).getId());
        taskService.addComment(taskId, processInstanceId, message);
        return true;
    }
    
    /**
     * ������������
     *
     * @throws ParseException
     */
    @RequestMapping("taskProperty/{taskId}")
    @ResponseBody
    public String changeTaskProperty(@PathVariable("taskId") String taskId, @RequestParam("propertyName") String propertyName, @RequestParam("value") String value)
            throws ParseException {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // ���ĵ�����
        if (StringUtils.equals(propertyName, "dueDate")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parse = sdf.parse(value);
            task.setDueDate(parse);
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "priority")) {
            // �����������ȼ�
            task.setPriority(Integer.parseInt(value));
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "owner")) {
            // ����ӵ����
            task.setOwner(value);
            taskService.saveTask(task);
        } else if (StringUtils.equals(propertyName, "assignee")) {
            // ���İ�����
            task.setAssignee(value);
            taskService.saveTask(task);
        } else {
            return "��֧��[" + propertyName + "]���ԣ�";
        }
        return "success";
    }
    
    /**
     * ���������
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
     * ɾ��������
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
     * �鿴�ѽ�������
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

        // ��ȡ������
        List<HistoricTaskInstance> subTasks = historyService.createHistoricTaskInstanceQuery().taskParentTaskId(taskId).list();
        mav.addObject("subTasks", subTasks);

        // ��ȡ����
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
     * �ļ����͵ĸ���
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
     * URL���͵ĸ���
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
	     * ���Ҫ���¸������ݣ��ȶ�ȡ��������Ȼ���������ԣ�ֻ�ܸ���name��description������󱣴渽������
	     */
        //taskService.saveAttachment(attachment);
        return "redirect:/doTask/" + taskId;
    }

    /**
     * ɾ������
     */
    @RequestMapping(value = "attachmentDelete/{attachmentId}")
    @ResponseBody
    public String delete(@PathVariable("attachmentId") String attachmentId) {
        taskService.deleteAttachment(attachmentId);
        return "true";
    }

    /**
     * ���ظ���
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
     * ����ί��
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
     * ��ί�������ߴ�������
     *
     * @param taskId
     */
    @RequestMapping("taskDelegateComplete/{taskId}")
    public String delegate(@PathVariable("taskId") String taskId) {
        taskService.resolveTask(taskId);
        return "redirect:/myTasks";
    }
 
	
}
