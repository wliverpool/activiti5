package com.activiti.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ManagementService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
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
public class JobController {

	@Autowired
	ManagementService managementService;

	public static Map<String, String> JOB_TYPES = new HashMap<String, String>();

	static {
		JOB_TYPES.put("activate-processdefinition", "�������̶���");
		JOB_TYPES.put("timer-intermediate-transition", "�м䶨ʱ");
		JOB_TYPES.put("timer-transition", "�߽綨ʱ");
		JOB_TYPES.put("timer-start-event", "��ʱ��������");
		JOB_TYPES.put("suspend-processdefinition", "�������̶���");
		JOB_TYPES.put("async-continuation", "�첽��");
	}
	
	/**
     * Job�б�
     *
     * @return
     */
    @RequestMapping(value = "jobList")
    public ModelAndView jobList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("jobList");
        JobQuery jobQuery = managementService.createJobQuery();

        Page<Job> page = new Page<Job>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);
        List<Job> jobList = jobQuery.listPage(pageParams[0], pageParams[1]);

        page.setResult(jobList);
        page.setTotalCount(jobQuery.count());

        Map<String, String> exceptionStacktraces = new HashMap<String, String>();
        for (Job job : jobList) {
            if (StringUtils.isNotBlank(job.getExceptionMessage())) {
                exceptionStacktraces.put(job.getId(), managementService.getJobExceptionStacktrace(job.getId()));
            }
        }

        mav.addObject("page", page);
        mav.addObject("exceptionStacktraces", exceptionStacktraces);
        mav.addObject("JOB_TYPES", JOB_TYPES);

        return mav;
    }
    
    /**
     * ɾ����ҵ
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "jobDelete/{jobId}")
    public String deleteJob(@PathVariable("jobId") String jobId) {
        managementService.deleteJob(jobId);
        return "redirect:/jobList";
    }
    
    /**
     * ִ����ҵ
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "executeJob/{jobId}")
    public String executeJob(@PathVariable("jobId") String jobId) {
        managementService.executeJob(jobId);
        return "redirect:/jobList";
    }
    
    /**
     * ������ҵ�����Դ���
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "changeJobRetries/{jobId}")
    public String changeRetries(@PathVariable("jobId") String jobId, @RequestParam("retries") int retries) {
        managementService.setJobRetries(jobId, retries);
        return "redirect:/jobList";
    }

    /**
     * ��ȡ��ҵ�쳣��Ϣ
     *
     * @param jobId
     * @return
     */
    @RequestMapping(value = "jobStacktrace/{jobId}")
    @ResponseBody
    public String getJobExceptionStacktrace(@PathVariable("jobId") String jobId) {
        return managementService.getJobExceptionStacktrace(jobId);
    }

}
