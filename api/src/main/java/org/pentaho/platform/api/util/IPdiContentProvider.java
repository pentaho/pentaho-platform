package org.pentaho.platform.api.util;

public interface IPdiContentProvider {

  /**
   * When a user creates a input parameter but does not want it to be written by others, it uses the syntax of an "_"
   * (underscore) as prefix; so, if a user creates input parameter "_name", then the other users looking at it will know
   * that they should not change it.
   */
  public static final String PROTECTED_PARAMETER_PREFIX = "_";

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
