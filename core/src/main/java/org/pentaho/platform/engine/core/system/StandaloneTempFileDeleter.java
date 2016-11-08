/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
