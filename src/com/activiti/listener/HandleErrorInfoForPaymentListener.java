package com.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author 吴福明
 */
public class HandleErrorInfoForPaymentListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
    	//当前激活的活动id
        String currentActivityId = execution.getCurrentActivityId();
        if ("exclusivegateway-treasurerAudit".equals(currentActivityId)) {
            execution.setVariable("message", "财务审批未通过");
        } else if ("exclusivegateway-generalManagerAudit".equals(currentActivityId)) {
            execution.setVariable("message", "总经理审批未通过");
        }
    }

}
