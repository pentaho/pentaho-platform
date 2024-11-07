/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.test.platform.security.acls;

import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.File;

@SuppressWarnings( "nls" )
public class TestPentahoAclEntryTest extends BaseTest {
  private static final String SOLUTION_PATH = "src/test/resources/solution";
  private static final String ALT_SOLUTION_PATH = "src/test/resources/solution";
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
    junit.textui.TestRunner.run( TestPentahoAclEntryTest.class );
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

    aclEntry.setRecipient( new SimpleGrantedAuthority( "ROLE_ADMIN" ) ); //$NON-NLS-1$
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
