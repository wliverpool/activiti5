package com.activiti.processengine;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.db.DbSchemaCreate;

public class CreateProcessEngine {
	
	public static void main(String[] args) {
		//默认从classpath中寻找activiti.cfg.xml,根据里面配置的springbean创建流程引擎
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		//创建数据库表结构
		DbSchemaCreate.main(args);
		
	}

}
