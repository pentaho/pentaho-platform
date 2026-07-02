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
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoAllowAllAclVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.File;
import java.util.concurrent.Callable;

@SuppressWarnings( "nls" )
public class TestPentahoAllowAllAclVoterTest extends BaseTest {

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
    junit.textui.TestRunner.run( TestPentahoAllowAllAclVoterTest.class );
    System.exit( 0 );
  }

  @SuppressWarnings( "deprecation" )
  public void testVoter() throws Exception {
    SecurityHelper.getInstance().runAsUser( "suzy", new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        RepositoryFile testFile = new RepositoryFile( "Test Folder", null, null ); //$NON-NLS-1$
        // RepositoryFile has no acls on it. Nobody should be able to access it.
        // But, we're using an allowAll voter.
        PentahoAllowAllAclVoter voter = new PentahoAllowAllAclVoter();
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_EXECUTE ) );
        IPentahoAclEntry entry = voter.getEffectiveAcl( PentahoSessionHolder.getSession(), testFile );
        assertEquals( ( (PentahoAclEntry) entry ).getMask(), IPentahoAclEntry.PERM_FULL_CONTROL );
        assertTrue( voter.isPentahoAdministrator( PentahoSessionHolder.getSession() ) );
        assertTrue( voter.isGranted( PentahoSessionHolder.getSession(), new SimpleGrantedAuthority(
            "ROLE_ANYTHING" ) ) ); //$NON-NLS-1$

        return null;
      }

    } );

  }
}
