package com.activiti.controller;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.activiti.util.Page;
import com.activiti.util.PageUtil;
import com.activiti.util.UserUtil;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * �����е�ִ��ʵ��Execution
 * User: henryyan
 */
@Controller
@RequestMapping("/")
public class ExecutionController {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    /**
     * ��ѯ�������е�������,�û������������
     *
     * @return
     */
    @RequestMapping("joinedExecutionList")
    public ModelAndView list(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("joinedExecution");
    /* ��׼��ѯ
    List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().list();
    List<Execution> list = runtimeService.createExecutionQuery().list();
    mav.addObject("list", list);
    */

        User user = UserUtil.getUserFromSession(request.getSession());
        Page<Execution> page = new Page<Execution>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        NativeExecutionQuery nativeExecutionQuery = runtimeService.createNativeExecutionQuery();

        // native query
        String sql = "select RES.* from ACT_RU_EXECUTION RES left join ACT_HI_TASKINST ART on ART.PROC_INST_ID_ = RES.PROC_INST_ID_ "
                + " where ART.ASSIGNEE_ = #{userId} and ACT_ID_ is not null and IS_ACTIVE_ = 'TRUE' order by START_TIME_ desc";

        nativeExecutionQuery.parameter("userId", user.getId());

        List<Execution> executionList = nativeExecutionQuery.sql(sql).listPage(pageParams[0], pageParams[1]);

        // ��ѯ���̶������
        Map<String, ProcessDefinition> definitionMap = new HashMap<String, ProcessDefinition>();

        // �����Ӣ��-���Ķ���
        Map<String, Task> taskMap = new HashMap<String, Task>();

        // ÿ��Execution�ĵ�ǰ�ID������Ϊ���
        Map<String, List<String>> currentActivityMap = new HashMap<String, List<String>>();

        // ����ÿ��Execution����ĵ�ǰ��ڵ�
        for (Execution execution : executionList) {
            ExecutionEntity executionEntity = (ExecutionEntity) execution;
            String processInstanceId = executionEntity.getProcessInstanceId();
            String processDefinitionId = executionEntity.getProcessDefinitionId();

            // ����ProcessDefinition����Map����
            definitionCache(definitionMap, processDefinitionId);

            // ��ѯ��ǰ���̵����д��ڻ״̬�ĻID��������еĻ����ж��
            String sql2 = "SELECT * FROM ACT_RU_EXECUTION WHERE PROC_INST_ID_= #{processInstanceId} AND IS_ACTIVE_=TRUE";
            List<Execution> activeExecutionActivitys = runtimeService.createNativeExecutionQuery().sql(sql2).parameter("processInstanceId", processInstanceId).list();
            List<String> activeActivityIds = new ArrayList<String>();
            
            for (Execution activeExecutionActivity : activeExecutionActivitys) {
            	activeActivityIds.add(activeExecutionActivity.getId());
                // ��ѯ���ڻ״̬������
                Task task = taskService.createTaskQuery().taskDefinitionKey(activeExecutionActivity.getActivityId()).executionId(activeExecutionActivity.getId()).singleResult();

                // ���û
                if (task == null) {
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).singleResult();
                    if(null!=processInstance){
                    	task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
                        definitionCache(definitionMap, processInstance.getProcessDefinitionId());
                    }
                    
                }
                if(null!=task){
                	taskMap.put(activeExecutionActivity.getId(), task);
                }
            }
            currentActivityMap.put(execution.getId(), activeActivityIds);
        }

        mav.addObject("taskMap", taskMap);
        mav.addObject("definitions", definitionMap);
        mav.addObject("currentActivityMap", currentActivityMap);

        page.setResult(executionList);
        page.setTotalCount(nativeExecutionQuery.sql("select count(*) from (" + sql + ") a").count());
        mav.addObject("page", page);

        return mav;
    }
    
    /**
     * ��ѯ�ѽ�������ʵ��
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "finishedList")
    public ModelAndView finishedProcessInstanceList(HttpServletRequest request) {
        
    	ModelAndView mav = new ModelAndView("finishedProcess");
        Page<HistoricProcessInstance> page = new Page<HistoricProcessInstance>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery().finished();
        /*
         *select distinct RES.* , DEF.KEY_ as PROC_DEF_KEY_, DEF.NAME_ as PROC_DEF_NAME_, DEF.VERSION_ as PROC_DEF_VERSION_, DEF.DEPLOYMENT_ID_ as DEPLOYMENT_ID_ from ACT_HI_PROCINST RES 
         *left outer join ACT_RE_PROCDEF DEF on RES.PROC_DEF_ID_ = DEF.ID_ WHERE RES.END_TIME_ is not NULL order by RES.ID_ asc LIMIT ? OFFSET ? 
         */
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.listPage(pageParams[0], pageParams[1]);

        // ��ѯ���̶������
        Map<String, ProcessDefinition> definitionMap = new HashMap<String, ProcessDefinition>();

        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            definitionCache(definitionMap, historicProcessInstance.getProcessDefinitionId());
        }

        page.setResult(historicProcessInstances);
        page.setTotalCount(historicProcessInstanceQuery.count());
        mav.addObject("page", page);
        mav.addObject("definitions", definitionMap);

        return mav;
    }
    
    /**
     * ��ѯ��ʷ�����Ϣ
     *
     * @param processInstanceId
     * @return
     */
    @RequestMapping(value = "finishedView/{processInstanceId}")
    public ModelAndView historyDatas(@PathVariable("processInstanceId") String processInstanceId) {
        ModelAndView mav = new ModelAndView("viewFinishedProcess");

        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();

        // ��ѯ��ʷ����ʵ��
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        // ��ѯ�����йصı���
        List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

        List<HistoricDetail> formProperties = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).formProperties().list();

        // ��ѯ���̶������
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(historicProcessInstance.getProcessDefinitionId()).singleResult();

        mav.addObject("historicProcessInstance", historicProcessInstance);
        mav.addObject("variableInstances", variableInstances);
        mav.addObject("activities", activityInstances);
        mav.addObject("formProperties", formProperties);
        mav.addObject("processDefinition", processDefinition);

        return mav;
    }

    /**
     * ���̶�����󻺴�
     *
     * @param definitionMap
     * @param processDefinitionId
     */
    private void definitionCache(Map<String, ProcessDefinition> definitionMap, String processDefinitionId) {
        if (definitionMap.get(processDefinitionId) == null) {
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
            processDefinitionQuery.processDefinitionId(processDefinitionId);
            ProcessDefinition processDefinition = processDefinitionQuery.singleResult();

            // ���뻺��
            definitionMap.put(processDefinitionId, processDefinition);
        }
    }
}

