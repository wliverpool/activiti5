package com.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class ProcessEndExecutionListener implements ExecutionListener {

	private static final long serialVersionUID = 7089450945781156014L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		execution.setVariable("setInEndListener", true);
		System.out.println(this.getClass().getSimpleName()+" : "+ execution.getEventName());
	}

}
