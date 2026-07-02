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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement( name = "List" )
@XmlAccessorType( XmlAccessType.FIELD )
public class JaxbWrapper {

  @XmlElement( name = "Item" )
  private List<JaxbList> list;

  public JaxbWrapper() {
  }

  public JaxbWrapper( List<JaxbList> list ) {
    this.list = list;
  }

  public List<JaxbList> getList() {
    return list;
  }

  public void setList( List<JaxbList> list ) {
    this.list = list;
  }
}
