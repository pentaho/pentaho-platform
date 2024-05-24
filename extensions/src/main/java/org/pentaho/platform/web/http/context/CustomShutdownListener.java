package org.pentaho.platform.web.http.context;


import  jakarta.servlet.ServletContextListener;
import  jakarta.servlet.ServletContextEvent;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.xml.XmlConfiguration;
public class CustomShutdownListener implements ServletContextListener {
  
  
  private CacheManager cacheManager;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // Initialize CacheManager
    Configuration xmlConfig = new XmlConfiguration(getClass().getResource("/ehcache.xml"));
    cacheManager = org.ehcache.config.builders.CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Shutdown CacheManager
    if (cacheManager != null) {
      cacheManager.close();
    }
  }
}
