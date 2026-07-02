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


package org.pentaho.platform.repository2.unified.exception;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * An exception that occurs when a file already exists.
 * 
 * @author mlowery
 */
public class RepositoryFileDaoFileExistsException extends RepositoryFileDaoException {

  private static final long serialVersionUID = 451157145460281861L;

  private RepositoryFile file;

  public RepositoryFileDaoFileExistsException( final RepositoryFile file ) {
    super();
    this.file = file;
  }

  public RepositoryFile getFile() {
    return file;
  }

  @Override
  public String toString() {
    return "RepositoryFileDaoFileExistsException [file=" + file + "]"; //$NON-NLS-1$ //$NON-NLS-2$
  }

}
