package com.activiti.util;

import org.activiti.engine.form.AbstractFormType;

/**
 * 自定义动态表单数据类型,需在activiti.cfg.xml中注册
 * @author 吴福明
 *
 */
public class JavascriptFormType extends AbstractFormType {

	private static final long serialVersionUID = -2458577103133188775L;

	@Override
	public String getName() {
		//定义类型名称,与流程文件中activiti:formProperty里的属性type要一致
		return "javascript";
	}

	@Override
	public Object convertFormValueToModelValue(String propertyValue) {
		//把表单填写的内容转换为java的对象
		return propertyValue;
	}

	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		// 把java对象转换为字符型
		return (String)modelValue;
	}

}
