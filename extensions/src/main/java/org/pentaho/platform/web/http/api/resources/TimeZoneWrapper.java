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

package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class TimeZoneWrapper {
  Map<String, String> timeZones = new HashMap<String, String>();
  String serverTzId;

  public TimeZoneWrapper() {
    this( null );
  }

  public TimeZoneWrapper( Map<String, String> timeZones ) {
    this( timeZones, null );
  }

  /**
   * @param timeZones2
   * @param id
   */
  public TimeZoneWrapper( Map<String, String> timeZones, String serverTzId ) {
    super();
    this.timeZones.putAll( timeZones );
    this.serverTzId = serverTzId;
  }

  public Map<String, String> getTimeZones() {
    return timeZones;
  }

  public void setTimeZones( Map<String, String> timeZones ) {
    if ( timeZones != this.timeZones ) {
      this.timeZones.clear();
      this.timeZones.putAll( timeZones );
    }
  }

  public String getServerTzId() {
    return serverTzId;
  }

  public void setServerTzId( String serverTzId ) {
    this.serverTzId = serverTzId;
  }
}
