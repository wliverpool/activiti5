package com.activiti.bpmn;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

/**
 * 自定义前置bpmn解析处理器
 * @author 吴福明
 *
 */
public class MyPreParseHandler extends AbstractBpmnParseHandler<Process> {

	/**
	 * 解析器处理的类型
	 */
	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return Process.class;
	}

	/**
	 * 执行转换的步骤
	 */
	@Override
	protected void executeParse(BpmnParse bpmnParse, Process process) {
		process.setName(process.getName()+"-被pre解析器修改");
	}

}
