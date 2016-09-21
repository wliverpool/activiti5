package com.activiti.helloworld;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class HelloLeaveProcess {
	
	public static void main(String[] args) {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		String bpmnFileName = "com/activiti/helloworld/hello2.bpmn20.xml";
		DeploymentBuilder builder = repositoryService.createDeployment().addClasspathResource(bpmnFileName);
		builder.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionKey("helloLeave").singleResult();
		System.out.println("key:"+processDefinition.getKey());
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Map<String,Object> variables = new HashMap<String, Object>();
		variables.put("applyUser", "employee1");
		variables.put("days", 3);
		//��������ʵ��,��������ʵ����������̱���
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("helloLeave",variables);
		System.out.println("pid="+processInstance.getId()+", pdid="+processInstance.getProcessDefinitionId());
		TaskService taskService = processEngine.getTaskService();
		//��ѯdeptLeader����δǩ�յ�����
		Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
		System.out.println(deptLeaderTask.getName()+":"+deptLeaderTask.getId());
		//ǩ������
		taskService.claim(deptLeaderTask.getId(), "LeaderUser");
		variables = new HashMap<String, Object>();
		variables.put("approved", true);
		//�������
		taskService.complete(deptLeaderTask.getId(),variables);
		//��ѯ�Ѿ���ɵ�����ʵ��
		HistoryService historyService = processEngine.getHistoryService();
		long count = historyService.createHistoricProcessInstanceQuery().finished().count();
		System.out.println("finished:"+count);
	}

}
