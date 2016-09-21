package com.activiti.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.activiti.util.Page;
import com.activiti.util.PageUtil;

@Controller
@RequestMapping("/")
public class IdentityController {

    @Autowired
    IdentityService identityService;


    /**
     * ���б�
     *
     * @param request
     * @return
     */
    @RequestMapping("identityGroupList")
    public ModelAndView groupList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("groupList");

        Page<Group> page = new Page<Group>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);

        GroupQuery groupQuery = identityService.createGroupQuery();
        List<Group> groupList = groupQuery.listPage(pageParams[0], pageParams[1]);

        page.setResult(groupList);
        page.setTotalCount(groupQuery.count());
        mav.addObject("page", page);

        return mav;
    }

    /**
     * ����Group
     *
     * @return
     */
    @RequestMapping(value = "identityGroupSave", method = RequestMethod.POST)
    public String saveGroup(@RequestParam("groupId") String groupId,
                            @RequestParam("groupName") String groupName,
                            @RequestParam("type") String type,
                            RedirectAttributes redirectAttributes) {
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (group == null) {
            group = identityService.newGroup(groupId);
        }
        group.setName(groupName);
        group.setType(type);
        identityService.saveGroup(group);
        redirectAttributes.addFlashAttribute("message", "�ɹ�������[" + groupName + "]");
        return "redirect:/identityGroupList";
    }

    /**
     * ɾ��Group
     */
    @RequestMapping(value = "identityGroupDelete/{groupId}", method = RequestMethod.GET)
    public String deleteGroup(@PathVariable("groupId") String groupId,
                              RedirectAttributes redirectAttributes) {
        identityService.deleteGroup(groupId);
        redirectAttributes.addFlashAttribute("message", "�ɹ�ɾ����[" + groupId + "]");
        return "redirect:/identityGroupList";
    }

    /**
     * �û��б�
     *
     * @param request
     * @return
     */
    @RequestMapping("identityUserList")
    public ModelAndView userList(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("userList");

        Page<User> page = new Page<User>(PageUtil.PAGE_SIZE);
        int[] pageParams = PageUtil.init(page, request);

        UserQuery userQuery = identityService.createUserQuery();
        List<User> userList = userQuery.listPage(pageParams[0], pageParams[1]);

        // ��ѯÿ���˵ķ��飬������д���ȽϺķ����ܡ�ʱ�䣬�������߲ο�
        Map<String, List<Group>> groupOfUserMap = new HashMap<String, List<Group>>();
        for (User user : userList) {
            List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();
            groupOfUserMap.put(user.getId(), groupList);
        }

        page.setResult(userList);
        page.setTotalCount(userQuery.count());
        mav.addObject("page", page);
        mav.addObject("groupOfUserMap", groupOfUserMap);

        // ��ȡ������
        List<Group> groups = identityService.createGroupQuery().list();
        mav.addObject("allGroup", groups);

        return mav;
    }

    /**
     * ����User
     *
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(value = "identityUserSave", method = RequestMethod.POST)
    public String saveUser(@RequestParam("userId") String userId,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "email", required = false) String email,
                           RedirectAttributes redirectAttributes) {
        User user = identityService.createUserQuery().userId(userId).singleResult();
        if (user == null) {
            user = identityService.newUser(userId);
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
        }
        identityService.saveUser(user);
        redirectAttributes.addFlashAttribute("message", "�ɹ������û�[" + firstName + " " + lastName + "]");
        return "redirect:/identityUserList";
    }

    /**
     * ɾ��User
     */
    @RequestMapping(value = "identityUserDelete/{userId}", method = RequestMethod.GET)
    public String deleteUser(@PathVariable("userId") String userId,
                             RedirectAttributes redirectAttributes) {
        identityService.deleteUser(userId);
        redirectAttributes.addFlashAttribute("message", "�ɹ�ɾ���û�[" + userId + "]");
        return "redirect:/identityUserList";
    }

    /**
     * Ϊ�û�����������
     * @param userId
     * @param groupIds
     * @return
     */
    @RequestMapping(value = "identityGroupSet", method = RequestMethod.POST)
    public String groupForUser(@RequestParam("userId") String userId, @RequestParam("group") String[] groupIds) {
        List<Group> groupInDb = identityService.createGroupQuery().groupMember(userId).list();
        for (Group group : groupInDb) {
            identityService.deleteMembership(userId, group.getId());
        }
        for (String group : groupIds) {
            identityService.createMembership(userId, group);
        }
        return "redirect:/identityUserList";
    }

}
