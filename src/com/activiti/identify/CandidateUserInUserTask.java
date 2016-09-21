package com.activiti.identify;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;

public class CandidateUserInUserTask {
	
	public ProcessEngine processEngine = null;
	public RepositoryService repositoryService = null;
	public RuntimeService runtimeService = null;
	public TaskService taskService = null;
	
	@Before
	public void setUp()throws Exception{
		processEngine = ProcessEngines.getDefaultProcessEngine();
		repositoryService = processEngine.getRepositoryService();
		runtimeService = processEngine.getRuntimeService();
		taskService = processEngine.getTaskService();
		String bpmnFileName = "com/activiti/identify/candidateUserInUserTask.bpmn20.xml";
		DeploymentBuilder builder = repositoryService.createDeployment().addClasspathResource(bpmnFileName);
		builder.deploy();
	}
	
	@Test
	public void testUserTaskWithGroupContainsTwoUser()throws Exception{
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("candidateUserInUserTask");
		assertNotNull(processInstance);
		//两个用户都能查询到流程中分配给这两个用户的共同任务
		Task fowlerTask = taskService.createTaskQuery().taskCandidateUser("fowler").singleResult();
		assertNotNull(fowlerTask);
		Task wliverpoolTask = taskService.createTaskQuery().taskCandidateUser("wliverpool").singleResult();
		assertNotNull(wliverpoolTask);
		//一个用户签收了任务之后,两个用户就查询不到原先能查询到的任务
		taskService.claim(fowlerTask.getId(), "fowler");
		Task fowlerTask2 = taskService.createTaskQuery().taskCandidateUser("mittermeyer").singleResult();
		Task wliverpoolTask2 = taskService.createTaskQuery().taskCandidateUser("wliverpool").singleResult();
		assertNull(fowlerTask2);
		assertNull(wliverpoolTask2);
		taskService.complete(fowlerTask.getId());
	}
	
}
