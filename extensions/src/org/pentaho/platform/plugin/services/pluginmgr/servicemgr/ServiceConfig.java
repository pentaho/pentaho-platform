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

package org.pentaho.platform.plugin.services.pluginmgr.servicemgr;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IServiceConfig;

import java.util.Collection;

public class ServiceConfig implements IServiceConfig {

  private Class<?> serviceClass;
  private Collection<Class<?>> extraClasses;
  private String title, description, id;
  private String serviceType;
  private boolean enabled = true; // does this really belong here, or in the service manager?

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getId()
   */
  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the enabled state of this service
   * 
   * @param enabled
   */
  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getExtraClasses()
   */
  public Collection<Class<?>> getExtraClasses() {
    return extraClasses;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getServiceClass()
   */
  public Class<?> getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass( Class<?> serviceClass ) {
    this.serviceClass = serviceClass;
  }

  public void setExtraClasses( Collection<Class<?>> extraClasses ) {
    this.extraClasses = extraClasses;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getTitle()
   */
  public String getTitle() {
    return ( StringUtils.isEmpty( title ) ) ? getId() : title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getDescription()
   */
  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.IServiceConfig#getServiceType()
   */
  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType( String serviceType ) {
    this.serviceType = serviceType;
  }
}
