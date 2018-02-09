/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
