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


package org.pentaho.test.platform.plugin.services.webservices;

import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;

import java.util.ArrayList;
import java.util.Collection;

public class StubServiceWrapper extends ServiceConfig {

  public Class<?> getServiceClass() {
    return StubService.class;
  }

  public String getId() {
    return "StubService";
  }

  public String getTitle() {
    return "test title"; //$NON-NLS-1$
  }

  public String getDescription() {
    return "test description"; //$NON-NLS-1$
  }

  @Override
  public Collection<Class<?>> getExtraClasses() {
    ArrayList<Class<?>> extraClasses = new ArrayList<Class<?>>();
    extraClasses.add( ComplexType.class );
    return extraClasses;
  }
}
