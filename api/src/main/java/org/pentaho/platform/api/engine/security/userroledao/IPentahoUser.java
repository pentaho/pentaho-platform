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


package org.pentaho.platform.api.engine.security.userroledao;

import org.pentaho.platform.api.mt.ITenant;

import java.io.Serializable;

/**
 * A user of the Pentaho platform. Contains a set of roles for which this user is a member.
 * 
 * @author mlowery
 */
public interface IPentahoUser extends Serializable {

  String getUsername();

  ITenant getTenant();

  String getPassword();

  void setPassword( String password );

  boolean isEnabled();

  void setEnabled( boolean enabled );

  String getDescription();

  void setDescription( String description );
}
