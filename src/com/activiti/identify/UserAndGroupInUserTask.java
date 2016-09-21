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

public class UserAndGroupInUserTask {
	
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
		String bpmnFileName = "com/activiti/identify/userAndGroupInUserTask.bpmn20.xml";
		DeploymentBuilder builder = repositoryService.createDeployment().addClasspathResource(bpmnFileName);
		builder.deploy();
	}
	
	@Test
	public void testUserAndGroupInUserTask()throws Exception{
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userAndGroupInUserTask");
		assertNotNull(processInstance);
		//mittermeyer是deptLeader组中一员所以能领取到deptLeader组中的任务
		Task task = taskService.createTaskQuery().taskCandidateUser("mittermeyer").singleResult();
		taskService.claim(task.getId(), "mittermeyer");
		taskService.complete(task.getId());
	}
	
	@Test
	public void testUserTaskWithGroupContainsTwoUser()throws Exception{
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userAndGroupInUserTask");
		assertNotNull(processInstance);
		//deptLeader组中两个成员都能查询到流程中分配给deptLeader组的任务
		Task mittermeyerTask = taskService.createTaskQuery().taskCandidateUser("mittermeyer").singleResult();
		assertNotNull(mittermeyerTask);
		Task hyypiaTask = taskService.createTaskQuery().taskCandidateUser("hyypia").singleResult();
		assertNotNull(hyypiaTask);
		//一个用户组中的成员签收了任务之后,用户组中的成员就查询不到原先能查询到的任务
		taskService.claim(mittermeyerTask.getId(), "mittermeyer");
		Task mittermeyerTask2 = taskService.createTaskQuery().taskCandidateUser("mittermeyer").singleResult();
		Task hyypiaTask2 = taskService.createTaskQuery().taskCandidateUser("hyypia").singleResult();
		assertNull(mittermeyerTask2);
		assertNull(hyypiaTask2);
		taskService.complete(mittermeyerTask.getId());
	}
	
}
