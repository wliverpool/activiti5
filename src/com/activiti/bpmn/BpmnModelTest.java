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
     * ��XMLת����BpmnModel����
     * @throws Exception
     */
    @Test
    public void testXmlToBpmnModel() throws Exception {

        // ��֤�Ƿ���ɹ�
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count>0);

        // �������̶����ȡXML��Դ�ļ�������
        /*ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();
        String resourceName = processDefinition.getResourceName();
        InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);*/

        // ��classpath�л�ȡ
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("com/activiti/leave/leave.bpmn");

        // ����ת������
        BpmnXMLConverter converter = new BpmnXMLConverter();

        // ����XMLStreamReader��ȡXML��Դ
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        // ��XMLת����BpmnModel����
        BpmnModel bpmnModel = converter.convertToBpmnModel(reader);

        // ��֤BpmnModel����Ϊ��
        assertNotNull(bpmnModel);
        Process process = bpmnModel.getMainProcess();
        assertEquals("leave-3", process.getId());
    }

    /**
     * ��BpmnModelת��ΪXML����
     * @throws Exception
     */
    @Test
    public void testBpmnModelToXml() throws Exception {

        // ��֤�Ƿ���ɹ�
        long count = repositoryService.createProcessDefinitionQuery().count();
        assertTrue(count>0);

        // ��ѯ���̶������
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave-3").singleResult();

        // ��ȡBpmnModel����
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        // ����ת������
        BpmnXMLConverter converter = new BpmnXMLConverter();

        // ��BpmnModel����ת�����ַ���Ҳ����������ļ��У�
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

        // 6. ��������ͼ
        InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
        // ���ļ������ڱ�����Ŀ��test-classesĿ¼��
        String userHomeDir = getClass().getResource("/").getFile();
        System.out.println(userHomeDir);
        FileUtils.copyInputStreamToFile(processDiagram, new File(userHomeDir + "/diagram.png"));

        // 7. ����Bpmn�ļ��������ļ�ϵͳ
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
