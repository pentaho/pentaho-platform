/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.scheduler2.recur;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QualifiedDayOfMonth implements ITimeRecurrence {

  public QualifiedDayOfMonth() {
  }

  public String toString() {
    return "L"; //$NON-NLS-1$
  }
}
