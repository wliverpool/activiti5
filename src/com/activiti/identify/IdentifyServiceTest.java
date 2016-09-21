package com.activiti.identify;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * activiti用户管理测试
 * @author 吴福明
 *
 */

public class IdentifyServiceTest {

	@Rule
	//默认使用classpath中寻找activiti.cfg.xml,根据里面配置的springbean创建流程引擎
	public ActivitiRule activitiRule = new ActivitiRule();
	
	/**
	 * 测试用户管理
	 * @throws Exception
	 */
	@Test
	public void testUser()throws Exception{
		//获取用户和组的管理service
		IdentityService identityService = activitiRule.getIdentityService();
		//创建一个用户
		User user = identityService.newUser("reina");
		user.setFirstName("reina");
		user.setLastName("pepe");
		user.setEmail("reina@liverpool.com");
		//保存用户到数据库中
		identityService.saveUser(user);
		//测试创建用户是否成功
		User userInDb = identityService.createUserQuery().userId("reina").singleResult();
		assertNotNull(userInDb);
		//删除一个用户
		identityService.deleteUser("mittermeyer");
		//测试删除用户是否成功
		userInDb = identityService.createUserQuery().userId("mittermeyer").singleResult();
		assertNull(userInDb);
	}
	
	/**
	 * 测试组的管理
	 * @throws Exception
	 */
	@Test
	public void testGroup()throws Exception{
		IdentityService identityService = activitiRule.getIdentityService();
		Group group = identityService.newGroup("manager");
		group.setName("总经理");
		group.setType("assignment");
		identityService.saveGroup(group);
		List<Group> groupList = identityService.createGroupQuery().groupId("manager").list();
		assertEquals(1, groupList.size());
		identityService.deleteGroup("deptLeader");
		groupList = identityService.createGroupQuery().groupId("deptLeader").list();
		assertEquals(0, groupList.size());
	}
	
	/**
	 * 测试用户与组的管理
	 * @throws Exception
	 */
	@Test
	public void testUserAndGroupMembership()throws Exception{
		IdentityService identityService = activitiRule.getIdentityService();
		//保存用户与组的关系
		identityService.createMembership("carragher", "manager");
		//查找属于组的用户
		List<User> userInGroups = identityService.createUserQuery().memberOfGroup("manager").list();
		assertTrue(userInGroups.size()>0);
		for(User user:userInGroups){
			System.out.println("id:"+user.getId()+",name:"+user.getFirstName()+",email:"+user.getEmail());
		}
		//查找用户所在的组
		List<Group> groups = identityService.createGroupQuery().groupMember("carragher").list();
		assertTrue(groups.size()>0);
		for(Group group : groups){
			System.out.println("id:"+group.getId()+",name:"+group.getName()+",type:"+group.getType());
		}
		//删除用户与组的关系
		identityService.deleteMembership("carragher", "manager");
	}
	
}
