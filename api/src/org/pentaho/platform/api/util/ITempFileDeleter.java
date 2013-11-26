/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.util;

import java.io.File;

public interface ITempFileDeleter {

  public static final String DELETER_SESSION_VARIABLE = "PENTAHO_TMP_DELETER"; //$NON-NLS-1$

  /**
   * Adds the provided file to the list of files being tracked
   * 
   * @param aFile
   */
  public void trackTempFile( File aFile );

  /**
   * Performs the temp file cleanup.
   */
  public void doTempFileCleanup();

  /**
   * Returns true if the specified file is being tracked by the deleter
   * 
   * @param aFileName
   * @return true if the deleter has the filename in it's list
   */
  public boolean hasTempFile( String aFileName );

}
