/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeImpl;
import org.apache.jackrabbit.core.security.authorization.AbstractAccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.PrivilegeManagerImpl;
import org.apache.jackrabbit.spi.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;

import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.when;

public class PentahoACLProviderTest {

  private String rootPath = "/";
  private PentahoACLProvider provider;

  private SessionImpl systemSession;
  private NodeImpl rootNode;
  private NodeImpl nodeMock;
  private ACLEditor editor;
  private PrincipalManager pMgr;
  private AccessControlManager acMgr;
  private Principal everyone;
  private ACLTemplate acList;
  private ACLTemplate.Entry aclEntry;
  private Privilege jcrReadAccessControlPriv;
  private JackrabbitWorkspace mockWorkspace;
  private PrivilegeManagerImpl mockPrivilegeManager;
  private NodeTypeImpl mockNodeTypeImpl;
  private Name mockName;

  @Before
  public void setup() throws Exception {
    systemSession = Mockito.mock( SessionImpl.class, RETURNS_MOCKS );
    rootNode = Mockito.mock( NodeImpl.class );
    pMgr = Mockito.mock( PrincipalManager.class );
    editor = Mockito.mock( ACLEditor.class );
    acList = Mockito.mock( ACLTemplate.class );
    acMgr = Mockito.mock( AccessControlManager.class );
    everyone = Mockito.mock( Principal.class );
    aclEntry = Mockito.mock( ACLTemplate.Entry.class );
    jcrReadAccessControlPriv = Mockito.mock( Privilege.class );
    nodeMock = Mockito.mock( NodeImpl.class, RETURNS_SMART_NULLS );
    mockWorkspace = Mockito.mock( JackrabbitWorkspace.class, RETURNS_MOCKS );
    mockPrivilegeManager = Mockito.mock( PrivilegeManagerImpl.class );
    mockNodeTypeImpl = Mockito.mock( NodeTypeImpl.class );
    mockName = Mockito.mock( Name.class );

    when( systemSession.getRootNode() ).thenReturn( rootNode );
    when( systemSession.getPrincipalManager() ).thenReturn( pMgr );
    when( systemSession.getAccessControlManager() ).thenReturn( acMgr );
    when( systemSession.getWorkspace() ).thenReturn( mockWorkspace );
    when( systemSession.getNode( AdditionalMatchers.or( anyString(), eq( null ) ) ) ).thenReturn( nodeMock );
    when( nodeMock.isNode() ).thenReturn( true );
    when( nodeMock.getPrimaryNodeType() ).thenReturn( mockNodeTypeImpl );
    when( mockNodeTypeImpl.getQName() ).thenReturn( mockName );
    when( mockWorkspace.getPrivilegeManager() ).thenReturn( mockPrivilegeManager );
    when( rootNode.getPath() ).thenReturn( rootPath );
    when( pMgr.getEveryone() ).thenReturn( everyone );
    when( acMgr.privilegeFromName( Privilege.JCR_READ_ACCESS_CONTROL ) ).thenReturn( jcrReadAccessControlPriv );

    final AccessControlPolicy[] acls = new AccessControlPolicy[]{acList};
    when( editor.getPolicies( rootPath ) ).thenReturn( acls );

    final AccessControlEntry[] acEntries = new AccessControlEntry[]{ aclEntry };
    when( acList.getAccessControlEntries() ).thenReturn( acEntries );

    provider = new PentahoACLProvider();
    Map configMap = new HashMap();
    configMap.put( AbstractAccessControlProvider.PARAM_OMIT_DEFAULT_PERMISSIONS, null );
    provider.init( systemSession, configMap );
  }

  @Test
  public void testRequireRootAclUpdate() throws Exception {

    // everyone principle does not exist, update should be required
    assertTrue( provider.requireRootAclUpdate( editor ) );

    // add everyone principle
    when( aclEntry.getPrincipal() ).thenReturn( everyone );
    // everyone principle exists, but the JCR_READ_ACCESS_CONTROL privilege is not yet added, update should be required
    assertTrue( provider.requireRootAclUpdate( editor ) );

    // add the JCR_READ_ACCESS_CONTROL privilege
    Privilege[] privs = new Privilege[] { jcrReadAccessControlPriv };
    when( aclEntry.getPrivileges() ).thenReturn( privs );
    // everyone principle exists and it had the the JCR_READ_ACCESS_CONTROL privilege, update should not be required
    assertFalse( provider.requireRootAclUpdate( editor ) );
  }
}