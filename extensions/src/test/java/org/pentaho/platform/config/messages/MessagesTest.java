package org.pentaho.platform.config.messages;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/20/15.
 */
public class MessagesTest {

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( Messages.getInstance() );
  }

}
