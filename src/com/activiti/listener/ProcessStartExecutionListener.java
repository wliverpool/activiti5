package com.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class ProcessStartExecutionListener implements ExecutionListener {

	private static final long serialVersionUID = -7624706682023625536L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		execution.setVariable("setInStartListener", true);
		System.out.println(this.getClass().getSimpleName()+" : "+ execution.getEventName());
	}

}
