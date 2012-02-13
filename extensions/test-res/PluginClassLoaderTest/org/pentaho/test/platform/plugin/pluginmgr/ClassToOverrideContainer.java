package org.pentaho.test.platform.plugin.pluginmgr;

public class ClassToOverrideContainer {
  public static final ClassToOverride CLASS_TO_OVERRIDE = new ClassToOverride();
  
  @Override
  public String toString() {
    return new ClassToOverride().toString();
  }
}
