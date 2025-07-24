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

import java.util.List;
import java.util.Set;

/**
 * JAXB-safe version of {@code RoleBindingStruct}. ({@code RoleBindingStruct} has contains a {@code Map}.)
 * 
 * @see RoleBindingStruct
 * 
 * @author mlowery
 */
public class JaxbSafeRoleBindingStruct {
  public List<StringKeyStringValueMapEntry> logicalRoleNameMapEntries;

  public List<StringKeyListValueMapEntry> bindingMapEntries;

  public Set<String> immutableRoles;
}
