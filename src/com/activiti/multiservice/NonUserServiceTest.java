package com.activiti.multiservice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NonUserServiceTest {
	
	private ClassPathXmlApplicationContext context = null;
	private ProcessEngineFactoryBean factoryBean = null;
	private RuntimeService runtimeService = null;
	private TaskService taskService = null;
	private HistoryService historyService = null;
	
	@Before
	public void setUp()throws Exception{
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		runtimeService = factoryBean.getObject().getRuntimeService();
		taskService = factoryBean.getObject().getTaskService();
		historyService = factoryBean.getObject().getHistoryService();
	}
	
	@Test
	public void testMultiInstanceFixedNumbers(){
		Map<String, Object> variables = new HashMap<String, Object>();
        long loop = 3;
        //����serviceTask���д���
        variables.put("loop", loop);
        variables.put("counter", 0); // ������
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMultiInstanceFixedNumbers", variables);
        Object variable = runtimeService.getVariable(processInstance.getId(), "counter");
        assertEquals(loop, variable);
	}
	
	@Test
	public void testMultiInstanceUserTasksBySequential(){
		Map<String, Object> variables = new HashMap<String, Object>();
		//��������֮ǰ���������û�����,����ʵ�����������������˳���ʵ���û�����
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);
        for (String userId : users) {
            Task task = taskService.createTaskQuery().taskAssignee(userId).singleResult();
            taskService.complete(task.getId());
        }
	}
	
	@Test
	public void testMultiInstanceUserTasksByParallel(){
		Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTaskParallel", variables);
        for (String userId : users) {
            assertEquals(1, taskService.createTaskQuery().taskAssignee(userId).count());
        }
        Task task = taskService.createTaskQuery().taskAssignee("user2").singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().taskAssignee("user1").singleResult();
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().taskAssignee("user3").singleResult();
        taskService.complete(task.getId());
	}
	
	@Test
	public void testMultiInstanceUserTasksByParallelWithCompleteCondition(){
		Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        //���ö��û�����ʵ�����������Ϊ60%
        variables.put("users", users);
        variables.put("rate", 0.6d);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTaskWithCompleteCondition", variables);

        Task task = taskService.createTaskQuery().taskAssignee("user1").singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().taskAssignee("user2").singleResult();
        taskService.complete(task.getId());
        //������ʵ�����ܹ�3������,�����2��֮���ж�����������ʵ���Ƿ����
        long count = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("testMultiInstanceForUserTaskWithCompleteCondition").finished().count();
        assertEquals(2, count);
	}

}
