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
		//ʹ��Ĭ�ϵ�activiti.cfg.xml���ô�����������
		//ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		//ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		//�������̶����ļ�
		RepositoryService repositoryService = factoryBean.getObject().getRepositoryService();
		DeploymentBuilder builder = repositoryService.createDeployment().addClasspathResource("com/activiti/helloworld/hello.bpmn20.xml");
		builder.deploy();
		//��ѯ�Ѿ���������̶���
		//ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
		//System.out.println("==================="+processDefinition.getKey()+"===================");
		//��������ʵ��
		//RuntimeService runtimeService = processEngine.getRuntimeService();
		//ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("hello");
		//System.out.println(processInstance.toString());
		//System.out.println("pid="+processInstance.getId()+", pdid="+processInstance.getProcessDefinitionId());
	}

}
