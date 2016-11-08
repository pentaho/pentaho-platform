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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.springframework.security.core.GrantedAuthority;

@Deprecated
public interface IAclVoter {

  /**
   * Determines whether the user (auth) has the requested authority (mask) based on the list of effective
   * authorities from the holder.
   *
   * @param auth
   * @param holder
   * @param mask
   * @return true if the user has the requested access.
   */
  public boolean hasAccess( IPentahoSession session, IAclHolder holder, int mask );

  /**
   * Returns an array of the authorities from the IAclHolder that apply to the provided authentication object.
   *
   * mlowery In practice this method does not do the same thing as EffectiveAclsResolver.
   *
   * @param auth
   * @param holder
   * @return The array of authorities from the IAclHolder that apply to the person in question
   */
  public IAclEntry[] getEffectiveAcls( IPentahoSession session, IAclHolder holder );

  /**
   * Determines whether the user is a super-manager of Pentaho. Uses the Manager Role.
   *
   * @param session
   * @return <code>true</code> if the user is a super-manager
   */
  public boolean isPentahoAdministrator( IPentahoSession session );

  /**
   * Gets the role used to determine whether someone is the system-manager.
   *
   * @return <code>GrantedAuthority</code> of the role someone must be in to be the system manager.
   */
  public GrantedAuthority getAdminRole();

  /**
   * Sets the role used to determine whether someone is the system-manager.
   *
   * @param value
   *          The <code>GrantedAuthority</code> which someone must be a considered a system manager
   */
  public void setAdminRole( GrantedAuthority value );

  /**
   * Returns true if the user is a member of the specified role
   *
   * @param session
   * @param role
   * @return <code>true</code> if the user is a member of the specified role
   */
  public boolean isGranted( IPentahoSession session, GrantedAuthority role );

  /**
   * This returns the effective ACL for the piece of content for the given user. Ideally, this will look at all the
   * effective ACLs returned for this user for this piece of content, and return an ACL that encapsulates all the
   * users' access to that content. The returning PentahoAclEntry will represent the ACL that the user has to the
   * content.
   *
   * This method should NEVER return <code>null</code>. If the user has no access to the object, it needs to return
   * a PentahoAclEntry with nothing (mask of 0).
   *
   * @param session
   * @param holder
   * @return PentahoAclEntry holding the access to the object.
   */
  public IPentahoAclEntry getEffectiveAcl( IPentahoSession session, IAclHolder holder );

}
