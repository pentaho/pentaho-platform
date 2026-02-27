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
public class SimpleUser implements IPermissionRecipient {

  String userName;

  public SimpleUser( final String userName ) {
    this.userName = userName;
  }

  public String getName() {
    return userName;
  }

  @Override
  public boolean equals( final Object o ) {
    return ( ( o instanceof SimpleUser ) ? userName.equals( ( (SimpleUser) o ).getName() ) : false );
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return userName.hashCode();
  }

  @Override
  public String toString() {
    return String.format( "SimpleUser[userName=%s]", userName ); //$NON-NLS-1$
  }
}
