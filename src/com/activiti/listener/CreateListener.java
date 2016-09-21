package com.activiti.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEventType;

public class CreateListener {
	
	public static void main(String[] args) {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		//使用代码在运行时期间为事件类型添加监听器,运行期添加的监听器引擎重启后就消失了
		runtimeService.addEventListener(new MyEventListener(),ActivitiEventType.ENGINE_CLOSED);
		processEngine.close();
	}

}
