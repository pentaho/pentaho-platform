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
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

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
