package org.pentaho.test.platform.plugin.pluginmgr;

import org.pentaho.test.platform.plugin.pluginmgr.ClassToOverride;

public class ClassToOverrideContainer {
  public static final ClassToOverride CLASS_TO_OVERRIDE = new ClassToOverride();
  
  @Override
  public String toString() {
    return new ClassToOverride().toString();
  }
}
