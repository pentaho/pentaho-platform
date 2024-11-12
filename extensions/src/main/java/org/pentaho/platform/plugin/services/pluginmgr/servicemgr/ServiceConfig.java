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
