package com.activiti.helloworld;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HellowWorldProcess {
	
	public static void main(String[] args)throws Exception {
		//使用默认的activiti.cfg.xml配置创建流程引擎
		//ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		//ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		//部署流程定义文件
		RepositoryService repositoryService = factoryBean.getObject().getRepositoryService();
		DeploymentBuilder builder = repositoryService.createDeployment().addClasspathResource("com/activiti/helloworld/hello.bpmn20.xml");
		builder.deploy();
		//查询已经部署的流程定义
		//ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
		//System.out.println("==================="+processDefinition.getKey()+"===================");
		//启动流程实例
		//RuntimeService runtimeService = processEngine.getRuntimeService();
		//ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("hello");
		//System.out.println(processInstance.toString());
		//System.out.println("pid="+processInstance.getId()+", pdid="+processInstance.getProcessDefinitionId());
	}

}
