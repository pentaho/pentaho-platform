package org.pentaho.platform.scheduler2.email;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class EmailerTest {

  private Emailer emailer;

  @Before
  public void setUp() {
    emailer = new Emailer();
  }

  @Test
  public void testSubjectInjection() {
    emailer.setSubject( "Test\r\nCC: test@test.com" );
    Assert.assertEquals( "TestCC: test@test.com", emailer.getSubject() );
  }

}
