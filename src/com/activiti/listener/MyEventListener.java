package com.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

/**
 * 实现一个activiti中自定义的事件监控器
 * @author 吴福明
 *
 */
public class MyEventListener implements ActivitiEventListener {

	@Override
	public boolean isFailOnException() {
		/*
		 * 当onEvent方法抛出异常时是否忽略异常
		 * 这里返回的是false，会忽略异常。 当返回true时，异常不会忽略，继续向上传播，迅速导致当前命令失败
		 */
		
		return false;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		//根据事件类型显示输出内容
		switch(event.getType()){
			//工作流引擎创建时
			case ENGINE_CREATED:System.out.println("ENGINE_CREATED..........");break;
			//工作流引擎关闭时
			case ENGINE_CLOSED:System.out.println("ENGINE_CLOSED.............");break;
			//任务执行成功时
			case JOB_EXECUTION_SUCCESS:System.out.println("JOB_EXECUTION_SUCCESS.............");break;
			//任务执行失败时
			case JOB_EXECUTION_FAILURE:System.out.println("JOB_EXECUTION_FAILURE.............");break;
			default:System.out.println("Event received: "+event.getType());
		}
	}

}
