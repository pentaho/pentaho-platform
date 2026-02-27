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


package org.pentaho.platform.util.beans;

import org.pentaho.platform.util.PropertiesHelper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;

import java.util.List;


public class PropertyListFactoryBean extends PropertiesLoaderSupport implements FactoryBean {
  @Override public Object getObject() throws Exception {
    return PropertiesHelper.segment( mergeProperties() );
  }

  @Override public Class getObjectType() {
    return List.class;
  }

  @Override public boolean isSingleton() {
    return false;
  }
}
