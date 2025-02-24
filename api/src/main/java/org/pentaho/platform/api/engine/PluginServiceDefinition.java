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
