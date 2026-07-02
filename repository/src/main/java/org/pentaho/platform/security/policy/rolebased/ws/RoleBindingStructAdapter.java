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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.util.messages.Messages;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
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
