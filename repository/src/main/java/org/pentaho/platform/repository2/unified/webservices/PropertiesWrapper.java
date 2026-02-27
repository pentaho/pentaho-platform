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


package org.pentaho.platform.repository2.unified.webservices;

import java.util.Properties;

/**
 * User: RFellows Date: 9/10/14
 */
public class PropertiesWrapper {

  private Properties props = new Properties();

  public PropertiesWrapper() {
  }

  public PropertiesWrapper( Properties props ) {
    setProperties( props );
  }

  public void setProperties( Properties props ) {
    this.props = props;
  }

  public Properties getProperties() {
    return props;
  }

}
