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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
