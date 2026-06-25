/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.pluginmgr.servicemgr;

import org.pentaho.platform.api.engine.ServiceInitializationException;

public class GwtRpcServiceManager extends AbstractServiceTypeManager {

  public void initServices() throws ServiceInitializationException {
  }

  public String getSupportedServiceType() {
    return "gwt"; //$NON-NLS-1$
  }

}
