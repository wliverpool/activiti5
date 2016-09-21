package com.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;

public class CreateTaskListener implements TaskListener {

	private static final long serialVersionUID = 8205867222800489388L;
	
	private Expression content;
	private Expression task;

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println(task.getValue(delegateTask));
		delegateTask.setVariable("setInTaskCreate", delegateTask.getEventName()+" : "+content.getValue(delegateTask));
		System.out.println(delegateTask.getEventName()+",任务分配给："+delegateTask.getAssignee());
		//重新分配任务
		delegateTask.setAssignee("fowler");
	}

}
