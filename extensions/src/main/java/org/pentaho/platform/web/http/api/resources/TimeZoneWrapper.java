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
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class TimeZoneWrapper {
  TimeZonesEntries timeZones;
  String serverTzId;

  public TimeZoneWrapper() {
    this( null );
  }

  public TimeZoneWrapper( TimeZonesEntries timeZones ) {
    this( timeZones, null );
  }

  /**
   * @param timeZones2
   * @param id
   */
  public TimeZoneWrapper( TimeZonesEntries timeZones, String serverTzId ) {
    super();
    if ( timeZones == null ) {
      this.timeZones = new TimeZonesEntries();
      this.timeZones.setEntry( new ArrayList<>() );
    } else {
      this.timeZones = timeZones;
    }
    this.serverTzId = serverTzId;
  }

  public TimeZonesEntries getTimeZones() {
    return timeZones;
  }

  public void setTimeZones( TimeZonesEntries timeZones ) {
    if ( timeZones != this.timeZones ) {
      this.timeZones = timeZones;
    }
    this.serverTzId = serverTzId;
  }

  public String getServerTzId() {
    return serverTzId;
  }

  public void setServerTzId( String serverTzId ) {
    this.serverTzId = serverTzId;
  }

  @XmlRootElement
  public static class TimeZonesEntries {
    private List<TimeZoneEntry> entry = new ArrayList<>();

    public TimeZonesEntries() {
    }

    public TimeZonesEntries( List<TimeZoneEntry> entry ) {
      this.entry = entry;
    }

    public List<TimeZoneEntry> getEntry() {
      return entry;
    }

    public void setEntry( List<TimeZoneEntry> entry ) {
      this.entry = entry;
    }
  }
}
