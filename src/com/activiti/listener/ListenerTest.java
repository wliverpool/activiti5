package com.activiti.listener;

import static junit.framework.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ListenerTest {
	
	@Test
	public void testListener()throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		RuntimeService runtimeService = factoryBean.getObject().getRuntimeService();
		TaskService taskService = factoryBean.getObject().getTaskService();
		HistoryService historyService = factoryBean.getObject().getHistoryService();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assignmentDelegate", context.getBean(TaskAssigneeListener.class));
		variables.put("name","Liverpool FC");
		
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("listener", variables);
		
		String processInstanceId = instance.getId();
		assertTrue((boolean)runtimeService.getVariable(processInstanceId, "setInStartListener"));
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee("fowler").singleResult();
		String setInTaskCreate = (String)taskService.getVariable(task.getId(),"setInTaskCreate");
		assertEquals("create : Hello,Liverpool FC", setInTaskCreate);
		taskService.complete(task.getId());
		
		List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
		boolean hasVariableOfEndListener = false;
		for(HistoricVariableInstance variableInstance : list){
			if(variableInstance.getVariableName().equals("setInEndListener")){
				hasVariableOfEndListener = true;
			}
		}
		assertTrue(hasVariableOfEndListener);
	}

}
