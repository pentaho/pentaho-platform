/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

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
