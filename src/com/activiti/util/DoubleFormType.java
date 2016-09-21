package com.activiti.util;

import org.activiti.engine.form.AbstractFormType;
import org.apache.commons.lang3.ObjectUtils;

/**
 * �Զ���activiti��̬����������:double���ֶ�����, ��ʾ���������
 *
 * @author �⸣��
 */
public class DoubleFormType extends AbstractFormType {

	private static final long serialVersionUID = 3443291099366967779L;

	@Override
    public String getName() {
        return "double";
    }

    @Override
    public Object convertFormValueToModelValue(String propertyValue) {
        return new Double(propertyValue);
    }

    @Override
    public String convertModelValueToFormValue(Object modelValue) {
        return ObjectUtils.toString(modelValue);
    }

}
