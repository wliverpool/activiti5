package com.activiti.bpmn;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

/**
 * �Զ���ǰ��bpmn����������
 * @author �⸣��
 *
 */
public class MyPreParseHandler extends AbstractBpmnParseHandler<Process> {

	/**
	 * ���������������
	 */
	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return Process.class;
	}

	/**
	 * ִ��ת���Ĳ���
	 */
	@Override
	protected void executeParse(BpmnParse bpmnParse, Process process) {
		process.setName(process.getName()+"-��pre�������޸�");
	}

}
