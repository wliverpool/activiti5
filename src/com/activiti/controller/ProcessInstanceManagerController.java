package com.activiti.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.activiti.util.Page;
import com.activiti.util.PageUtil;

@Controller
@RequestMapping(value = "/")
public class ProcessInstanceManagerController {
	
	@Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    /**
     * ����ʵ���б�
     */
    @RequestMapping(value = "processinstanceManagerList")
    public ModelAndView processInstanceList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("processinstanceManagerList");

        Page<ProcessInstance> page = new Page<ProcessInstance>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);

        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        List<ProcessInstance> processInstanceList = processInstanceQuery.listPage(pageParams[0], pageParams[1]);

        // �������̶���
        Map<String, ProcessDefinition> definitionMap = new HashMap<String, ProcessDefinition>();
        for (ProcessInstance processInstance : processInstanceList) {
            definitionCache(definitionMap, processInstance.getProcessDefinitionId());
        }
        page.setResult(processInstanceList);
        page.setTotalCount(processInstanceQuery.count());
        mav.addObject("page", page);
        mav.addObject("definitions", definitionMap);

        return mav;
    }
    
    /**
     * ��������ʵ����״̬
     *
     * @return
     */
    @RequestMapping(value = "processinstanceManager/{state}/{processInstanceId}")
    public String changeState(@PathVariable("state") String state,@PathVariable("processInstanceId") String processInstanceId) {
        if (StringUtils.equals("active", state)) {
            runtimeService.activateProcessInstanceById(processInstanceId);
        } else {
            runtimeService.suspendProcessInstanceById(processInstanceId);
        }

        return "redirect:/processinstanceManagerList";
    }
    
    /**
     * ɾ������ʵ��
     *
     * @return
     */
    @RequestMapping(value = "deleteProcessinstance/{processInstanceId}")
    @ResponseBody
    public boolean delete(@PathVariable("processInstanceId") String processInstanceId,@RequestParam("deleteReason") String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
        return true;
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
