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
