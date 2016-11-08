/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
