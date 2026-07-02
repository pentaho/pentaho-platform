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


package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

import java.util.List;

@XmlRootElement( name = "List" )
@XmlAccessorType( XmlAccessType.FIELD )
public class JaxbList<T> {
  @XmlElement( name = "Item" )
  protected List<T> list;

  public JaxbList() {
  }

  public JaxbList( List<T> list ) {
    this.list = list;
  }

  public List<T> getList() {
    return list;
  }

  public void setList( List<T> list ) {
    this.list = list;
  }
}
