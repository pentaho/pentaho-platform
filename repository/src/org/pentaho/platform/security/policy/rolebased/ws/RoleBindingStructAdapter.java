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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.util.messages.Messages;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts {@code RoleBindingStruct} into JAXB-safe object and vice-versa.
 * 
 * @author mlowery
 */
public class RoleBindingStructAdapter extends XmlAdapter<JaxbSafeRoleBindingStruct, RoleBindingStruct> {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory.getLog( RoleBindingStructAdapter.class );

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public RoleBindingStructAdapter() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  @Override
  public JaxbSafeRoleBindingStruct marshal( final RoleBindingStruct v ) throws Exception {
    JaxbSafeRoleBindingStruct jaxbSafeRoleBindingStruct = new JaxbSafeRoleBindingStruct();
    try {
      if ( v.logicalRoleNameMap != null ) {
        List<StringKeyStringValueMapEntry> jaxbLogicalRoleNameMapEntries =
            new ArrayList<StringKeyStringValueMapEntry>();
        for ( Map.Entry<String, String> entry : v.logicalRoleNameMap.entrySet() ) {
          StringKeyStringValueMapEntry jaxbEntry = new StringKeyStringValueMapEntry();
          jaxbEntry.key = entry.getKey();
          jaxbEntry.value = entry.getValue();
          jaxbLogicalRoleNameMapEntries.add( jaxbEntry );
        }
        jaxbSafeRoleBindingStruct.logicalRoleNameMapEntries = jaxbLogicalRoleNameMapEntries;
      }
      if ( v.bindingMap != null ) {
        List<StringKeyListValueMapEntry> jaxbBindingMapEntries = new ArrayList<StringKeyListValueMapEntry>();
        for ( Map.Entry<String, List<String>> entry : v.bindingMap.entrySet() ) {
          StringKeyListValueMapEntry jaxbEntry = new StringKeyListValueMapEntry();
          jaxbEntry.key = entry.getKey();
          jaxbEntry.value = entry.getValue();
          jaxbBindingMapEntries.add( jaxbEntry );
        }
        jaxbSafeRoleBindingStruct.bindingMapEntries = jaxbBindingMapEntries;
      }
      if ( v.immutableRoles != null ) {
        jaxbSafeRoleBindingStruct.immutableRoles = new HashSet<String>( v.immutableRoles );
      }
      return jaxbSafeRoleBindingStruct;
    } catch ( Exception e ) {
      logger
          .error(
              Messages
                  .getInstance()
                  .getString(
                      "RoleBindingStructAdapter.ERROR_0001_MARSHAL", RoleBindingStruct.class.getName(), JaxbSafeRoleBindingStruct.class.getName() ), e ); //$NON-NLS-1$
      throw e;
    }
  }

  @Override
  public RoleBindingStruct unmarshal( final JaxbSafeRoleBindingStruct v ) throws Exception {
    Map<String, String> logicalRoleNameMap = new HashMap<String, String>();
    Map<String, List<String>> bindingMap = new HashMap<String, List<String>>();
    Set<String> immutableRoles = new HashSet<String>();
    try {
      if ( v.logicalRoleNameMapEntries != null ) {
        for ( StringKeyStringValueMapEntry jaxbEntry : v.logicalRoleNameMapEntries ) {
          logicalRoleNameMap.put( jaxbEntry.key, jaxbEntry.value );
        }
      }
      if ( v.bindingMapEntries != null ) {
        for ( StringKeyListValueMapEntry jaxbEntry : v.bindingMapEntries ) {
          bindingMap.put( jaxbEntry.key, jaxbEntry.value );
        }
      }
      if ( v.immutableRoles != null ) {
        immutableRoles.addAll( v.immutableRoles );
      }
      return new RoleBindingStruct( logicalRoleNameMap, bindingMap, immutableRoles );
    } catch ( Exception e ) {
      logger
          .error(
              Messages
                  .getInstance()
                  .getString(
                      "RoleBindingStructAdapter.ERROR_0002_UNMARSHAL", JaxbSafeRoleBindingStruct.class.getName(), RoleBindingStruct.class.getName() ), e ); //$NON-NLS-1$
      throw e;
    }
  }
}
