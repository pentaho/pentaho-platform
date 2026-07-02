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


package org.pentaho.mantle.client.objects;

import java.io.Serializable;

public class UserPermission implements Serializable {
  private static final long serialVersionUID = 4396089838181301563L;
  public String name;
  public int mask;

  public UserPermission() {
  }

  public UserPermission( String name, int mask ) {
    this.name = name;
    this.mask = mask;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public int getMask() {
    return mask;
  }

  public void setMask( int mask ) {
    this.mask = mask;
  }

  public String toString() {
    return "UserPermission[name=" + name + ", mask=" + mask + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
