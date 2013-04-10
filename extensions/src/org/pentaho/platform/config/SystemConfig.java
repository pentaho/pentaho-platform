package org.pentaho.platform.config;

import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: nbaker
 * Date: 4/2/13
 */
public class SystemConfig implements ISystemConfig {
  private final Map<String, IConfiguration> configs = new ConcurrentHashMap<String, IConfiguration>();
  private static final Pattern pattern = Pattern.compile("([^\\.]+)\\.(.+)");
  private Logger logger = LoggerFactory.getLogger(getClass());

  public SystemConfig(){

  }

  public SystemConfig(List<IConfiguration> startingConfigs) throws IOException {
    for (IConfiguration startingConfig : startingConfigs) {
      this.registerConfiguration(startingConfig);
    }
  }

  public String getProperty(String placeholder) {
    // placeholder must be in the form of ID.PROP where
    if(!pattern.matcher(placeholder).matches()){
      throw new IllegalArgumentException("property does not follow the pattern ID.PROP");
    }
    return this.resolveValue(placeholder);
  }

  private String resolveValue(String placeholder){
    Matcher matcher = pattern.matcher(placeholder);
    matcher.find();
    String pid = matcher.group(1);
    String key = matcher.group(2);
    IConfiguration con = getConfiguration(pid);
    if(con == null){
      logger.info("Error resolving key replacement: "+placeholder);
      return null;
    }
    try {
      return con.getProperties().getProperty(key);
    } catch (IOException e) {
      logger.error("Error getting properties for configuration: "+key);
      return null;
    }
  }

  @Override
  public IConfiguration getConfiguration(String configId) {
    return configs.get(configId);
  }

  @Override
  public void registerConfiguration(IConfiguration configuration) throws IOException {
    String configId = configuration.getId();
    if(configId == null){
      throw new IllegalStateException("Config id is null");
    }
    if(configs.containsKey(configId)){
      // existing config, update current instance
      try{
        configs.get(configId).update(configuration.getProperties());
      } catch (UnsupportedOperationException e){
        // ignored
      }
    } else {
      configs.put(configId, configuration);
    }
  }

  @Override
  public IConfiguration[] listConfigurations() {
    Collection<IConfiguration> entries = configs.values();
    return entries.toArray(new IConfiguration[entries.size()]);
  }

}
