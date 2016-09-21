package com.activiti.controller;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.activiti.util.ActivitiUtils;

@Controller
@RequestMapping(value = "/")
public class TraceProcessController {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	TaskService taskService;

	@Autowired
	HistoryService historyService;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	protected IdentityService identityService;
	
	@Autowired
    ProcessEngineConfiguration processEngineConfiguration;

	/**
	 * ��ȡ��ʷ����
	 * 
	 * @return
	 */
	@RequestMapping(value = "processTraceView/{executionId}")
	public ModelAndView historyDatas(@PathVariable("executionId") String executionId) {
		
		ModelAndView mav = new ModelAndView("traceProcess");

		// ��ѯExecution����
		Execution execution = runtimeService.createExecutionQuery()
				.executionId(executionId).singleResult();

		// ��ѯ���е���ʷ���¼
		String processInstanceId = execution.getProcessInstanceId();
		List<HistoricActivityInstance> activityInstances = historyService
				.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).list();

		// ��ѯ��ʷ����ʵ��
		HistoricProcessInstance historicProcessInstance = historyService
				.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();

		// ��ѯ�����йصı���
		List<HistoricVariableInstance> variableInstances = historyService
				.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstanceId).list();

		List<HistoricDetail> formProperties = historyService
				.createHistoricDetailQuery()
				.processInstanceId(processInstanceId).formProperties().list();

		// ��ѯ���̶������
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery()
				.processDefinitionId(
						historicProcessInstance.getProcessDefinitionId())
				.singleResult();

		// ��ѯ����ʱ����ʵ��
		ProcessInstance parentProcessInstance = runtimeService
				.createProcessInstanceQuery()
				.subProcessInstanceId(execution.getProcessInstanceId())
				.singleResult();

		mav.addObject("parentProcessInstance", parentProcessInstance);
		mav.addObject("historicProcessInstance", historicProcessInstance);
		mav.addObject("variableInstances", variableInstances);
		mav.addObject("activities", activityInstances);
		mav.addObject("formProperties", formProperties);
		mav.addObject("processDefinition", processDefinition);
		mav.addObject("executionId", executionId);

		return mav;
	}
	
	/**
     * ��ȡ��������
     *
     * @param executionId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "traceData/{executionId}")
    @ResponseBody
    public List<Map<String, Object>> readActivityDatas(@PathVariable("executionId") String executionId) throws Exception {
        ExecutionEntity executionEntity = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        //��ȡ����ʵ���м���Ļ
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionId);
        //��ȡ���̶���
        RepositoryServiceImpl repositoryServiceImpl = (RepositoryServiceImpl) repositoryService;
        ReadOnlyProcessDefinition deployedProcessDefinition = repositoryServiceImpl.getDeployedProcessDefinition(executionEntity.getProcessDefinitionId());

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) deployedProcessDefinition;
        //�����̶����л�ȡ�������ڵ�
        List<ActivityImpl> activitiList = getAllActivitiByProcessDefinition(processDefinition);

        List<Map<String, Object>> activityInfos = new ArrayList<Map<String, Object>>();
        for (ActivityImpl activity : activitiList) {
        	
            ActivityBehavior activityBehavior = activity.getActivityBehavior();

            boolean currentActiviti = false;
            //�жϵ�ǰ�ڵ��Ƿ��Ǽ���Ľڵ�
            String activityId = activity.getId();
            if (activeActivityIds.contains(activityId)) {
                currentActiviti = true;
            }
            //��ȡ��ǰ�ڵ���Ϣ
            Map<String, Object> activityImageInfo = packageSingleActivitiInfo(activity, executionEntity.getId(), currentActiviti);
            activityInfos.add(activityImageInfo);

            // ����������
            if (activityBehavior instanceof SubProcessActivityBehavior) {
                List<ActivityImpl> innerActivityList = activity.getActivities();
                for (ActivityImpl innerActivity : innerActivityList) {
                    String innerActivityId = innerActivity.getId();
                    if (activeActivityIds.contains(innerActivityId)) {
                        currentActiviti = true;
                    } else {
                        currentActiviti = false;
                    }
                    activityImageInfo = packageSingleActivitiInfo(innerActivity, executionEntity.getId(), currentActiviti);
                    activityInfos.add(activityImageInfo);
                }
            }

        }

        return activityInfos;
    }
    
    /**
     * ��ȡ������Դ
     */
    @RequestMapping(value = "traceDataAuto/{executionId}")
    public void readResource(@PathVariable("executionId") String executionId, HttpServletResponse response)
            throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(executionId);
        List<String> highLightedFlows = getHighLightedFlows(processDefinition, processInstance.getId());
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        InputStream imageStream =diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows);

        // �����Դ���ݵ���Ӧ����
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
        List<String> highLightedFlows = new ArrayList<String>();
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> historicActivityInstanceList = new ArrayList<String>();
        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        for (ActivityImpl activity : processDefinition.getActivities()) {
            int index = historicActivityInstanceList.indexOf(activity.getId());

            if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
                List<PvmTransition> pvmTransitionList = activity
                        .getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination().getId();
                    if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }
        return highLightedFlows;
    }
    
    /**
     * ��װ�����Ϣ����������ǰ�ڵ��X��Y���ꡢ������Ϣ���������͡���������
     *
     * @param activity
     * @param currentActiviti
     * @return
     */
    private Map<String, Object> packageSingleActivitiInfo(ActivityImpl activity, String executionId,
                                                          boolean currentActiviti) throws Exception {
        Map<String, Object> activityInfo = new HashMap<String, Object>();
        activityInfo.put("currentActiviti", currentActiviti);

        // ����ͼ�ε�XY�����Լ���ȡ��߶�
        setSizeAndPositonInfo(activity, activityInfo);

        Map<String, Object> vars = new HashMap<String, Object>();
        Map<String, Object> properties = activity.getProperties();
        vars.put("��������", ActivitiUtils.getZhActivityType(properties.get("type").toString()));
        vars.put("��������", properties.get("name"));
        
        // ��ǰ�ڵ��task
        if (currentActiviti) {
            setCurrentTaskInfo(executionId, activity.getId(), vars);
        }

        logger.debug("trace variables: {}", vars);
        activityInfo.put("vars", vars);
        return activityInfo;
    }

    /**
     * ��ȡ��ǰ�ڵ���Ϣ
     *
     * @return
     */
    private void setCurrentTaskInfo(String executionId, String activityId, Map<String, Object> vars) {
        Task currentTask = taskService.createTaskQuery().executionId(executionId)
                .taskDefinitionKey(activityId).singleResult();
        logger.debug("current task for processInstance: {}", ToStringBuilder.reflectionToString(currentTask));

        if (currentTask == null) return;

        String assignee = currentTask.getAssignee();
        if (assignee != null) {
            User assigneeUser = identityService.createUserQuery().userId(assignee).singleResult();
            String userInfo = assigneeUser.getFirstName() + " " + assigneeUser.getLastName() + "/" + assigneeUser.getId();
            vars.put("��ǰ������", userInfo);
            vars.put("����ʱ��", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(currentTask.getCreateTime()));
        } else {
            vars.put("����״̬", "δǩ��");
        }

    }
    
    /**
     * ���ÿ�ȡ��߶ȡ���������
     *
     * @param activity
     * @param activityInfo
     */
    private void setSizeAndPositonInfo(ActivityImpl activity, Map<String, Object> activityInfo) {
        activityInfo.put("width", activity.getWidth());
        activityInfo.put("height", activity.getHeight());
        activityInfo.put("x", activity.getX());
        activityInfo.put("y", activity.getY());
    }
    
    private List<ActivityImpl> getAllActivitiByProcessDefinition(ProcessDefinitionEntity processDefinition){
    	
        List<ActivityImpl> activitiList = new ArrayList<ActivityImpl>();
        //��ȡ�����ɵĽڵ�
        List<ActivityImpl> mainActivitiList = processDefinition.getActivities();
        
        for(ActivityImpl activity : mainActivitiList){
        	loadActivity(activity,activitiList);
        }
        
        return activitiList;
    }
    
    /**
     * ���ص�ǰ�ڵ�������ֽڵ�
     */
    private void loadActivity(ActivityImpl activiti,List<ActivityImpl> activitiList){
    	activitiList.add(activiti);
    	//�ݹ��ȡ��ǰ�ڵ��������activity
    	List<ActivityImpl> subActivityList = activiti.getActivities();
    	if(null!=subActivityList){
    		for(ActivityImpl sub : subActivityList){
    			activitiList.add(sub);
    			loadActivity(sub, activitiList);
    		}
    	}
    }

}
