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


package org.pentaho.platform.engine.services.actionsequence;

public class ActionParameterSource {

  private String sourceName;

  private String value;

  public ActionParameterSource( final String sourceName, final String value ) {
    this.sourceName = sourceName;
    this.value = value;
  }

  public String getSourceName() {
    return sourceName;
  }

  public String getValue() {
    return value;
  }

}
