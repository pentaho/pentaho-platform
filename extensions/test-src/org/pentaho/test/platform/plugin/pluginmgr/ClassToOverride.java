package org.pentaho.test.platform.plugin.pluginmgr;

@SuppressWarnings("nls")
public class ClassToOverride {
  
  public String originalClass;
  
  @Override
  public String toString() {
    return "I am the original class from the parent class loader";
  }

}
