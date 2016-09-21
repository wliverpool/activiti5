package com.activiti.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.activiti.engine.task.Task;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ActivitiQueryTest {
	
	private QueryDao queryDao;
	
	@Before
	public void setUp(){
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		queryDao = context.getBean(QueryDao.class);
	}
	
	@Test
	public void testNativeQuery(){
		List<Task> tasks = queryDao.queryTaskListByNativeQuery();
		assertTrue(tasks.size()>0);
		for(Task task : tasks){
			System.out.println(task.getId()+":"+task.getName());
		}
	}
	
	@Test
	public void testQueryTaskByPage(){
		List<Task> tasks = queryDao.queryTaskListByPage();
		assertTrue(tasks.size()>0);
		for(Task task : tasks){
			System.out.println(task.getId()+":"+task.getName());
		}
	}

}
