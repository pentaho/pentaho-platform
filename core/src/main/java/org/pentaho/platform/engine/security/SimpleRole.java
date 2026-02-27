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


package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IPermissionRecipient;

@Deprecated
public class SimpleRole implements IPermissionRecipient {

  String roleName;

  public SimpleRole( final String roleName ) {
    this.roleName = roleName;
  }

  public String getName() {
    return roleName;
  }

  @Override
  public boolean equals( final Object o ) {
    return ( ( o instanceof SimpleRole ) ? roleName.equals( ( (SimpleRole) o ).getName() ) : false );
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return roleName.hashCode();
  }

  @Override
  public String toString() {
    return String.format( "SimpleRole[roleName=%s]", roleName ); //$NON-NLS-1$
  }
}
