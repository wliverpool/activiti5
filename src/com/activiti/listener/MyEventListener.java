package com.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

/**
 * ʵ��һ��activiti���Զ�����¼������
 * @author �⸣��
 *
 */
public class MyEventListener implements ActivitiEventListener {

	@Override
	public boolean isFailOnException() {
		/*
		 * ��onEvent�����׳��쳣ʱ�Ƿ�����쳣
		 * ���ﷵ�ص���false��������쳣�� ������trueʱ���쳣������ԣ��������ϴ�����Ѹ�ٵ��µ�ǰ����ʧ��
		 */
		
		return false;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		//�����¼�������ʾ�������
		switch(event.getType()){
			//���������洴��ʱ
			case ENGINE_CREATED:System.out.println("ENGINE_CREATED..........");break;
			//����������ر�ʱ
			case ENGINE_CLOSED:System.out.println("ENGINE_CLOSED.............");break;
			//����ִ�гɹ�ʱ
			case JOB_EXECUTION_SUCCESS:System.out.println("JOB_EXECUTION_SUCCESS.............");break;
			//����ִ��ʧ��ʱ
			case JOB_EXECUTION_FAILURE:System.out.println("JOB_EXECUTION_FAILURE.............");break;
			default:System.out.println("Event received: "+event.getType());
		}
	}

}
