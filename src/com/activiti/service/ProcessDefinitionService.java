package com.activiti.service;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ProcessDefinitionService {
	
	@Autowired
    RepositoryService repositoryService;
	
	/**
     * ���ú�ѡ�����ˡ���
     * @param processDefinitionId
     * @param userArray
     * @param groupArray
     */
    public void setStartables(String processDefinitionId, String[] userArray, String[] groupArray) {

        // 1���������е�����
        List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinitionId);
        for (IdentityLink link : links) {
            if (StringUtils.isNotBlank(link.getUserId())) {
                repositoryService.deleteCandidateStarterUser(processDefinitionId, link.getUserId());
            }
            if (StringUtils.isNotBlank(link.getGroupId())) {
                repositoryService.deleteCandidateStarterGroup(processDefinitionId, link.getGroupId());
            }
        }

        // 2.1��ѭ����Ӻ�ѡ��
        if (!ArrayUtils.isEmpty(userArray)) {
            for (String user : userArray) {
                repositoryService.addCandidateStarterUser(processDefinitionId, user);
            }
        }

        // 2.2��ѭ����Ӻ�ѡ��
        if (!ArrayUtils.isEmpty(groupArray)) {
            for (String group : groupArray) {
                repositoryService.addCandidateStarterGroup(processDefinitionId, group);
            }
        }
    }

}
