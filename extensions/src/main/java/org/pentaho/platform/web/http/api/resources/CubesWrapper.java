/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement( name = "cubes" )
@XmlAccessorType( XmlAccessType.FIELD )
public class CubesWrapper {

  @XmlElement( name = "cube" )
  private List<Cube> cubes;

  public CubesWrapper() {

  }

  public CubesWrapper( List<Cube> cubes ) {
    this.cubes = cubes;
  }

  public List<Cube> getCubes() {
    return cubes;
  }

  public void setCubes( List<Cube> cubes ) {
    this.cubes = cubes;
  }
}
