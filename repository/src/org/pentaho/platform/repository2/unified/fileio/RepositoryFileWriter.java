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
