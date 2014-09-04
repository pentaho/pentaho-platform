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
  public void testHeaderInjection() {
    emailer.setTo( "test@test.ru\r This is header injection" );
    emailer.setCc( "test@test.ru\r This is header injection" );
    emailer.setBcc( "test@test.ru\r This is header injection" );

    Assert.assertEquals( "test@test.ru This is header injection", emailer.getTo() );
    Assert.assertEquals( "test@test.ru This is header injection", emailer.getCc() );
    Assert.assertEquals( "test@test.ru This is header injection", emailer.getBcc() );
  }

}
