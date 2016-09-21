package com.activiti.controller;

import org.activiti.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.util.ActivitiUtils;

/**
 * 抽象Controller，提供一些基础的方法、属性
 *
 * @author 吴福明
 */
public abstract class AbstractController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    //protected ProcessEngine processEngine = null;
    protected RepositoryService repositoryService;
    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected HistoryService historyService;
    protected IdentityService identityService;
    protected ManagementService managementService;
    protected FormService formService;

    public AbstractController() {
        super();
        //processEngine = ActivitiUtils.getProcessEngine();
        //repositoryService = processEngine.getRepositoryService();
        //runtimeService = processEngine.getRuntimeService();
        //taskService = processEngine.getTaskService();
        //historyService = processEngine.getHistoryService();
        //identityService = processEngine.getIdentityService();
        //managementService = processEngine.getManagementService();
        //formService = processEngine.getFormService();
    }

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	@Autowired
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
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

	public HistoryService getHistoryService() {
		return historyService;
	}

	@Autowired
	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}

	public IdentityService getIdentityService() {
		return identityService;
	}

	@Autowired
	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
	}

	public ManagementService getManagementService() {
		return managementService;
	}

	@Autowired
	public void setManagementService(ManagementService managementService) {
		this.managementService = managementService;
	}

	public FormService getFormService() {
		return formService;
	}

	@Autowired
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

}
