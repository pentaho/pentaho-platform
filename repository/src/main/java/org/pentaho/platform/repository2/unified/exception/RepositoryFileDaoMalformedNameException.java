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
