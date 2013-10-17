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

package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.util.LocalizationUtil;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository2.unified.RepositoryUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class CachingPentahoMetadataDomainRepository extends PentahoMetadataDomainRepository {
  private Map<String, Domain> cache;

  /**
   * @param repository
   */
  public CachingPentahoMetadataDomainRepository( final IUnifiedRepository repository ) {
    super( repository );
    cache = new HashMap<String, Domain>();
  }

  /**
   * @param repository
   * @param repositoryUtils
   * @param xmiParser
   * @param localizationUtil
   */
  public CachingPentahoMetadataDomainRepository( final IUnifiedRepository repository,
      final RepositoryUtils repositoryUtils, final XmiParser xmiParser, final LocalizationUtil localizationUtil ) {
    super( repository, repositoryUtils, xmiParser, localizationUtil );
    cache = new HashMap<String, Domain>();
  }

  /**
   * Store a domain to the repository. The domain should persist between JVM restarts.
   * 
   * @param domain
   *          domain object to store
   * @param overwrite
   *          if true, overwrite existing domain
   * @throws org.pentaho.metadata.repository.DomainIdNullException
   *           if domain id is null
   * @throws org.pentaho.metadata.repository.DomainAlreadyExistsException
   *           if domain exists and overwrite = false
   * @throws org.pentaho.metadata.repository.DomainStorageException
   *           if there is a problem storing the domain
   */
  @Override
  public void storeDomain( final Domain domain, final boolean overwrite ) throws DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {
    super.storeDomain( domain, overwrite );
    cache.put( domain.getId(), domain );
  }

  /**
   * retrieve a domain from the repo. This does lazy loading of the repo, so it calls reloadDomains() if not already
   * loaded.
   * 
   * @param domainId
   *          domain to get from the repository
   * @return domain object
   */
  @Override
  public Domain getDomain( final String domainId ) {
    Domain domain = cache.get( domainId );
    if ( null == domain ) {
      domain = super.getDomain( domainId );
      if ( null != domain ) {
        cache.put( domainId, domain );
      }
    }
    return domain;
  }

  /**
   * Returns a list of all the domain ids in the repository.
   * 
   * @return the domain Ids.
   */
  @Override
  public Set<String> getDomainIds() {
    if ( cache.isEmpty() ) {
      Set<String> domainIds = super.getDomainIds();
      for ( final String domainId : domainIds ) {
        cache.put( domainId, null );
      }
    }
    return cache.keySet();
  }

  /**
   * reload domains from disk
   */
  @Override
  public void reloadDomains() {
    flushDomains();
    getDomainIds();
  }

  /**
   * flush the domains from memory
   */
  @Override
  public void flushDomains() {
    super.flushDomains();
    cache.clear();
  }
}
