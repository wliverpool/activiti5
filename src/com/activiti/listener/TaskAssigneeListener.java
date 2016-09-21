package com.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class TaskAssigneeListener implements TaskListener {

	private static final long serialVersionUID = -2695208114143748652L;

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println(delegateTask.getEventName()+",任务分配给："+delegateTask.getAssignee());
	}

}
