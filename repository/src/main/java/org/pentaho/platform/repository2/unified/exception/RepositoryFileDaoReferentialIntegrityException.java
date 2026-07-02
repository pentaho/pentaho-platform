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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An exception that occurs when a file is attempted to be deleted and another file is referring to it.
 * 
 * @author mlowery
 */
public class RepositoryFileDaoReferentialIntegrityException extends RepositoryFileDaoException {

  private static final long serialVersionUID = 451157145460281861L;

  private RepositoryFile target;

  private Set<RepositoryFile> referrers;

  public RepositoryFileDaoReferentialIntegrityException( final RepositoryFile target,
      final Set<RepositoryFile> referrers ) {
    super();
    this.target = target;
    this.referrers = new HashSet<RepositoryFile>( referrers );
  }

  public RepositoryFileDaoReferentialIntegrityException( final String message, final Throwable cause,
      final RepositoryFile target, final Set<RepositoryFile> referrers ) {
    super( message, cause );
    this.target = target;
    this.referrers = new HashSet<RepositoryFile>( referrers );
  }

  public RepositoryFileDaoReferentialIntegrityException( final String message, final RepositoryFile target,
      final Set<RepositoryFile> referrers ) {
    super( message );
    this.target = target;
    this.referrers = new HashSet<RepositoryFile>( referrers );
  }

  public RepositoryFileDaoReferentialIntegrityException( final Throwable cause, final RepositoryFile target,
      final Set<RepositoryFile> referrers ) {
    super( cause );
    this.target = target;
    this.referrers = new HashSet<RepositoryFile>( referrers );
  }

  public RepositoryFile getTarget() {
    return target;
  }

  public Set<RepositoryFile> getReferrers() {
    return Collections.unmodifiableSet( referrers );
  }

  @Override
  public String toString() {
    return "RepositoryFileDaoReferentialIntegrityException [target=" + target + ", referrers=" + referrers + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
