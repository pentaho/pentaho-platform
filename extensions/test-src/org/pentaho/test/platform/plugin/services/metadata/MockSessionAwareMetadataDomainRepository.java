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

package org.pentaho.test.platform.plugin.services.metadata;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metadata Domain Repository that returns a cloned domain with the session id as the description.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public class MockSessionAwareMetadataDomainRepository implements IMetadataDomainRepository {

  public static final String TEST_LOCALE = ""; //$NON-NLS-1$

  private Map<String, Domain> domains;
  // Domains to be available after a refresh
  private Map<String, Domain> persistedDomains;
  private Map<String, AtomicInteger> invocationCounts;

  public MockSessionAwareMetadataDomainRepository() {
    this.domains = new HashMap<String, Domain>();
    this.invocationCounts = new HashMap<String, AtomicInteger>();
  }

  public void setPersistedDomains( final Domain... domains ) {
    persistedDomains = new HashMap<String, Domain>();
    for ( Domain d : domains ) {
      persistedDomains.put( d.getId(), d );
    }
  }

  private void incrementInvocationCount( String methodName ) {
    AtomicInteger count = invocationCounts.get( methodName );
    if ( count == null ) {
      count = new AtomicInteger( 0 );
      invocationCounts.put( methodName, count );
    }
    count.incrementAndGet();
  }

  public int getInvocationCount( String methodName ) {
    AtomicInteger count = invocationCounts.get( methodName );
    return count == null ? 0 : count.get();
  }

  @Override
  public void storeDomain( Domain domain, boolean overwrite ) throws DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException {
    incrementInvocationCount( "storeDomain" ); //$NON-NLS-1$
    if ( !overwrite && domains.get( domain.getId() ) != null ) {
      throw new DomainAlreadyExistsException( "cannot overwrite existing domain without overwrite=true" ); //$NON-NLS-1$
    }
    domains.put( domain.getId(), domain );
  }

  @Override
  public Domain getDomain( String id ) {
    incrementInvocationCount( "getDomain" ); //$NON-NLS-1$
    Domain d = domains.get( id );
    if ( d == null ) {
      return null;
    }
    d = (Domain) d.clone();
    final IPentahoSession session = PentahoSessionHolder.getSession();
    if ( session == null ) {
      return d;
    }
    d.setDescription( new LocalizedString( TEST_LOCALE, PentahoSessionHolder.getSession().getId() ) );
    return d;
  }

  @Override
  public Set<String> getDomainIds() {
    incrementInvocationCount( "getDomainIds" ); //$NON-NLS-1$
    return domains.keySet();
  }

  @Override
  public void reloadDomains() {
    incrementInvocationCount( "reloadDomains" ); //$NON-NLS-1$
    domains = new HashMap<String, Domain>( persistedDomains );
  }

  @Override
  public void flushDomains() {
    incrementInvocationCount( "flushDomains" ); //$NON-NLS-1$
    domains = new HashMap<String, Domain>();
  }

  @Override
  public void removeDomain( String id ) {
    incrementInvocationCount( "removeDomain" ); //$NON-NLS-1$
    domains.remove( id );
  }

  @Override
  public void removeModel( String domainId, String modelId ) throws DomainIdNullException, DomainStorageException {
    incrementInvocationCount( "removeModel" ); //$NON-NLS-1$
    // don't actually do anything
    Domain domain = getDomain( domainId );
    removeDomain( domainId );
    try {
      storeDomain( domain, true );
    } catch ( DomainAlreadyExistsException ex ) {
      throw new IllegalStateException( ex );
    }
  }

  @Override
  public String generateRowLevelSecurityConstraint( LogicalModel logicalModel ) {
    incrementInvocationCount( "generateRowLevelSecurityConstraint" ); //$NON-NLS-1$
    return null;
  }

  @Override
  public boolean hasAccess( int i, IConcept iConcept ) {
    incrementInvocationCount( "hasAccess" ); //$NON-NLS-1$
    return true;
  }
}
