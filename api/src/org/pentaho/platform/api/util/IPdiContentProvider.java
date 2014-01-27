package org.pentaho.platform.api.util;

public interface IPdiContentProvider {

  /**
   * given a ktr/kjb filePath, looks in its metadata for the existence of user input parameters, required to properly
   * execute
   * 
   * @param kettleFilePath
   *          ktr/kjb filePath
   * @return true if ktr/kjb required some user-input parameters to execute, false otherwise
   */
  boolean hasUserParameters( String kettleFilePath );

  /**
   * lists user input parameters for a given a ktr/kjb filePath
   * 
   * @param kettleFilePath
   *          ktr/kjb filePath
   * @return list of user input parameters' name
   */
  String[] getUserParameters( String kettleFilePath );
}
