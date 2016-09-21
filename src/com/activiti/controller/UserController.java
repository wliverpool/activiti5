package com.activiti.controller;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.activiti.util.UserUtil;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户相关控制器
 * 
 * @author 吴福明
 */
@Controller
@RequestMapping("/")
public class UserController extends AbstractController {

	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	//private IdentityService identityService = processEngine.getIdentityService();

	/**
	 * 登录系统
	 * 
	 * @param userName
	 * @param password
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "logon")
	public String logon(@RequestParam("username") String userName,@RequestParam("password") String password, HttpSession session) {
		logger.debug("logon request: {username={}, password={}}", userName,password);
		boolean checkPassword = identityService.checkPassword(userName,password);
		if (checkPassword) {

			// 查看用户是否存在
			User user = identityService.createUserQuery().userId(userName).singleResult();
			UserUtil.saveUserToSession(session, user);

			/*
			 * 读取角色
			 */
			List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();
			session.setAttribute("groups", groupList);

			String[] groupNames = new String[groupList.size()];
			for (int i = 0; i < groupNames.length; i++) {
				groupNames[i] = groupList.get(i).getName();
			}
			session.setAttribute("groupNames", ArrayUtils.toString(groupNames));

			return "redirect:/index.jsp";
		} else {
			return "redirect:/login.jsp?error=true";
		}
	}

	/**
	 * 退出登录
	 */
	@RequestMapping(value = "logout")
	public String logout(HttpSession session) {
		session.removeAttribute("user");
		return "/logon";
	}

}
