/*!
 * Copyright 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import javax.jcr.Node;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import java.security.Principal;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class PentahoACLProviderTest {

  private String rootPath = "/";
  private PentahoACLProvider provider;

  private SessionImpl systemSession;
  private Node rootNode;
  private ACLEditor editor;
  private PrincipalManager pMgr;
  private AccessControlManager acMgr;
  private Principal everyone;
  private ACLTemplate acList;
  private ACLTemplate.Entry aclEntry;
  private Privilege jcrReadAccessControlPriv;

  @Before
  public void setup() throws Exception {
    systemSession = Mockito.mock( SessionImpl.class );
    rootNode = Mockito.mock( NodeImpl.class );
    pMgr = Mockito.mock( PrincipalManager.class );
    editor = Mockito.mock( ACLEditor.class );
    acList = Mockito.mock( ACLTemplate.class );
    acMgr = Mockito.mock( AccessControlManager.class );
    everyone = Mockito.mock( Principal.class );
    aclEntry = Mockito.mock( ACLTemplate.Entry.class );
    jcrReadAccessControlPriv = Mockito.mock( Privilege.class );

    when( systemSession.getRootNode() ).thenReturn( rootNode );
    when( systemSession.getPrincipalManager() ).thenReturn( pMgr );
    when( systemSession.getAccessControlManager() ).thenReturn( acMgr );
    when( rootNode.getPath() ).thenReturn( rootPath );
    when( pMgr.getEveryone() ).thenReturn( everyone );
    when( acMgr.privilegeFromName( Privilege.JCR_READ_ACCESS_CONTROL ) ).thenReturn( jcrReadAccessControlPriv );

    final AccessControlPolicy[] acls = new AccessControlPolicy[]{acList};
    when( editor.getPolicies( rootPath ) ).thenReturn( acls );

    final AccessControlEntry[] acEntries = new AccessControlEntry[]{ aclEntry };
    when( acList.getAccessControlEntries() ).thenReturn( acEntries );

    provider = new PentahoACLProvider();
    Whitebox.setInternalState( provider, "session", systemSession );
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