package com.activiti.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.pojo.Leave;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.activiti.listener.TaskAssigneeListener;
import com.activiti.service.LeaveService;
import com.activiti.util.UserUtil;

@Controller
@RequestMapping("/")
public class LeaveController{
	
	private static Logger logger = LoggerFactory.getLogger(LeaveController.class);
	
	private LeaveService leaveService;
	
	public LeaveService getLeaveService() {
		return leaveService;
	}

	@Autowired
	public void setLeaveService(LeaveService leaveService) {
		this.leaveService = leaveService;
	}
	
	@InitBinder  
	public void initBinder(WebDataBinder binder) {  
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    dateFormat.setLenient(false);  
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));  
	}
	
	@RequestMapping(value = "toLeaveApply")
	public ModelAndView toStartWorkflow(){
		ModelAndView mav = new ModelAndView("leaveApply");
		return mav;
	}

	/**
     * �����������
     */
    @RequestMapping(value = "leaveStart", method = RequestMethod.POST)
    public String startWorkflow(Leave leave, RedirectAttributes redirectAttributes, HttpSession session) {
    	//@RequestParam("endTime")Date endTime,@RequestParam("startTime")Date startTime,@RequestParam("leaveType")String leaveType,@RequestParam("reason")String reason
        //Leave leave = new Leave();
        //leave.setEndTime(endTime);
        //leave.setStartTime(startTime);
        //leave.setLeaveType(leaveType);
        //leave.setReason(reason);
    	try {
            User user = UserUtil.getUserFromSession(session);
            Map<String, Object> variables = new HashMap<String, Object>();
            ProcessInstance processInstance = leaveService.startLeaveFlow(leave, user.getId(), variables);
            redirectAttributes.addFlashAttribute("message", "����������������ID��" + processInstance.getId());
        } catch (ActivitiException e) {
            if (e.getMessage().indexOf("no processes deployed with key") != -1) {
                logger.warn("û�в�������!", e);
                redirectAttributes.addFlashAttribute("error", "û�в����������");
            } else {
                logger.error("�����������ʧ�ܣ�", e);
                redirectAttributes.addFlashAttribute("error", "ϵͳ�ڲ�����");
            }
        } catch (Exception e) {
            logger.error("�����������ʧ�ܣ�", e);
            redirectAttributes.addFlashAttribute("error", "ϵͳ�ڲ�����");
        }
        return "redirect:/toLeaveApply";
    }
    
    /**
     * �����б�
     *
     * @param leave
     */
    @RequestMapping(value = "toTaskLeaveList")
    public ModelAndView taskList(HttpSession session) {
        ModelAndView mav = new ModelAndView("leaveTaskList");
        String userId = UserUtil.getUserFromSession(session).getId();
        List<Leave> results = leaveService.findTodoTasks(userId);
        mav.addObject("records", results);
        return mav;
    }
    
    /**
     * ǩ������
     */
    @RequestMapping(value = "LeaveTaskClaim/{id}")
    public String claim(@PathVariable("id") String taskId, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = UserUtil.getUserFromSession(session).getId();
        leaveService.claimTask(taskId,userId);
        redirectAttributes.addFlashAttribute("message", "������ǩ��");
        return "redirect:/toTaskLeaveList";
    }
    
    /**
     * ������ϸ
     *
     * @param leave
     */
    @RequestMapping(value = "LeaveTaskView/{taskId}")
    public ModelAndView showTaskView(@PathVariable("taskId") String taskId) {
    	Leave leave = leaveService.getLeaveTaskDetail(taskId);
        ModelAndView mav = new ModelAndView("task-" + leave.getTask().getTaskDefinitionKey());
        mav.addObject("leave", leave);
        mav.addObject("task", leave.getTask());
        return mav;
    }
    
    /**
     * �����������
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "leaveAuditComplete/{taskId}", method = {RequestMethod.POST, RequestMethod.GET})
    public String completeAudit(@PathVariable("taskId") String taskId, @RequestParam("id")Long leaveId,HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, Object> variables = new HashMap<String, Object>();
        Enumeration<String> parameterNames = request.getParameterNames();
        try {
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                if (parameterName.startsWith("p_")) {
                    // �����ṹ��p_B_name��pΪ������ǰ׺��BΪ���ͣ�nameΪ��������
                    String[] parameter = parameterName.split("_");
                    if (parameter.length == 3) {
                        String paramValue = request.getParameter(parameterName);
                        Object value = paramValue;
                        //�����쵼����hr��������,����������ַ�����true����falseת���ɲ���ֵ
                        value = BooleanUtils.toBoolean(paramValue);
                        variables.put(parameter[2], value);
                    } else {
                        throw new RuntimeException("invalid parameter for activiti variable: " + parameterName);
                    }
                }
            }
            leaveService.completeAuditTask(taskId, variables);
            redirectAttributes.addFlashAttribute("message", "���������");
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
            request.setAttribute("error", "�������ʧ��");
        }
        return "redirect:/toTaskLeaveList";
    }
    
    /**
     * ��������ύ����
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "leaveTaskRecommit/{taskId}", method = {RequestMethod.POST, RequestMethod.GET})
    public String leaveTaskRecommit(@PathVariable("taskId") String taskId,@RequestParam("id")Long id,@RequestParam("endTime")Date endTime,@RequestParam("startTime")Date startTime,@RequestParam("leaveType")String leaveType,@RequestParam("reason")String reason, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, Object> variables = new HashMap<String, Object>();
        Enumeration<String> parameterNames = request.getParameterNames();
        Leave leave = new Leave();
        leave.setId(id);
        leave.setEndTime(endTime);
        leave.setStartTime(startTime);
        leave.setLeaveType(leaveType);
        leave.setReason(reason);
        try {
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                if (parameterName.startsWith("p_")) {
                    // �����ṹ��p_B_name��pΪ������ǰ׺��BΪ���ͣ�nameΪ��������
                    String[] parameter = parameterName.split("_");
                    if (parameter.length == 3) {
                        String paramValue = request.getParameter(parameterName);
                        Object value = paramValue;
                        if (parameter[1].equals("B")) {
                            value = BooleanUtils.toBoolean(paramValue);
                        } else if (parameter[1].equals("DT")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            value = sdf.parse(paramValue);
                        }
                        variables.put(parameter[2], value);
                    } else {
                        throw new RuntimeException("invalid parameter for activiti variable: " + parameterName);
                    }
                }
            }
            leaveService.completeNormalTask(leave, taskId, variables);
            redirectAttributes.addFlashAttribute("message", "���������");
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
            request.setAttribute("error", "�������ʧ��");
        }
        return "redirect:/toTaskLeaveList";
    }
    
    /**
     * �����������
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "leaveReportComplete/{taskId}", method = {RequestMethod.POST, RequestMethod.GET})
    public String leaveReportComplete(@PathVariable("taskId") String taskId, @RequestParam("id")Long leaveId,HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("reportBackEndProcessor", new TaskAssigneeListener());
        Enumeration<String> parameterNames = request.getParameterNames();
        Leave leave = new Leave();
        leave.setId(leaveId);
        try {
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                if (parameterName.startsWith("p_")) {
                    // �����ṹ��p_B_name��pΪ������ǰ׺��BΪ���ͣ�nameΪ��������
                    String[] parameter = parameterName.split("_");
                    if (parameter.length == 3) {
                        String paramValue = request.getParameter(parameterName);
                        Object value = paramValue;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        value = sdf.parse(paramValue);
                        variables.put(parameter[2], value);
                        if("realityStartTime".equals(parameter[2])){
                            leave.setRealityStartTime(sdf.parse(paramValue));
                        }else{
                        	leave.setRealityEndTime(sdf.parse(paramValue));
                        }
                    } else {
                        throw new RuntimeException("invalid parameter for activiti variable: " + parameterName);
                    }
                }
            }
            leaveService.completeNormalTask(leave,taskId, variables);
            redirectAttributes.addFlashAttribute("message", "���������");
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
            request.setAttribute("error", "�������ʧ��");
        }
        return "redirect:/toTaskLeaveList";
    }

}
