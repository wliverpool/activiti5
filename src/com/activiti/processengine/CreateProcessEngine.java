package com.activiti.processengine;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.db.DbSchemaCreate;

public class CreateProcessEngine {
	
	public static void main(String[] args) {
		//Ĭ�ϴ�classpath��Ѱ��activiti.cfg.xml,�����������õ�springbean������������
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		//�������ݿ��ṹ
		DbSchemaCreate.main(args);
		
	}

}
