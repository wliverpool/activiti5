package com.activiti.bpmn;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BpmnModelTest {
	
	private ProcessEngineFactoryBean factoryBean;
	private RepositoryService repositoryService;
	private RuntimeService runtimeService;
	private TaskService taskService;
	
	@Before
	public void setUp()throws Exception{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		factoryBean = context.getBean(ProcessEngineFactoryBean.class);
		repositoryService = factoryBean.getObject().getRepositoryService();
		runtimeService = factoryBean.getObject().getRuntimeService();
		taskService = factoryBean.getObject().getTaskService();
	}

    /**
     * 把XML转换成BpmnModel对象
     * @throws Exception
     */
    @Test
    public void testXmlToBpmnModel() throws Exception {

        // 验证是否部署成功
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count>0);

        // 根据流程定义获取XML资源文件流对象
        /*ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();
        String resourceName = processDefinition.getResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);*/

        // 从classpath中获取
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("com/activiti/leave/leave.bpmn");

        // 创建转换对象
        BpmnXMLConverter converter = new BpmnXMLConverter();

        // 创建XMLStreamReader读取XML资源
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        // 把XML转换成BpmnModel对象
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);

        // 验证BpmnModel对象不为空
        assertNotNull(bpmnModel);
        Process process = bpmnModel.getMainProcess();
        assertEquals("leave-3", process.getId());
    }

    /**
     * 把BpmnModel转换为XML对象
     * @throws Exception
     */
    @Test
    public void testBpmnModelToXml() throws Exception {

        // 验证是否部署成功
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count>0);

        // 查询流程定义对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave-3").singleResult();

        // 获取BpmnModel对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        // 创建转换对象
        BpmnXMLConverter converter = new BpmnXMLConverter();

        // 把BpmnModel对象转换成字符（也可以输出到文件中）
        byte[] bytes = converter.convertToXML(bpmnModel);
        String xmlContent = new String(bytes,"utf-8");
        System.out.println(xmlContent);
    }
    
    @Test
    public void testDynamicDeploy()throws Exception{
    	BpmnModel model = new BpmnModel();
    	Process process = new Process();
    	model.addProcess(process);
    	process.setId("DynamicProcess");
    	
    	process.addFlowElement(createStartEvent());
    	process.addFlowElement(createUserTask("task1", "First task", "gerrard"));
    	process.addFlowElement(createUserTask("task2", "second task", "riise"));
    	process.addFlowElement(createEndEvent());
    	
    	process.addFlowElement(createSequenceFlow("start", "task1"));
    	process.addFlowElement(createSequenceFlow("task1", "task2"));
    	process.addFlowElement(createSequenceFlow("task2", "end"));
    	
    	new BpmnAutoLayout(model).execute();
    	
    	Deployment deployment = repositoryService.createDeployment().addBpmnModel("dynamic-model.bpmn", model).name("Dynamic Process Deployment").deploy();
    	
    	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("DynamicProcess");
    	
    	List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    	
    	assertEquals(1, tasks.size());
        assertEquals("First task", tasks.get(0).getName());
        assertEquals("gerrard", tasks.get(0).getAssignee());

        // 6. 导出流程图
        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        // 把文件生成在本章项目的test-classes目录中
        String userHomeDir = getClass().getResource("/").getFile();
        System.out.println(userHomeDir);
        FileUtils.copyInputStreamToFile(processDiagram, new File(userHomeDir + "/diagram.png"));

        // 7. 导出Bpmn文件到本地文件系统
        InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
        FileUtils.copyInputStreamToFile(processBpmn, new File(userHomeDir + "/process.bpmn20.xml"));
    }
    
    private UserTask createUserTask(String id,String name,String assignee){
    	UserTask userTask = new UserTask();
    	userTask.setName(name);
    	userTask.setId(id);
    	userTask.setAssignee(assignee);
    	return userTask;
    }
    
    private SequenceFlow createSequenceFlow(String from,String to){
    	SequenceFlow flow = new SequenceFlow();
    	flow.setSourceRef(from);
    	flow.setTargetRef(to);
    	return flow;
    }
    
    private StartEvent createStartEvent(){
    	StartEvent startEvent = new StartEvent();
    	startEvent.setId("start");
    	return startEvent;
    }
    
    private EndEvent createEndEvent(){
    	EndEvent endEvent = new EndEvent();
    	endEvent.setId("end");
    	return endEvent;
    }

}
