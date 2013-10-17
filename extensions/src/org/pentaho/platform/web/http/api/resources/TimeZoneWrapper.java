/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
