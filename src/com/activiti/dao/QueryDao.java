package com.activiti.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class QueryDao {
	
	private TaskService taskService;
	private ManagementService managementService;
	private RuntimeService runtimeService;
	
	public RuntimeService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
	
	public ManagementService getManagementService() {
		return managementService;
	}

	public void setManagementService(ManagementService managementService) {
		this.managementService = managementService;
	}

	/**
	 * ͨ���Զ���sql��ѯ�����б�
	 */
	public List<Task> queryTaskListByNativeQuery(){
		//ManagementService���Ը��ݴ����activiti�е�entity���ȡ����entity��Ӧ�ı���
		List<Task> tasks = taskService.createNativeTaskQuery().sql("select * from " + managementService.getTableName(Task.class) + " T where T.NAME_=#{taskName}")
				.parameter("taskName", "��������").list();
		return tasks;
	}
	
	public List<Task> queryTaskListByPage(){
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("mittermeyer").listPage(6, 5);
		return tasks;
	}

}
