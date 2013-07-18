package org.pentaho.platform.repository2.unified.exportManifest;

import org.pentaho.platform.repository2.unified.exportManifest.bindings.MapAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nbaker
 * Date: 7/15/13
 */
@XmlJavaTypeAdapter(MapAdapter.class)
public class Parameters extends HashMap<String, String> {

}
