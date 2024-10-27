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


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
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
