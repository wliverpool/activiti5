package org.activiti.designer.test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

public class ProcessTestLeave {

	private String filename = "E:\\wufuming\\eworkspace\\activiti5\\src\\com\\activiti\\leave\\leaveProcess.bpmn";

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	public void testLeave() throws Exception {
		String currentUserId = "henryyan";
		//设置当前用户
		IdentityService identityService = activitiRule.getIdentityService();
		identityService.setAuthenticatedUserId(currentUserId);
		RepositoryService repositoryService = activitiRule.getRepositoryService();
		TaskService taskService = activitiRule.getTaskService();
		FormService formService = activitiRule.getFormService();
		HistoryService hisService = activitiRule.getHistoryService();
		ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").latestVersion().singleResult();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, String> variables = new HashMap<String,String>();
		Calendar ca = Calendar.getInstance();
		String startDate = sdf.format(ca.getTime());
		ca.add(Calendar.DAY_OF_MONTH, 2);
		String endDate = sdf.format(ca.getTime());
		variables.put("startDate", startDate);
		variables.put("endDate", endDate);
		variables.put("reason", "公休");
		//启动流程实例并设置动态表单的内容
		ProcessInstance proInstance = formService.submitStartFormData(definition.getId(), variables);
		assertNotNull(proInstance);
		//领导审批
		Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
		variables = new HashMap<String,String>();
		variables.put("deptLeaderApproved", "true");
		formService.submitTaskFormData(deptLeaderTask.getId(), variables);
		//hr审批
		Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
		variables = new HashMap<String,String>();
		variables.put("hrApproved", "true");
		formService.submitTaskFormData(hrTask.getId(), variables);
		//销假
		Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
		variables = new HashMap<String,String>();
		variables.put("reportBackDate", sdf.format(ca.getTime()));
		formService.submitTaskFormData(reportBackTask.getId(),variables);
		//验证流程是否结束
		HistoricProcessInstance historicProcessInstance = hisService.createHistoricProcessInstanceQuery().processDefinitionKey("leave").finished().processInstanceId(proInstance.getId()).singleResult();
		assertNotNull(historicProcessInstance);
		Map<String, Object> historyVariables = packageVariables(proInstance, hisService);
		assertEquals("ok", historyVariables.get("result"));
	}
	
	private Map<String,Object> packageVariables (ProcessInstance processInstance,HistoryService historyService){
		Map<String, Object> historyVariables = new HashMap<String,Object>();
		List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
		for(HistoricVariableInstance v : list){
			historyVariables.put(v.getVariableName(), v.getValue());
			System.out.println("variable:"+v.getVariableName()+"="+v.getValue());
		}
		return historyVariables;
	}
}