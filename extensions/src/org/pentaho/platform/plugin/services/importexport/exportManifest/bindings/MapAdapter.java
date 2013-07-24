package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

/**
 * User: nbaker
 * Date: 7/14/13
 */

public class MapAdapter extends XmlAdapter<Parameters,Map<String,String>> {

  @Override
  public Map<String, String> unmarshal(Parameters params) {

    Map<String, String> map = new org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters();
    for(Parameters.Entries.Entry entry : params.getEntries().getEntry()){
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }


  @Override
  public Parameters marshal(Map<String, String> map) {
    try {
      Parameters params = new Parameters();
      for(Map.Entry<String, String> entry : map.entrySet()){
        Parameters.Entries.Entry e = new Parameters.Entries.Entry();
        e.setKey(entry.getKey());
        e.setValue(entry.getValue());
        Parameters.Entries entries = new Parameters.Entries();
        params.setEntries(entries);
        params.getEntries().getEntry().add(e);
      }
      return params;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}