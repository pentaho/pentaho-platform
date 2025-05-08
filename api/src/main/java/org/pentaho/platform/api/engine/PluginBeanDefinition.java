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

public class PluginBeanDefinition {
  private String beanId, classname;

  public PluginBeanDefinition( String beanId, String classname ) {
    this.beanId = beanId;
    this.classname = classname;
  }

  public String getBeanId() {
    return beanId;
  }

  public void setBeanId( String beanId ) {
    this.beanId = beanId;
  }

  public String getClassname() {
    return classname;
  }

  public void setClassname( String classname ) {
    this.classname = classname;
  }
}
