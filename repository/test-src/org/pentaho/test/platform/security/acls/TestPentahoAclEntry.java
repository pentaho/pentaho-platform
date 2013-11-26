/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2005 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.test.platform.security.acls;

import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import java.io.File;

@SuppressWarnings( "nls" )
public class TestPentahoAclEntry extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }

  }

  public static void main( String[] args ) {
    junit.textui.TestRunner.run( TestPentahoAclEntry.class );
    System.exit( 0 );
  }

  @SuppressWarnings( "deprecation" )
  public void testAcls() {
    PentahoAclEntry aclEntry = null;

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_NOTHING ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "------" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_EXECUTE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "X-----" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_SUBSCRIBE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "-S----" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_CREATE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "--C---" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_UPDATE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "---U--" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_DELETE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "----D-" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_UPDATE_PERMS ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "-----P" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_ADMINISTRATION ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "--CUDP" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_EXECUTE_SUBSCRIBE ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "XS----" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_ADMIN_ALL ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "XSCUD-" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_SUBSCRIBE_ADMINISTRATION ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "-SCUDP" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_EXECUTE_ADMINISTRATION ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "X-CUDP" ); //$NON-NLS-1$

    aclEntry = new PentahoAclEntry( "admin", IPentahoAclEntry.PERM_FULL_CONTROL ); //$NON-NLS-1$
    assertEquals( aclEntry.printPermissionsBlock(), "XSCUDP" ); //$NON-NLS-1$

    aclEntry.setRecipient( new GrantedAuthorityImpl( "ROLE_ADMIN" ) ); //$NON-NLS-1$
    Object recip = aclEntry.getRecipient();
    if ( !( recip instanceof GrantedAuthority ) ) {
      fail( "setRecipientString failed - GrantedAuthority." ); //$NON-NLS-1$
    }
    aclEntry.setRecipient( "suzy" ); //$NON-NLS-1$
    recip = aclEntry.getRecipient();
    if ( !( recip instanceof String ) ) {
      fail( "setRecipientString failed - User." ); //$NON-NLS-1$
    }

  }

}
