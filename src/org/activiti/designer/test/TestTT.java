package org.activiti.designer.test;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.junit.Test;

public class TestTT {
	
	@Test
	public void testIt(){
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService service = engine.getIdentityService();
		service.checkPassword("Mittermeyer", "Mittermeyer");
	}

}
