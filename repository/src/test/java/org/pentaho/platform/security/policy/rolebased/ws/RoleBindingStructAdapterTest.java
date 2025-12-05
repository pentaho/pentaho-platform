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


package org.pentaho.platform.security.policy.rolebased.ws;

import org.junit.Test;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

//@SuppressWarnings( "nls" )
public class RoleBindingStructAdapterTest {

  @Test
  public void testMarshalUnmarshal() throws Exception {
    final HashMap<String, String> logicalRoleNameMap = new HashMap<String, String>();
    logicalRoleNameMap.put( RepositoryCreateAction.NAME, "Create Content" );
    final HashMap<String, List<String>> bindingMap = new HashMap<String, List<String>>();
    final LinkedList<String> roles = new LinkedList<String>();
    roles.add( RepositoryCreateAction.NAME );
    bindingMap.put( "admin", roles );
    final HashSet<String> immutableRoles = new HashSet<String>();
    immutableRoles.add( "admin" );
    final RoleBindingStruct roleBindingStruct = new RoleBindingStruct( logicalRoleNameMap, bindingMap, immutableRoles );

    RoleBindingStructAdapter adapter = new RoleBindingStructAdapter();
    final JaxbSafeRoleBindingStruct marshaled = adapter.marshal( roleBindingStruct );
    assertEquals( roleBindingStruct.bindingMap.size(), marshaled.bindingMapEntries.size() );
    assertEquals( roleBindingStruct.logicalRoleNameMap.size(), marshaled.logicalRoleNameMapEntries.size() );
    assertEquals( roleBindingStruct.immutableRoles.size(), marshaled.immutableRoles.size() );

    final RoleBindingStruct unmarshaled = adapter.unmarshal( marshaled );

    assertEquals( roleBindingStruct.bindingMap, unmarshaled.bindingMap );
    assertEquals( roleBindingStruct.logicalRoleNameMap, unmarshaled.logicalRoleNameMap );
    assertEquals( roleBindingStruct.immutableRoles, unmarshaled.immutableRoles );
  }
}
