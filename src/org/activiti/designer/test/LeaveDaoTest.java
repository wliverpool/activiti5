package org.activiti.designer.test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.activiti.pojo.Leave;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.activiti.dao.LeaveDao;

public class LeaveDaoTest {
	
	private LeaveDao leaveDao = null;
	
	@Before
	public void setUp(){
		ApplicationContext context =new ClassPathXmlApplicationContext("applicationContext.xml");
		leaveDao = context.getBean(LeaveDao.class);
	}
	
	@Test
	public void testInsert(){
		Leave leave = new Leave();
		leave.setApplyTime(new Date());
		Calendar d = Calendar.getInstance();
		leave.setStartTime(d.getTime());
		leave.setRealityStartTime(d.getTime());
		d.add(Calendar.DAY_OF_MONTH, 1);
		leave.setEndTime(d.getTime());
		leave.setRealityEndTime(d.getTime());
		leave.setLeaveType("tst");
		leave.setProcessInstanceId("processIds1");
		leave.setReason("²âÊÔÔ­Òò");
		leave.setUserId("gerrard");
		long key = leaveDao.save(leave);
		System.out.println(key);
		assertTrue(key>0);
	}
	
	@Test
	public void testDeleteById(){
		int rows = leaveDao.deleteById(2L);
		System.out.println(rows);
		assertTrue(rows>0);
	}
	
	@Test
	public void testDeleteByProcessInstanceId(){
		int rows = leaveDao.deleteByProcessInstanceId("processIds1");
		System.out.println(rows);
		assertTrue(rows>0);
	}
	
	@Test
	public void testGetById(){
		Leave leave = leaveDao.getById(4L);
		assertNotNull(leave.getProcessInstanceId());
	}
	
	@Test
	public void testGetByProcessInstanceId(){
		Leave leave = leaveDao.getByProcessInstanceId("processIds1");
		assertNotNull(leave.getReason());
	}
	
	@Test
	public void testUpdate(){
		Leave leave = new Leave();
		leave.setId(4L);
		leave.setLeaveType("1111");
		leaveDao.updateLeave(leave);
	}

}
