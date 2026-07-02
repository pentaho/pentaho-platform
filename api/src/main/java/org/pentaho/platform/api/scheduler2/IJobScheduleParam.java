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

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.List;

public interface IJobScheduleParam {
  String getName();

  void setName( String name );

  String getType();
  void setType( String type );

  Serializable getValue();

  void setStringValue( List<String> value );

  List<String> getStringValue();
}
