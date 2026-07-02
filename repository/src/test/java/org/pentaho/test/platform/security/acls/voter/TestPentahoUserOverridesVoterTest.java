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
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
import org.pentaho.platform.engine.security.acls.voter.PentahoUserOverridesVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@SuppressWarnings( "nls" )
public class TestPentahoUserOverridesVoterTest extends BaseTest {

  private static final String SOLUTION_PATH = "src/test/resources/solution";
  private static final String ALT_SOLUTION_PATH = "src/test/resources/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }

  }

  public void testVoter() throws Exception {
    SecurityHelper.getInstance().runAsUser( "suzy", new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        RepositoryFile testFile = new RepositoryFile( "Test Folder", null, null ); //$NON-NLS-1$
        Map<IPermissionRecipient, IPermissionMask> perms = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
        perms.put( new SimpleUser( "suzy" ), new SimplePermissionMask( IPentahoAclEntry.PERM_NOTHING ) );
        perms.put( new SimpleRole( "ROLE_POWER_USER" ), new SimplePermissionMask(
            IPentahoAclEntry.PERM_FULL_CONTROL ) );
        SpringSecurityPermissionMgr.instance().getPermissions( testFile );

        // Now, the stage is set. We should be able to double-check that suzy
        // has no access to the testFile.
        PentahoUserOverridesVoter voter = new PentahoUserOverridesVoter();
        assertNotNull( voter );
        assertFalse( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_EXECUTE ) );
        return null;
      }

    } );

  }
}
