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


package org.pentaho.platform.plugin.services.importer;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.InputStream;

public class LocaleFileDescriptor {

  private String name;
  private String extension;
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

  LocaleFileDescriptor( String name, String extension, String description, String path, RepositoryFile file,
                               InputStream inputStream ) {
    this ( name, description, path, file, inputStream );
    this.extension = extension;
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

  public String getExtension() {
    return extension;
  }

  public void setExtension( String extension ) {
    this.extension = extension;
  }

}
