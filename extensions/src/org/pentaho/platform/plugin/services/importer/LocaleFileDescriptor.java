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

package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.InputStream;

public class LocaleFileDescriptor {

  private String name;
  private String path;
  private String description;
  private RepositoryFile file;
  private InputStream inputStream;

  public LocaleFileDescriptor( String name, String description, String path, RepositoryFile file,
      InputStream inputStream ) {
    this.name = name;
    this.description = description;
    this.path = path;
    this.inputStream = inputStream;
    this.file = file;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream( InputStream inputStream ) {
    this.inputStream = inputStream;
  }

  public RepositoryFile getFile() {
    return file;
  }

  public void setFile( RepositoryFile file ) {
    this.file = file;
  }
}
