package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.MapAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

/**
 * User: nbaker
 * Date: 7/15/13
 */
@XmlJavaTypeAdapter(MapAdapter.class)
public class Parameters extends HashMap<String, String> {

}
