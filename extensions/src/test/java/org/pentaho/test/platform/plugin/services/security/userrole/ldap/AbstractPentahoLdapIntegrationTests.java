/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.config.BeanIds;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;

import javax.naming.NamingException;

/**
 * Based on <code>org.springframework.security.ldap.AbstractLdapIntegrationTests</code>.
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public abstract class AbstractPentahoLdapIntegrationTests {
  private static ClassPathXmlApplicationContext appContext;
  protected static final String ROOT_DN = "dc=pentaho,dc=org"; //$NON-NLS-1$

  protected AbstractPentahoLdapIntegrationTests() {
  }

  @BeforeClass
  public static void loadContext() throws NamingException {
    shutdownRunningServers();
    appContext = new ClassPathXmlApplicationContext( "/ldapIntegrationTestContext.xml" ); //$NON-NLS-1$
  }

  @AfterClass
  public static void closeContext() throws Exception {
    if ( appContext != null ) {
      appContext.close();
    }
    shutdownRunningServers();

    System.out.println( "Waiting for 1 minute, 5 seconds while ApacheDS closes its port..." );
    System.out.println( "Do a 'netstat | grep 53389' if you don't believe me. Connections should be in TIME_WAIT." );

    /*
     * For some reason, on Linux at least, the embedded directory server keeps its port open for about a minute after we
     * request that it shutdown.
     */
    Thread.sleep( 65000 );
  }

  private static void shutdownRunningServers() throws NamingException {
    try {
      DirectoryService ds = new DefaultDirectoryService();
      ds.shutdown();
    } catch ( Exception e ) {
      throw new NamingException( e.getMessage() );
    }
  }

  public BaseLdapPathContextSource getContextSource() {
    return (BaseLdapPathContextSource) appContext.getBean( BeanIds.CONTEXT_SOURCE );
  }

}
