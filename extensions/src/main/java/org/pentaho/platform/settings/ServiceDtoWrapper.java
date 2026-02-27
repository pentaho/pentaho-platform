/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.settings;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement( name = "serviceDtoes" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ServiceDtoWrapper {

  @XmlElement( name = "Service" )
  private List<ServiceDto> serviceDtoes;

  public ServiceDtoWrapper() {
  }

  public ServiceDtoWrapper( List<ServiceDto> serviceDtoes ) {
    this.serviceDtoes = serviceDtoes;
  }
}
