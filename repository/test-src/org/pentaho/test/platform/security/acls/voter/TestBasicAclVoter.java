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
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoBasicAclVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@SuppressWarnings( "nls" )
public class TestBasicAclVoter extends BaseTest {
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
    junit.textui.TestRunner.run( TestBasicAclVoter.class );
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
        PentahoBasicAclVoter voter = new PentahoBasicAclVoter();
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_EXECUTE ) );
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile, IPentahoAclEntry.PERM_SUBSCRIBE ) );
        assertTrue( voter.hasAccess( PentahoSessionHolder.getSession(), testFile,
          IPentahoAclEntry.PERM_ADMINISTRATION ) );
        PentahoAclEntry entry = voter.getEffectiveAcl( PentahoSessionHolder.getSession(), testFile );
        assertNotNull( entry );
        assertEquals( entry.printPermissionsBlock(), "XSCUDP" ); //$NON-NLS-1$    
        return null;
      }

    } );

  }

}
