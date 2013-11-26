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

import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
import org.pentaho.platform.engine.security.acls.AclPublisher;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( "nls" )
public class TestAclPublisher extends BaseTest {

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
    junit.textui.TestRunner.run( TestAclPublisher.class );
    System.exit( 0 );
  }

  private Map<IPermissionRecipient, IPermissionMask> defaultAcls =
      new LinkedHashMap<IPermissionRecipient, IPermissionMask>();

  public void setup() {
    super.setUp();
    defaultAcls.put(
        new SimpleRole( "ROLE_ADMINISTRATOR" ), new SimplePermissionMask( IPentahoAclEntry.PERM_FULL_CONTROL ) ); //$NON-NLS-1$
    defaultAcls
        .put( new SimpleRole( "ROLE_POWER_USER" ), new SimplePermissionMask( IPentahoAclEntry.PERM_FULL_CONTROL ) ); //$NON-NLS-1$
    defaultAcls.put( new SimpleRole( "ROLE_AUTHENTICATED" ), new SimplePermissionMask( IPentahoAclEntry.PERM_EXECUTE ) ); //$NON-NLS-1$
  }

  public void testPublisher() {
    AclPublisher publisher = new AclPublisher( defaultAcls );
    assertNotNull( publisher );

    RepositoryFile rootFile = getPopulatedSolution();
    publisher.publishDefaultAcls( rootFile );
    checkAcls( rootFile );
  }

  public static RepositoryFile getPopulatedSolution() {
    RepositoryFile root = new RepositoryFile( "root", null, null ); //$NON-NLS-1$
    final int topFolderCount = 3;
    final int subFolderCount = 3;
    final int filesPerFolder = 4;
    final byte[] fileData = "This is file data".getBytes(); //$NON-NLS-1$
    for ( int i = 0; i < topFolderCount; i++ ) {
      RepositoryFile topFolder = new RepositoryFile( "topFolder" + i, root, null ); //$NON-NLS-1$
      for ( int j = 0; j < subFolderCount; j++ ) {
        RepositoryFile subFolder = new RepositoryFile( "subFolder" + j, topFolder, null ); //$NON-NLS-1$
        for ( int k = 0; k < filesPerFolder; k++ ) {
          RepositoryFile aFile = new RepositoryFile( "aFile" + k, subFolder, fileData ); //$NON-NLS-1$
        }
      }
    }
    return root;
  }

  public void checkAcls( IAclSolutionFile solnFile ) {
    if ( solnFile.isDirectory() ) {
      Map<IPermissionRecipient, IPermissionMask> perms =
          SpringSecurityPermissionMgr.instance().getPermissions( solnFile );
      assertEquals( perms.size(), defaultAcls.size() );
      assertTrue( perms.entrySet().containsAll( defaultAcls.entrySet() ) );
      Set kidsSet = solnFile.getChildrenFiles();
      Iterator it = kidsSet.iterator();
      while ( it.hasNext() ) {
        IAclSolutionFile kidFile = (IAclSolutionFile) it.next();
        if ( kidFile.isDirectory() ) {
          checkAcls( kidFile );
        }
      }
    }
  }

}
