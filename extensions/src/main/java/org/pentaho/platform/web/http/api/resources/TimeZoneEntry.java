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

package org.pentaho.platform.web.http.api.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TimeZoneEntry {

  String key;
  String value;

  public TimeZoneEntry() {
  }

  public TimeZoneEntry( String key, String value ) {
    this.key = key;
    this.value = value;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
