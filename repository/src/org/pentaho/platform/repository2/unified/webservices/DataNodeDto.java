/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataNodeDto implements Serializable {
  private static final long serialVersionUID = -670354483372381494L;

  String id;

  String name;

  List<DataNodeDto> childNodes = new ArrayList<DataNodeDto>( 0 );

  List<DataPropertyDto> childProperties = new ArrayList<DataPropertyDto>( 0 );

  public DataNodeDto() {
    super();
    // TODO Auto-generated constructor stub
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "DataNodeDto [id=" + id + ", name=" + name + ", childNodes=" + childNodes + ", childProperties="
        + childProperties + "]";
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<DataNodeDto> getChildNodes() {
    return childNodes;
  }

  public void setChildNodes( List<DataNodeDto> childNodes ) {
    this.childNodes = childNodes;
  }

  public List<DataPropertyDto> getChildProperties() {
    return childProperties;
  }

  public void setChildProperties( List<DataPropertyDto> childProperties ) {
    this.childProperties = childProperties;
  }

}
