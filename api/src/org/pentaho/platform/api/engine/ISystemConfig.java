package org.pentaho.platform.api.engine;

import java.io.IOException;

/**
 * User: nbaker
 * Date: 4/2/13
 */
public interface ISystemConfig {
  IConfiguration getConfiguration(String configId);
  String getProperty(String placeholder);
  void registerConfiguration(IConfiguration configuration) throws IOException;
  IConfiguration[] listConfigurations();

}
