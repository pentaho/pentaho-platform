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
 * A role in the Pentaho platform. Contains a set of users to which the role is assigned. A role is also known as
 * an authority.
 * 
 * @author mlowery
 */
public interface IPentahoRole extends Serializable {

  ITenant getTenant();

  String getName();

  String getDescription();

  void setDescription( String description );

}
