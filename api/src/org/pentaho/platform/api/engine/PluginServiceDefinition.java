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

package org.pentaho.platform.api.engine;

import java.util.Collection;

public class PluginServiceDefinition {

  private String id, title, description;
  private String[] types;
  private String serviceBeanId, serviceClass;
  private Collection<String> extraClasses;

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String[] getTypes() {
    return types;
  }

  public void setTypes( String[] types ) {
    this.types = types;
  }

  public String getServiceBeanId() {
    return serviceBeanId;
  }

  public void setServiceBeanId( String serviceBeanId ) {
    this.serviceBeanId = serviceBeanId;
  }

  public String getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass( String serviceClass ) {
    this.serviceClass = serviceClass;
  }

  public Collection<String> getExtraClasses() {
    return extraClasses;
  }

  public void setExtraClasses( Collection<String> extraClasses ) {
    this.extraClasses = extraClasses;
  }

}
