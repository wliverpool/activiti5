package com.activiti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.pojo.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.dao.LeaveDao;

@Service
@Transactional
public class LeaveService {
	
	private LeaveDao leaveDao;
    private IdentityService identityService;
    private RuntimeService runtimeService;
	private TaskService taskService;
    private RepositoryService repositoryService;

	public LeaveDao getLeaveDao() {
		return leaveDao;
	}

	@Autowired
	public void setLeaveDao(LeaveDao leaveDao) {
		this.leaveDao = leaveDao;
	}
	
	public IdentityService getIdentityService() {
		return identityService;
	}

	@Autowired
	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
	}

	public RuntimeService getRuntimeService() {
		return runtimeService;
	}

	@Autowired
	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	@Autowired
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}
	
	public ProcessInstance startLeaveFlow(Leave leave,String userId,Map<String, Object> variables){
		if(null==leave.getId()){
			leave.setApplyTime(new Date());
			leave.setUserId(userId);
		}
		long key = leaveDao.save(leave);
		leave.setId(key);
		String businessKey = String.valueOf(key);
		identityService.setAuthenticatedUserId(userId);
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("leave-3",businessKey,variables);
		String processInstanceId = instance.getId();
		leave.setProcessInstanceId(processInstanceId);
		leaveDao.updateLeave(leave);
		return instance;
	}
	
	@Transactional(readOnly=true)
	public List<Leave> findTodoTasks(String userId){
		List<Leave> results = new ArrayList<Leave>();
		
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("leave-3").taskCandidateOrAssigned(userId).list();
		for(Task task : tasks){
			String processInstanceId= task.getProcessInstanceId();
			ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			String businessKey = instance.getBusinessKey();
			Leave leave = leaveDao.getById(new Long(businessKey));
			leave.setTask(task);
			leave.setProcessInstance(instance);
			String prcessDefinitionId = instance.getProcessDefinitionId();
			ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId(prcessDefinitionId).singleResult();
			leave.setProcessDefinition(definition);
			results.add(leave);
		}
		return results;
	}
	
	public void claimTask(String taskId,String userId){
		taskService.claim(taskId, userId);
	}
	
	@Transactional(readOnly=true)
	public Leave getLeaveTaskDetail(String taskId){
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        Leave leave = leaveDao.getById(new Long(processInstance.getBusinessKey()));
        leave.setTask(task);
        return leave;
	}
	
	public void completeAuditTask(String taskId,Map<String, Object> variables){
		taskService.complete(taskId,variables);
	}
	
	public void completeNormalTask(Leave leave,String taskId,Map<String, Object> variables){
		leaveDao.updateLeave(leave);
		taskService.complete(taskId,variables);
	}

}
