package org.pentaho.test.platform.plugin.pluginmgr;

@SuppressWarnings("nls")
public class ClassToOverride {
  
  private String overridenClass;
  
  @Override
  public String toString() {
    return "I am the overridden class from the plugin class loader";
  }

}
