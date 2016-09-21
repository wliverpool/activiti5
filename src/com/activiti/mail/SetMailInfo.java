package com.activiti.mail;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.identity.User;

import java.util.Calendar;
import java.util.Date;

/**
 * �������--�ʼ�������������������÷����ʼ�ʱ��һЩ����
 * 
 * @author �⸣��
 */
public class SetMailInfo implements ExecutionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		IdentityService identityService = execution.getEngineServices().getIdentityService();
		String applyUserId = (String) execution.getVariable("applyUserId");
		User user = identityService.createUserQuery().userId(applyUserId).singleResult();
		execution.setVariableLocal("to", "elearn_admin@bankcomm.com");
		execution.setVariableLocal("name",user.getFirstName() + " " + user.getLastName());

		// ��ʱ����ʱ�����ã���ٽ���ʱ��+1��
		Date endDate = (Date) execution.getVariable("endDate");
		Calendar ca = Calendar.getInstance();
		//ca.setTime(endDate);
		ca.add(Calendar.MINUTE, 5);
		execution.setVariableLocal("reportBackTimeout", ca.getTime());
	}

}