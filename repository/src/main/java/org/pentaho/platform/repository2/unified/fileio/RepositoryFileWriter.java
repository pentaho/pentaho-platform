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


package org.pentaho.platform.repository2.unified.fileio;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class RepositoryFileWriter extends OutputStreamWriter {

  public RepositoryFileWriter( String path, String charsetName ) throws UnsupportedEncodingException,
    FileNotFoundException {
    super( new RepositoryFileOutputStream( path, charsetName ), charsetName );
  }

  public RepositoryFileWriter( RepositoryFile file, String charsetName ) throws UnsupportedEncodingException,
    FileNotFoundException {
    super( new RepositoryFileOutputStream( file, charsetName ), charsetName );
  }

  public RepositoryFileWriter( Serializable id, String charsetName ) throws UnsupportedEncodingException,
    FileNotFoundException {
    super( new RepositoryFileOutputStream( id, charsetName ), charsetName );
  }

}
