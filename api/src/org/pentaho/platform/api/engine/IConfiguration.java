package org.pentaho.platform.api.engine;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

/**
 * User: nbaker
 * Date: 4/2/13
 */
public interface IConfiguration {
  String getId();
  Properties getProperties() throws IOException;
  void update(Properties properties) throws IOException;
}
