/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * User: nbaker Date: 7/14/13
 */

public class MapAdapter extends XmlAdapter<Parameters, Map<String, String>> {

  @Override
  public Map<String, String> unmarshal( Parameters params ) {

    Map<String, String> map = new org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters();
    for ( Parameters.Entries.Entry entry : params.getEntries().getEntry() ) {
      map.put( entry.getKey(), entry.getValue() );
    }
    return map;
  }

  @Override
  public Parameters marshal( Map<String, String> map ) {
    try {
      Parameters params = new Parameters();
      Parameters.Entries entries = new Parameters.Entries();
      params.setEntries( entries );
      for ( Map.Entry<String, String> entry : map.entrySet() ) {
        Parameters.Entries.Entry e = new Parameters.Entries.Entry();
        e.setKey( entry.getKey() );
        e.setValue( entry.getValue() );
        params.getEntries().getEntry().add( e );
      }
      return params;
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }
}
