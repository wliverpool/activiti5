package com.activiti.mail;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MailTest {
	
	@Test
	public void testSendMail()throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		RuntimeService runtimeService = factoryBean.getObject().getRuntimeService();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("name", "Liverpool FC");
		variables.put("to", "elearn_admin@bankcomm.com");
		variables.put("from", "amis_admin@bankcomm.com");
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("testMailTask",variables);
		assertNotNull(instance);
	}

}
