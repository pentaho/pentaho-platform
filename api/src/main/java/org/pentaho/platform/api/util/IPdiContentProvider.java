/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.api.util;

import java.util.Map;

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
  Map<String, String> getUserParameters(String kettleFilePath );

  /**
   * lists variables for a given a ktr/kjb filePath
   *
   * @param kettleFilePath
   *          ktr/kjb filePath
   * @return list of variables' name
   */
  Map<String, String> getVariables( String kettleFilePath );
}
