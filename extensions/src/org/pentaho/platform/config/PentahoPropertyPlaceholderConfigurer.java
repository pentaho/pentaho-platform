package org.pentaho.platform.config;

import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * User: nbaker
 * Date: 4/2/13
 */
public class PentahoPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
  private ISystemConfig config;

  public PentahoPropertyPlaceholderConfigurer(ISystemConfig config) {
    if(config == null){
      throw new IllegalArgumentException("ISystemConfig was null");
    }
    this.config = config;
    this.setIgnoreUnresolvablePlaceholders(true);
  }

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props) {
    // placeholder must be in the form of ID.PROP where
    String val = this.resolveValue(placeholder);
    if(val == null){
      val = super.resolvePlaceholder(placeholder, props);
    }
    return val;
  }

  private String resolveValue(String placeholder){
    return config.getProperty(placeholder);
  }

}
