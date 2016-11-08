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

/**
 * An exception that occurs when name used within a file is not valid.
 * 
 * @author mlowery
 */
public class RepositoryFileDaoMalformedNameException extends RepositoryFileDaoException {

  private static final long serialVersionUID = 451157145460281861L;

  private String name;

  public RepositoryFileDaoMalformedNameException( final String name ) {
    super();
    this.name = name;
  }

  public RepositoryFileDaoMalformedNameException( final String message, final Throwable cause, final String name ) {
    super( message, cause );
    this.name = name;
  }

  public RepositoryFileDaoMalformedNameException( final String message, final String name ) {
    super( message );
    this.name = name;
  }

  public RepositoryFileDaoMalformedNameException( final Throwable cause, final String name ) {
    super( cause );
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "RepositoryFileDaoMalformedNameException [name=" + name + "]"; //$NON-NLS-1$//$NON-NLS-2$
  }

}
