package com.activiti.util;

import org.activiti.engine.impl.form.StringFormType;

/**
 * �Զ���activiti��̬���������ͣ����ı����ֶ�
 *
 * @author �⸣��
 */
public class BigTextFormType extends StringFormType {

	private static final long serialVersionUID = -6020683607093811686L;

	@Override
    public String getName() {
        return "bigtext";
    }

}
