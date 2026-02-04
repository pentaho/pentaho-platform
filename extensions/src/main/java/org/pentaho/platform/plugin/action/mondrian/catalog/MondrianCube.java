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


package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

public class MondrianCube implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name;
  private String id;

  public MondrianCube( final String name, final String identifier ) {
    this.name = name;
    this.id = identifier;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return new ToStringBuilder( this ).append( "name", name ).append( "id", id ).toString(); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
