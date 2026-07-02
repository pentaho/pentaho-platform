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


package org.pentaho.test.platform.security.acls.voter;

import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoBasicAclVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@SuppressWarnings( "nls" )
public class TestBasicAclVoterTest extends BaseTest {
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
    junit.textui.TestRunner.run( TestBasicAclVoterTest.class );
    System.exit( 0 );
  }

  @SuppressWarnings( "deprecation" )
  public void testVoter() throws Exception {
    SecurityHelper.getInstance().runAsUser( "suzy", new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        RepositoryFile testFile = new RepositoryFile( "Test Folder", null, null ); //$NON-NLS-1$
        Map<IPermissionRecipient, IPermissionMask> perms = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
        perms.put( new SimpleUser( "suzy" ), new SimplePermissionMask( IPentahoAclEntry.PERM_EXECUTE ) );
        perms.put( new SimpleRole( "ROLE_POWER_USER" ), new SimplePermissionMask( IPentahoAclEntry.PERM_SUBSCRIBE ) );
        SpringSecurityPermissionMgr.instance().setPermissions( perms, testFile );
        PentahoBasicAclVoter voter =
            new PentahoBasicAclVoterForTesting( new MockAuthentication( "suzy", Arrays.asList( new GrantedAuthority[] {
              new SimpleGrantedAuthority( "ROLE_AUTHENTICATED" ), new SimpleGrantedAuthority(
                  "ROLE_POWER_USER" ) } ) ) );
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_EXECUTE ) );
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_SUBSCRIBE ) );
        assertFalse( voter.hasAccess( PentahoSessionHolder.getSession(), testFile,
            IPentahoAclEntry.PERM_ADMINISTRATION ) );
        PentahoAclEntry entry = voter.getEffectiveAcl( PentahoSessionHolder.getSession(), testFile );
        assertNotNull( entry );
        assertEquals( entry.printPermissionsBlock(), "XS----" ); //$NON-NLS-1$
        return null;
      }

    } );
  }

  private class PentahoBasicAclVoterForTesting extends PentahoBasicAclVoter {

    Authentication authentication;

    public PentahoBasicAclVoterForTesting( Authentication authentication ) {
      this.authentication = authentication;
    }

    @Override
    public Authentication getAuthentication( final IPentahoSession session ) {
      return authentication;
    }
  }

  /**
   * Mock class used for testing
   */
  private class MockAuthentication implements Authentication {

    private String currentUser;
    private Collection<? extends GrantedAuthority> authorities;

    public MockAuthentication( final String currentUser, Collection<? extends GrantedAuthority> authorities ) {
      this.currentUser = currentUser;
      this.authorities = authorities;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
      return authorities;
    }

    public Object getCredentials() {
      return null;
    }

    public Object getDetails() {
      return null;
    }

    public Object getPrincipal() {
      return currentUser;
    }

    public boolean isAuthenticated() {
      return true;
    }

    public void setAuthenticated( final boolean b ) throws IllegalArgumentException {
    }

    public String getName() {
      return currentUser;
    }
  }
}
