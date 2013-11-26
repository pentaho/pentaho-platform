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
