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
import java.util.Arrays;
import java.util.List;

public class MondrianSchema implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  private List<MondrianCube> cubes;

  private String[] roleNames;

  public MondrianSchema( final String name, final List<MondrianCube> cubes ) {
    this( name, cubes, null );
  }

  public MondrianSchema( final String name, final List<MondrianCube> cubes, String[] roles ) {
    this.name = name;
    this.cubes = cubes;
    if ( ( roles != null ) ) {
      if ( roles.length > 0 ) {
        this.roleNames = new String[roles.length];
        System.arraycopy( roles, 0, this.roleNames, 0, roles.length );
        Arrays.sort( this.roleNames ); // Required for binary search to work
      } else {
        // Since they're passing in an empty array, set this
        // to an empty array as well. This conscious choice could be
        // undesirable behavior - use cases to be seen
        this.roleNames = new String[0];
      }
    }
  }

  public String getName() {
    return name;
  }

  public List<MondrianCube> getCubes() {
    return cubes;
  }

  // This smethod must always return a sorted array
  public String[] getRoleNames() {
    return this.roleNames;
  }

  @Override
  public String toString() {
    return new ToStringBuilder( this ).append( "name", name ).append( "cubes", cubes ).toString(); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
