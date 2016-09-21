package com.activiti.processengine;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DeploymentTest {
	
	public RepositoryService repositoryService = null;
	
	@Before
	public void setUp()throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ProcessEngineFactoryBean factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		repositoryService = factoryBean.getObject().getRepositoryService();
	}
	
	@Test
	public void testClasspathDeployment() throws Exception{
		String bpmnClasspath = "com/activiti/leave/leave.bpmn";
		String pngClasspath = "com/activiti/leave/leave.png";
		//部署流程定义资源
		DeploymentBuilder builder = repositoryService.createDeployment();
		builder.addClasspathResource(bpmnClasspath);
		builder.addClasspathResource(pngClasspath);
		//部署使用外置表单的流程需要添加外置表单资源
		//builder.addClasspathResource("com/activiti/leave/leave-start.form");
		//builder.addClasspathResource("com/activiti/leave/approve-deptLeader.form");
		//builder.addClasspathResource("com/activiti/leave/approve-hr.form");
		//builder.addClasspathResource("com/activiti/leave/report-back.form");
		//builder.addClasspathResource("com/activiti/leave/modify-apply.form");
		builder.deploy();
		//验证是否部署成功
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		long count = query.processDefinitionKey("leave-3").latestVersion().count();
		assertTrue(count>0);
		ProcessDefinition definition = query.singleResult();
		String diagramResourceName = definition.getDiagramResourceName();
		assertEquals(pngClasspath,diagramResourceName);
	}
	/*
	@Test
	public void testInputStreamDeployment() throws Exception{
		//inputstream方式只能部署xml的流程定义文件
		String filePath = "E:\\wufuming\\eworkspace\\activiti5\\src\\com\\activiti\\helloworld\\test.bpmn";
		FileInputStream fileInputStream = new FileInputStream(filePath);
		DeploymentBuilder builder = repositoryService.createDeployment();
		builder.addInputStream("myProcess.bpmn", fileInputStream);
		builder.deploy();
		//验证是否部署成功
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		long count = query.processDefinitionKey("myProcess").latestVersion().count();
		assertTrue(count>0);
	}
	
	@Test
	public void testStringDeployment()throws Exception{
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>				<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" targetNamespace=\"http://www.activiti.org/test\">				  <process id=\"myProcess\" name=\"My process\" isExecutable=\"true\">				    <startEvent id=\"startevent1\" name=\"Start\"></startEvent>				    <userTask id=\"deptLeaveAudit\" name=\"领导审批\" activiti:assignee=\"leader\"></userTask>				    <sequenceFlow id=\"flow1\" sourceRef=\"startevent1\" targetRef=\"deptLeaveAudit\"></sequenceFlow>				    <userTask id=\"ehrAudit\" name=\"人事审批\" activiti:assignee=\"ehr\"></userTask>				    <sequenceFlow id=\"flow2\" sourceRef=\"deptLeaveAudit\" targetRef=\"ehrAudit\"></sequenceFlow>				    <userTask id=\"reportBack\" name=\"销假\" activiti:assignee=\"proposer\"></userTask>				    <sequenceFlow id=\"flow3\" sourceRef=\"ehrAudit\" targetRef=\"reportBack\"></sequenceFlow>				    <endEvent id=\"endevent1\" name=\"End\"></endEvent>				    <sequenceFlow id=\"flow4\" sourceRef=\"reportBack\" targetRef=\"endevent1\"></sequenceFlow>				  </process>				  <bpmndi:BPMNDiagram id=\"BPMNDiagram_myProcess\">				    <bpmndi:BPMNPlane bpmnElement=\"myProcess\" id=\"BPMNPlane_myProcess\">				      <bpmndi:BPMNShape bpmnElement=\"startevent1\" id=\"BPMNShape_startevent1\">				        <omgdc:Bounds height=\"35.0\" width=\"35.0\" x=\"70.0\" y=\"200.0\"></omgdc:Bounds>				      </bpmndi:BPMNShape>				      <bpmndi:BPMNShape bpmnElement=\"deptLeaveAudit\" id=\"BPMNShape_deptLeaveAudit\">				        <omgdc:Bounds height=\"55.0\" width=\"105.0\" x=\"170.0\" y=\"190.0\"></omgdc:Bounds>				      </bpmndi:BPMNShape>				      <bpmndi:BPMNShape bpmnElement=\"ehrAudit\" id=\"BPMNShape_ehrAudit\">				        <omgdc:Bounds height=\"55.0\" width=\"105.0\" x=\"320.0\" y=\"190.0\"></omgdc:Bounds>				      </bpmndi:BPMNShape>				      <bpmndi:BPMNShape bpmnElement=\"reportBack\" id=\"BPMNShape_reportBack\">				        <omgdc:Bounds height=\"55.0\" width=\"105.0\" x=\"470.0\" y=\"190.0\"></omgdc:Bounds>				      </bpmndi:BPMNShape>				      <bpmndi:BPMNShape bpmnElement=\"endevent1\" id=\"BPMNShape_endevent1\">				        <omgdc:Bounds height=\"35.0\" width=\"35.0\" x=\"620.0\" y=\"200.0\"></omgdc:Bounds>				      </bpmndi:BPMNShape>				      <bpmndi:BPMNEdge bpmnElement=\"flow1\" id=\"BPMNEdge_flow1\">				        <omgdi:waypoint x=\"105.0\" y=\"217.0\"></omgdi:waypoint>				        <omgdi:waypoint x=\"170.0\" y=\"217.0\"></omgdi:waypoint>				      </bpmndi:BPMNEdge>				      <bpmndi:BPMNEdge bpmnElement=\"flow2\" id=\"BPMNEdge_flow2\">				        <omgdi:waypoint x=\"275.0\" y=\"217.0\"></omgdi:waypoint>				        <omgdi:waypoint x=\"320.0\" y=\"217.0\"></omgdi:waypoint>				      </bpmndi:BPMNEdge>				      <bpmndi:BPMNEdge bpmnElement=\"flow3\" id=\"BPMNEdge_flow3\">				        <omgdi:waypoint x=\"425.0\" y=\"217.0\"></omgdi:waypoint>				        <omgdi:waypoint x=\"470.0\" y=\"217.0\"></omgdi:waypoint>				      </bpmndi:BPMNEdge>				      <bpmndi:BPMNEdge bpmnElement=\"flow4\" id=\"BPMNEdge_flow4\">				        <omgdi:waypoint x=\"575.0\" y=\"217.0\"></omgdi:waypoint>				        <omgdi:waypoint x=\"620.0\" y=\"217.0\"></omgdi:waypoint>				      </bpmndi:BPMNEdge>				    </bpmndi:BPMNPlane>				  </bpmndi:BPMNDiagram>				</definitions>";
		DeploymentBuilder builder = repositoryService.createDeployment();
		builder.addString("myProcess.bpmn", text);
		builder.deploy();
		//验证是否部署成功
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		long count = query.processDefinitionKey("myProcess").latestVersion().count();
		assertTrue(count>0);
	}
	
	@Test
	public void testZipStreamDeployment()throws Exception{
		InputStream zipSteam = getClass().getClassLoader().getResourceAsStream("com/activiti/helloworld/test.zip");
		repositoryService.createDeployment().addZipInputStream(new ZipInputStream(zipSteam)).deploy();
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		long count = query.processDefinitionKey("myProcess").latestVersion().count();
		assertTrue(count>0);
	}
	
	@Test
	public void testReadDeployementList()throws Exception{
		List<ProcessDefinition> lastVersionList = repositoryService.createProcessDefinitionQuery().latestVersion().list();
		for(ProcessDefinition definition : lastVersionList){
			System.out.println("id:"+definition.getId());
			System.out.println("deploymentId:"+definition.getDeploymentId());
			System.out.println("name:"+definition.getName());
			System.out.println("version:"+definition.getVersion());
			System.out.println("resourcenName:"+definition.getResourceName());
			System.out.println("diagramResourceName:"+definition.getDiagramResourceName());
		}
		List<ProcessDefinition> versionList = repositoryService.createProcessDefinitionQuery().list();
		for(ProcessDefinition definition : versionList){
			System.out.println("id:"+definition.getId());
			System.out.println("deploymentId:"+definition.getDeploymentId());
			System.out.println("name:"+definition.getName());
			System.out.println("version:"+definition.getVersion());
			System.out.println("resourcenName:"+definition.getResourceName());
			System.out.println("diagramResourceName:"+definition.getDiagramResourceName());
		}
	}*/

}
