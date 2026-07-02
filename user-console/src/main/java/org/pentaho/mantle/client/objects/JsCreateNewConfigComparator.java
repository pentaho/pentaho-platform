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


package org.pentaho.mantle.client.objects;

import java.util.Comparator;

public class JsCreateNewConfigComparator implements Comparator<JsCreateNewConfig> {

  public JsCreateNewConfigComparator() {
  }

  public int compare( JsCreateNewConfig o1, JsCreateNewConfig o2 ) {
    if ( o1.getPriority() < o2.getPriority() ) {
      return -1;
    }
    if ( o1.getPriority() > o2.getPriority() ) {
      return 1;
    }
    return 0;
  }

}
