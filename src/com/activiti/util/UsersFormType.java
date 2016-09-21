package com.activiti.util;

import org.activiti.engine.form.AbstractFormType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * �û����ֶ�����
 * 
 * @author �⸣��
 */
public class UsersFormType extends AbstractFormType {

	private static final long serialVersionUID = 4300847895359307232L;

	@Override
	public String getName() {
		return "users";
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		String[] split = StringUtils.split(propertyValue, ",");
		return Arrays.asList(split);
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		return ObjectUtils.toString(modelValue);
	}

}