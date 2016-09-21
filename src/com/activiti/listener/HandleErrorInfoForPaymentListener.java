package com.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author �⸣��
 */
public class HandleErrorInfoForPaymentListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
    	//��ǰ����Ļid
        String currentActivityId = execution.getCurrentActivityId();
        if ("exclusivegateway-treasurerAudit".equals(currentActivityId)) {
            execution.setVariable("message", "��������δͨ��");
        } else if ("exclusivegateway-generalManagerAudit".equals(currentActivityId)) {
            execution.setVariable("message", "�ܾ�������δͨ��");
        }
    }

}
