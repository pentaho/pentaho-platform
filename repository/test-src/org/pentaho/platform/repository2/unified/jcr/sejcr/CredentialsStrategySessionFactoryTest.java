package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.apache.jackrabbit.api.XASession;
import org.junit.Test;

import javax.jcr.Repository;
import javax.jcr.Session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;

/**
 * Created by nbaker on 7/21/16.
 */
public class CredentialsStrategySessionFactoryTest {
  @Test
  public void createSessionProxy() throws Exception {
    CredentialsStrategySessionFactory factory =
        new CredentialsStrategySessionFactory( mock( Repository.class ), mock( CredentialsStrategy.class ) );
    Session sessionProxy = factory.createSessionProxy( mock( Session.class ) );
    assertThat( sessionProxy, allOf( instanceOf( XASession.class ), instanceOf( Session.class ) ) );
  }

}