package org.pentaho.platform.config;

import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;

/**
 * User: nbaker
 * Date: 4/6/13
 */
public class SolutionPropertiesFileConfiguration extends PropertiesFileConfiguration {

  public SolutionPropertiesFileConfiguration(String id, String propFile) {
    super(id, new File(getSolutionPath()+File.separator+propFile));
  }
  private static String getSolutionPath(){
    String solutionPath = null;
    if(PentahoSystem.getApplicationContext() != null){
      solutionPath = PentahoSystem.getApplicationContext().getSolutionPath("system");
    } else {
      solutionPath = System.getProperty("PentahoSystemPath");
    }
    return solutionPath;
  }
}
