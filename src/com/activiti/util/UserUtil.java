package com.activiti.util;

import org.activiti.engine.identity.User;

import javax.servlet.http.HttpSession;

/**
 * �û�������
 *
 * @author �⸣��
 */
public class UserUtil {

    public static final String USER = "user";

    /**
     * �����û���session
     *
     * @param session
     * @param user
     */
    public static void saveUserToSession(HttpSession session, User user) {
        session.setAttribute(USER, user);
    }

    /**
     * ��Session��ȡ��ǰ�û���Ϣ
     *
     * @param session
     * @return
     */
    public static User getUserFromSession(HttpSession session) {
        Object attribute = session.getAttribute(USER);
        return attribute == null ? null : (User) attribute;
    }

}
