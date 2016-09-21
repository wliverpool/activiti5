package com.activiti.controller;

import java.util.List;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.pojo.RunningTask;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.activiti.dao.TaskQueryDao;

@Controller
@RequestMapping(value = "/")
public class MapperQueryController {

    @Autowired
    ManagementService managementService;

    @Autowired
    RepositoryService repositoryService;

    /**
     * 查询正在运行的任务
     *
     * @param processKey
     * @return
     */
    @RequestMapping(value = "taskRunning")
    public ModelAndView list(@RequestParam(value = "processKey", required = false) final String processKey) {
        ModelAndView mav = new ModelAndView("runningTasks");

        CustomSqlExecution<TaskQueryDao, List<RunningTask>> customSqlExecution =
                new AbstractCustomSqlExecution<TaskQueryDao, List<RunningTask>>(TaskQueryDao.class) {

                    public List<RunningTask> execute(TaskQueryDao customMapper) {

                        // 使用内置实体对象查询
                        // List<TaskEntity> taskByVariable = customMapper.findTasks("applyUserId");

                        List<RunningTask> tasks;
                        if (StringUtils.isBlank(processKey)) {
                            tasks = customMapper.selectRunningTasks();
                        } else {
                            tasks = customMapper.selectRunningTasksByProcessKey(processKey);
                        }
                        return tasks;
                    }
                };

        List<RunningTask> tasks = managementService.executeCustomSql(customSqlExecution);
        mav.addObject("tasks", tasks);

        // 读取引擎中所有的流程定义（只查询最新版本，目的在于获取流程定义的KEY和NAME）
        List<ProcessDefinition> processes = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        mav.addObject("processes", processes);



        return mav;
    }

}

