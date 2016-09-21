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
 * activiti�û��������
 * @author �⸣��
 *
 */

public class IdentifyServiceTest {

	@Rule
	//Ĭ��ʹ��classpath��Ѱ��activiti.cfg.xml,�����������õ�springbean������������
	public ActivitiRule activitiRule = new ActivitiRule();
	
	/**
	 * �����û�����
	 * @throws Exception
	 */
	@Test
	public void testUser()throws Exception{
		//��ȡ�û�����Ĺ���service
		IdentityService identityService = activitiRule.getIdentityService();
		//����һ���û�
		User user = identityService.newUser("reina");
		user.setFirstName("reina");
		user.setLastName("pepe");
		user.setEmail("reina@liverpool.com");
		//�����û������ݿ���
		identityService.saveUser(user);
		//���Դ����û��Ƿ�ɹ�
		User userInDb = identityService.createUserQuery().userId("reina").singleResult();
		assertNotNull(userInDb);
		//ɾ��һ���û�
		identityService.deleteUser("mittermeyer");
		//����ɾ���û��Ƿ�ɹ�
		userInDb = identityService.createUserQuery().userId("mittermeyer").singleResult();
		assertNull(userInDb);
	}
	
	/**
	 * ������Ĺ���
	 * @throws Exception
	 */
	@Test
	public void testGroup()throws Exception{
		IdentityService identityService = activitiRule.getIdentityService();
		Group group = identityService.newGroup("manager");
		group.setName("�ܾ���");
		group.setType("assignment");
		identityService.saveGroup(group);
		List<Group> groupList = identityService.createGroupQuery().groupId("manager").list();
		assertEquals(1, groupList.size());
		identityService.deleteGroup("deptLeader");
		groupList = identityService.createGroupQuery().groupId("deptLeader").list();
		assertEquals(0, groupList.size());
	}
	
	/**
	 * �����û�����Ĺ���
	 * @throws Exception
	 */
	@Test
	public void testUserAndGroupMembership()throws Exception{
		IdentityService identityService = activitiRule.getIdentityService();
		//�����û�����Ĺ�ϵ
		identityService.createMembership("carragher", "manager");
		//������������û�
		List<User> userInGroups = identityService.createUserQuery().memberOfGroup("manager").list();
		assertTrue(userInGroups.size()>0);
		for(User user:userInGroups){
			System.out.println("id:"+user.getId()+",name:"+user.getFirstName()+",email:"+user.getEmail());
		}
		//�����û����ڵ���
		List<Group> groups = identityService.createGroupQuery().groupMember("carragher").list();
		assertTrue(groups.size()>0);
		for(Group group : groups){
			System.out.println("id:"+group.getId()+",name:"+group.getName()+",type:"+group.getType());
		}
		//ɾ���û�����Ĺ�ϵ
		identityService.deleteMembership("carragher", "manager");
	}
	
}
