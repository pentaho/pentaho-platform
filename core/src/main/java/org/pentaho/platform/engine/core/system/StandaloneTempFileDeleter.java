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


package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.util.ITempFileDeleter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandaloneTempFileDeleter implements ITempFileDeleter {

  private List<File> tmpFileList = Collections.synchronizedList( new ArrayList<File>() );

  public void trackTempFile( File aFile ) {
    if ( aFile != null ) {
      tmpFileList.add( aFile );
    } else {
      throw new IllegalArgumentException();
    }
  }

  public void doTempFileCleanup() {
    synchronized ( tmpFileList ) {
      for ( File file : tmpFileList ) {
        if ( file.exists() ) {
          file.delete();
        }
      }
      tmpFileList.clear();
    }
  }

  public boolean hasTempFile( String aFileName ) {
    if ( ( aFileName != null ) && ( aFileName.length() > 0 ) ) {
      synchronized ( tmpFileList ) {
        for ( File f : tmpFileList ) {
          if ( ( f.getName().equals( aFileName ) ) ) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
