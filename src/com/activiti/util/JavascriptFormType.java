package com.activiti.util;

import org.activiti.engine.form.AbstractFormType;

/**
 * �Զ��嶯̬����������,����activiti.cfg.xml��ע��
 * @author �⸣��
 *
 */
public class JavascriptFormType extends AbstractFormType {

	private static final long serialVersionUID = -2458577103133188775L;

	@Override
	public String getName() {
		//������������,�������ļ���activiti:formProperty�������typeҪһ��
		return "javascript";
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		//�ѱ���д������ת��Ϊjava�Ķ���
		return propertyValue;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		// ��java����ת��Ϊ�ַ���
		return (String)modelValue;
	}

}
