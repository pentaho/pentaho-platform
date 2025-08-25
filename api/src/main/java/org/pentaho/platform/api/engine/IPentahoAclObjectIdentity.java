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


package org.pentaho.platform.api.engine;

import java.io.Serializable;


/**
 * This is a port from spring-security 2.0.8.RELEASE
 * @see https://github.com/spring-projects/spring-security/blob/2.0.8.RELEASE/core/src/main/java/org/springframework/security/acl/basic/AclObjectIdentity.java
 */
@Deprecated
public interface IPentahoAclObjectIdentity extends Serializable {
  //~ Methods ========================================================================================================

  /**
   * Refer to the <code>java.lang.Object</code> documentation for the interface contract.
   *
   * @param obj to be compared
   *
   * @return <code>true</code> if the objects are equal, <code>false</code> otherwise
   */
  boolean equals( Object obj );

  /**
   * Refer to the <code>java.lang.Object</code> documentation for the interface contract.
   *
   * @return a hash code representation of this object
   */
  int hashCode();
}
