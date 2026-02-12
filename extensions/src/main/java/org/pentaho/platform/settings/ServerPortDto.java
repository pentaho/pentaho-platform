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


package org.pentaho.platform.settings;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author tkafalas
 */
@XmlRootElement( name = "ServerPort" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ServerPortDto implements Serializable {
  private static final long serialVersionUID = 0;
  private String id;
  private String serviceName;
  private Integer value;
  private String friendlyName;

  public ServerPortDto() {

  }

  public ServerPortDto( ServerPort serverPort ) {
    this.id = serverPort.getId();
    this.value = serverPort.getAssignedPort();
    this.friendlyName = serverPort.getFriendlyName();
  }

  protected String getId() {
    return id;
  }

  protected void setId( String id ) {
    this.id = id;
  }

  protected Integer getValue() {
    return value;
  }

  protected void setValue( Integer value ) {
    this.value = value;
  }

  protected String getFriendlyName() {
    return friendlyName;
  }

  protected void setFriendlyName( String friendlyName ) {
    this.friendlyName = friendlyName;
  }

}
