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


package org.pentaho.platform.scheduler2.recur;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QualifiedDayOfMonth implements ITimeRecurrence {

  public QualifiedDayOfMonth() {
  }

  public String toString() {
    return "L"; //$NON-NLS-1$
  }
}
