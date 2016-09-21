package com.activiti.util;

import org.activiti.engine.form.AbstractFormType;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 自定义activiti动态表单表单项类型:double表单字段类型, 显示浮点数金额
 *
 * @author 吴福明
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
