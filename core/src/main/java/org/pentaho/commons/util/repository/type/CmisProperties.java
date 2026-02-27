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


package org.pentaho.commons.util.repository.type;

import java.util.ArrayList;
import java.util.List;

public class CmisProperties {

  private List<CmisProperty> properties = new ArrayList<CmisProperty>();

  public List<CmisProperty> getProperties() {
    return properties;
  }

  public void setProperties( List<CmisProperty> properties ) {
    this.properties = properties;
  }

}
