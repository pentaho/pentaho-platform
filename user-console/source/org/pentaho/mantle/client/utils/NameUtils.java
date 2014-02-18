package org.pentaho.mantle.client.utils;

public class NameUtils {

  public static boolean isRepositoryObjectNameValid(String name){
    String prohib = "\\/\\:\\[\\]\\*'\"\\|,\\.\\?;\\\\"; //$NON-NLS-1$
    String prohibited = "[^"+prohib+"]*["+prohib+"]+.*"; //$NON-NLS-1$
    if ( name.matches( prohibited ) ) {
      return false;
    }
    return true;
  }
  
}
