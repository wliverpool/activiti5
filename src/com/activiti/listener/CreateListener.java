package com.activiti.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;

public class CreateListener {
	
	public static void main(String[] args) {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		//ʹ�ô���������ʱ�ڼ�Ϊ�¼�������Ӽ�����,��������ӵļ������������������ʧ��
		runtimeService.addEventListener(new MyEventListener(),ActivitiEventType.ENGINE_CLOSED);
		processEngine.close();
	}

}
