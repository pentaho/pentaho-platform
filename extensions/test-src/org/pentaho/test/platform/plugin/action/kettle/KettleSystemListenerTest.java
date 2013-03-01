package org.pentaho.test.platform.plugin.action.kettle;

import org.apache.log4j.FileAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

public class KettleSystemListenerTest {
	private IApplicationContext mockApplicationContext;
	private FileAppender fileAppender = new FileAppender();
	
	@Before
	public void setup() {
		mockApplicationContext = mock(IApplicationContext.class);
		org.apache.log4j.Logger.getRootLogger().addAppender(fileAppender);
		PentahoSystem.setApplicationContext(mockApplicationContext);
	}
	
	@After
	public void teardown() {
		org.apache.log4j.Logger.getRootLogger().removeAppender(fileAppender);
	}
	
	@Test
	public void testStartup() {
		KettleSystemListener ksl = new KettleSystemListener();
		assertTrue(ksl.startup(null));
	}
}
