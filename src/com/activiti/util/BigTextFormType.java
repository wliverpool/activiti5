package com.activiti.util;

import org.activiti.engine.impl.form.StringFormType;

/**
 * 自定义activiti动态表单表单项类型：大文本表单字段
 *
 * @author 吴福明
 */
public class BigTextFormType extends StringFormType {

	private static final long serialVersionUID = -6020683607093811686L;

	@Override
    public String getName() {
        return "bigtext";
    }

}
