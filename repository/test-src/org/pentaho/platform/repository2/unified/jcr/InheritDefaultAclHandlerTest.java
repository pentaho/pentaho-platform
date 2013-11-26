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

package org.pentaho.platform.repository2.unified.jcr;

/*
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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * User: pminutillo
 * Date: 2/13/13
 * Time: 12:58 PM
 */

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InheritDefaultAclHandlerTest {

  InheritDefaultAclHandler inheritDefaultAclHandler;
  RepositoryFile repositoryFile;

  @Before
  public void setUp() {
    StandaloneSession pentahoSession = new StandaloneSession( "test", "test" );
    PentahoSessionHolder.setSession( pentahoSession );
    repositoryFile = mock( RepositoryFile.class );

    inheritDefaultAclHandler = new InheritDefaultAclHandler();
  }

  @Test
  public void testCreateDefaultAcl() {
    RepositoryFileAcl repositoryFileAcl = inheritDefaultAclHandler.createDefaultAcl( repositoryFile );
    assertTrue( repositoryFileAcl.isEntriesInheriting() );
  }

  @Test
  public void testSharedObjectsCreateDefaultAcl() {
    // default behavior is to inherit everything but database connections
    SharedObjectsDefaultAclHandler aclHandler = new SharedObjectsDefaultAclHandler();
    RepositoryFile f = mock( RepositoryFile.class );
    when( f.getPath() ).thenReturn( "/pentaho/tenant0/etc/pdi/databases/Test.kdb" );
    RepositoryFileAcl acl = aclHandler.createDefaultAcl( f );
    assertTrue( !acl.isEntriesInheriting() );
    assertTrue( acl.getAces().size() == 1 );
    assertTrue( "Authenticated".equals( acl.getAces().get( 0 ).getSid().getName().toString() ) );
    assertTrue( RepositoryFileSid.Type.ROLE.equals( acl.getAces().get( 0 ).getSid().getType() ) );
    assertTrue( acl.getAces().get( 0 ).getPermissions().size() == 1 );
    assertTrue( acl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.ALL ) );

    acl = aclHandler.createDefaultAcl( repositoryFile );
    assertTrue( acl.isEntriesInheriting() );

    // null role tells the acl handler to default to no aces
    List<String> paths = new ArrayList<String>();
    paths.add( "{0}/etc/pdi/databases" );
    aclHandler = new SharedObjectsDefaultAclHandler( null, paths );

    acl = aclHandler.createDefaultAcl( f );
    assertTrue( !acl.isEntriesInheriting() );
    System.out.println( acl.getAces() );
    assertTrue( acl.getAces().size() == 0 );

    acl = aclHandler.createDefaultAcl( repositoryFile );
    assertTrue( acl.isEntriesInheriting() );
  }
}
