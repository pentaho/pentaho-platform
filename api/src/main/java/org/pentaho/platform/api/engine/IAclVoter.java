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
   * Determines whether the user is a super-manager of Hitachi Vantara. Uses the Manager Role.
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
