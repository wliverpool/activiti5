package com.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

/**
 * ��ٻ�ǩ���������������ǩ�������ʱͳ��ͬ�������
 *
 * @author �⸣��
 */
@Component
public class LeaveCounterSignCompleteListener implements TaskListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void notify(DelegateTask delegateTask) {
        String approved = (String) delegateTask.getVariable("approved");
        if (approved.equals("true")) {
            Long agreeCounter = (Long) delegateTask.getVariable("approvedCounter");
            delegateTask.setVariable("approvedCounter", agreeCounter + 1);
        }
    }

}
