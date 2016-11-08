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

package org.pentaho.platform.plugin.services.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holder of information relating to the mappings between Pentaho Metadata Domain IDs, Locales, and the repository
 * filename.
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
class PentahoMetadataInformationMap {
  private static final Log log = LogFactory.getLog( PentahoMetadataInformationMap.class );

  private Map<String, Map<String, RepositoryFile>> mapping = new HashMap<String, Map<String, RepositoryFile>>();
  private static final String DOMAIN_ID_KEY = "domain-id";

  public Collection<String> getDomainIds() {
    return mapping.keySet();
  }

  public void reset() {
    mapping.clear();
  }

  public RepositoryFile getDomainFile( final String domainId ) {
    Assert.notNull( domainId );
    final Map<String, RepositoryFile> details = getDetails( domainId, false );
    if ( details != null ) {
      return details.get( DOMAIN_ID_KEY );
    }
    return null;
  }

  public Set<RepositoryFile> getFiles( final String domainId ) {
    final Set<RepositoryFile> files = new HashSet<RepositoryFile>();
    final Map<String, RepositoryFile> details = mapping.get( domainId );
    if ( null != details ) {
      files.addAll( details.values() );
    }
    return files;
  }

  public void addDomain( final String domainId, final RepositoryFile child ) {
    final Map<String, RepositoryFile> details = getDetails( domainId, true );
    if ( details.get( DOMAIN_ID_KEY ) != null ) {
      log.warn( "Adding domain when one already exists" ); // TODO I18N
    }
    details.put( DOMAIN_ID_KEY, child );
  }

  public void addLocale( final String domainId, final String locale, final RepositoryFile child ) {
    final Map<String, RepositoryFile> details = getDetails( domainId, true );
    if ( details.get( locale ) != null ) {
      log.warn( "Adding locale when one already exists" ); // TODO I18N
    }
    details.put( locale, child );
  }

  public RepositoryFile getLocaleFile( final String domainId, final String locale ) {
    RepositoryFile file = null;
    final Map<String, RepositoryFile> details = getDetails( domainId, false );
    if ( details != null ) {
      file = details.get( locale );
    }
    return file;
  }

  public Map<String, RepositoryFile> getLocaleFiles( final String domainId ) {
    Map<String, RepositoryFile> files = null;
    final Map<String, RepositoryFile> details = getDetails( domainId, false );
    if ( details != null ) {
      files = new HashMap<String, RepositoryFile>( details.size() );
      files.putAll( details );
      files.remove( DOMAIN_ID_KEY );
    }
    return files;
  }

  public void deleteDomain( final String domainId ) {
    mapping.remove( domainId );
  }

  private Map<String, RepositoryFile> getDetails( final String domainId, final boolean create ) {
    Map<String, RepositoryFile> details = mapping.get( domainId );
    if ( details == null && create ) {
      details = new HashMap<String, RepositoryFile>();
      mapping.put( domainId, details );
    }
    return details;
  }
}
