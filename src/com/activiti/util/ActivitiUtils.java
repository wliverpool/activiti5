package com.activiti.util;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;

/**
 * Activiti������
 *
 * @author �⸣��
 */
public class ActivitiUtils {

    private static ProcessEngine processEngine;
    
    public static Map<String, String> ACTIVITY_TYPE = new HashMap<String, String>();

    static {
        ACTIVITY_TYPE.put("userTask", "�û�����");
        ACTIVITY_TYPE.put("serviceTask", "ϵͳ����");
        ACTIVITY_TYPE.put("startEvent", "��ʼ�ڵ�");
        ACTIVITY_TYPE.put("endEvent", "�����ڵ�");
        ACTIVITY_TYPE.put("exclusiveGateway", "�����жϽڵ�(ϵͳ�Զ�������������)");
        ACTIVITY_TYPE.put("inclusiveGateway", "���д�������");
        ACTIVITY_TYPE.put("callActivity", "���û");
        ACTIVITY_TYPE.put("subProcess", "������");
    }

    /**
     * ����ģʽ��ȡ�������
     */
    public static ProcessEngine getProcessEngine() {
        if (processEngine == null) {
      /*
       * ʹ��Ĭ�ϵ������ļ����ƣ�activiti.cfg.xml�������������
       */
            processEngine = ProcessEngines.getDefaultProcessEngine();
        }
        return processEngine;
    }
    


    /**
     * ����Ӣ�Ļ�ȡ��������
     *
     * @param type
     * @return
     */
    public static String getZhActivityType(String type) {
        return ACTIVITY_TYPE.get(type) == null ? type : ACTIVITY_TYPE.get(type);
    }

}