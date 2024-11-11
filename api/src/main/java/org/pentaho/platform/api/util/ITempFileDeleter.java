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
