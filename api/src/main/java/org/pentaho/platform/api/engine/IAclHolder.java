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

import java.util.List;

/**
 * TODO mlowery List type should probably not be a concrete class (PentahoAclEntry).
 */
@Deprecated
public interface IAclHolder {
  public static final int ACCESS_TYPE_READ = 0;

  public static final int ACCESS_TYPE_WRITE = 1;

  public static final int ACCESS_TYPE_UPDATE = 2;

  public static final int ACCESS_TYPE_DELETE = 3;

  public static final int ACCESS_TYPE_ADMIN = 4;

  /**
   * Returns the ACLs on the existing object. Never returns null. If you need to get the effective access controls,
   * you may need to call getEffectiveAccessControls() which will chain up from this object if necessary to find
   * the ACLs that control this object.
   * 
   * @return List of ACLs for this object only.
   */
  public List<IPentahoAclEntry> getAccessControls();

  /**
   * Sets the access controls on this specific object. Currently doesn't check whether the acls are the same as
   * those assigned to the parent.
   * 
   * @param acls
   */
  public void setAccessControls( List<IPentahoAclEntry> acls );

  /**
   * Replaces existing access controls with a new list of access controls. This method should be used in favor of
   * setting the access controls with setAccessControls when the object is being persisted.
   * 
   * @param acls
   */
  public void resetAccessControls( List<IPentahoAclEntry> acls );

  /**
   * Examines whether the existing object has ACLs. If not, it will return the parent's ACLs. All the way up to the
   * top if necessary. This method should never return null.
   * 
   * @return List containing all the AclEntry objects
   */
  public List<IPentahoAclEntry> getEffectiveAccessControls();
}
