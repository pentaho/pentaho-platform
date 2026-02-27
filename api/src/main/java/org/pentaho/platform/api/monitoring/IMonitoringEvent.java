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


package org.pentaho.platform.api.monitoring;

import java.io.Serializable;

/**
 * top-most signature for any event being published to monitoring's event bus
 */
public interface IMonitoringEvent extends Serializable {

  Serializable getId();

}
