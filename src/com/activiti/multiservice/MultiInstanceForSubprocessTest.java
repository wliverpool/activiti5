package com.activiti.multiservice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MultiInstanceForSubprocessTest {
	
	private ClassPathXmlApplicationContext context = null;
	private ProcessEngineFactoryBean factoryBean = null;
	private RuntimeService runtimeService = null;
	private TaskService taskService = null;
	private IdentityService identityService = null;
	
	@Before
	public void setUp()throws Exception{
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		runtimeService = factoryBean.getObject().getRuntimeService();
		taskService = factoryBean.getObject().getTaskService();
		identityService = factoryBean.getObject().getIdentityService();
	}
	
	@Test
    public void testMultiInstanceForSubprocess() throws Exception {

        identityService.setAuthenticatedUserId("mittermeyer");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multiinstance-for-subprocess");

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskCandidateGroup("deptLeader").singleResult();
        taskService.claim(task.getId(), "mittermeyer");

        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        taskService.complete(task.getId(), variables);

        for (String user : users) {
            long count = taskService.createTaskQuery().taskAssignee(user).count();
            assertEquals(1, count);
            Task taskUser = taskService.createTaskQuery().taskAssignee(user).singleResult();
            taskService.claim(taskUser.getId(), user);
        }
        
        
    }

}
