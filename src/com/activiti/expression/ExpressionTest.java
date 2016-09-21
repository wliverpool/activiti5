package com.activiti.expression;

import static junit.framework.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ExpressionTest {
	
	@Test
	public void testExpression()throws Exception{
		
		//使用spring方式串讲activiti对象
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		assertNotNull(factoryBean);
		IdentityService identityService = factoryBean.getObject().getIdentityService();
		assertNotNull(identityService);
		RuntimeService runtimeService = factoryBean.getObject().getRuntimeService();
		TaskService taskService = factoryBean.getObject().getTaskService();
		
		MyBean myBean = context.getBean(MyBean.class);
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("myBean", myBean);
		String name = "Liverpool FC";
		variables.put("name", name);
		String businessKey = "9999";
		
		identityService.setAuthenticatedUserId("gerrard");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("expression", businessKey ,variables);
		assertEquals("gerrard", runtimeService.getVariable(processInstance.getId(), "authenticatedUserIdForTest"));
		assertEquals("Liverpool FC, added by print(String name)", runtimeService.getVariable(processInstance.getId(), "returnValue"));
		assertEquals(businessKey, runtimeService.getVariable(processInstance.getId(), "businessKey"));
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		String setByTask = (String) taskService.getVariable(task.getId(),"setByTask");
		assertEquals("I'm setted by DelegateTask, "+ name, setByTask);
		
	}

}
