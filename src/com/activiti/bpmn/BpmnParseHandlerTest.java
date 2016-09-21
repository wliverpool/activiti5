package com.activiti.bpmn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BpmnParseHandlerTest {
	
	private ProcessEngineFactoryBean factoryBean;
	private RepositoryService repositoryService;
	
	@Before
	public void setUp()throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		repositoryService = factoryBean.getObject().getRepositoryService();
	}
	
	@Test
	public void testParseHandler()throws Exception{
		String bpmnClasspath = "com/activiti/leave/leave.bpmn";
		String pngClasspath = "com/activiti/leave/leave.png";
		//部署流程定义资源
		DeploymentBuilder builder = repositoryService.createDeployment();
		builder.addClasspathResource(bpmnClasspath);
		builder.addClasspathResource(pngClasspath);
		builder.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave-3-modified by post parse handler").singleResult();
		//被解析器修改后
		assertEquals("请假流程-普通表单-被pre解析器修改", processDefinition.getName());
		assertEquals("leave-3-modified by post parse handler", processDefinition.getKey());
		
		RepositoryServiceImpl repositoryServiceImpl = (RepositoryServiceImpl) repositoryService;
        ReadOnlyProcessDefinition deployedProcessDefinition = repositoryServiceImpl.getDeployedProcessDefinition(processDefinition.getId());

        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) deployedProcessDefinition;
        //获得当前任务的所有节点
        List<ActivityImpl> activitiList = processDefinitionEntity.getActivities();
        for (ActivityImpl activity : activitiList) {
            System.out.println("Activity Name: " + activity.getProperty("name") + ", async=" + activity.isAsync());
        }
        //导出流程定义文件
        InputStream processBpmn = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
        String userHomeDir = getClass().getResource("/").getFile();
        FileUtils.copyInputStreamToFile(processBpmn, new File(userHomeDir + "/leave.bpmn20.xml"));
	}

}
