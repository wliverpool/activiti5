package com.activiti.util;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

/**
 * Activiti工具类
 *
 * @author 吴福明
 */
public class ActivitiUtils {

    private static ProcessEngine processEngine;
    
    public static Map<String, String> ACTIVITY_TYPE = new HashMap<String, String>();

    static {
        ACTIVITY_TYPE.put("userTask", "用户任务");
        ACTIVITY_TYPE.put("serviceTask", "系统任务");
        ACTIVITY_TYPE.put("startEvent", "开始节点");
        ACTIVITY_TYPE.put("endEvent", "结束节点");
        ACTIVITY_TYPE.put("exclusiveGateway", "条件判断节点(系统自动根据条件处理)");
        ACTIVITY_TYPE.put("inclusiveGateway", "并行处理任务");
        ACTIVITY_TYPE.put("callActivity", "调用活动");
        ACTIVITY_TYPE.put("subProcess", "子流程");
    }

    /**
     * 单例模式获取引擎对象
     */
    public static ProcessEngine getProcessEngine() {
        if (processEngine == null) {
      /*
       * 使用默认的配置文件名称（activiti.cfg.xml）创建引擎对象
       */
            processEngine = ProcessEngines.getDefaultProcessEngine();
        }
        return processEngine;
    }
    


    /**
     * 根据英文获取中文类型
     *
     * @param type
     * @return
     */
    public static String getZhActivityType(String type) {
        return ACTIVITY_TYPE.get(type) == null ? type : ACTIVITY_TYPE.get(type);
    }

}