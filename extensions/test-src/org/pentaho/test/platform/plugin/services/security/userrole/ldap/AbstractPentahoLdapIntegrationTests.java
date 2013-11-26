/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.security.userrole.ldap;

import org.apache.directory.server.core.DirectoryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.config.BeanIds;
import org.springframework.security.ldap.SpringSecurityContextSource;

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
    DirectoryService ds = DirectoryService.getInstance();
    ds.shutdown();
  }

  public SpringSecurityContextSource getContextSource() {
    return (SpringSecurityContextSource) appContext.getBean( BeanIds.CONTEXT_SOURCE );
  }

}
