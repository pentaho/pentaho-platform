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


package org.pentaho.platform.repository2.unified.lifecycle;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.springframework.util.Assert;

import java.util.List;

/**
 * An {@link IBackingRepositoryLifecycleManager} that does nothing itself but instead delegates to an ordered collection of other
 * {@link IBackingRepositoryLifecycleManager} instances.
 * 
 * @author mlowery
 */
public class DelegatingBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private List<IBackingRepositoryLifecycleManager> managers;

  // ~ Constructors
  // ====================================================================================================

  public DelegatingBackingRepositoryLifecycleManager( final List<IBackingRepositoryLifecycleManager> managers ) {
    super();
    Assert.notNull( managers, "The managers list must not be null. Ensure a valid list of managers is provided." );
    this.managers = managers;
  }

  // ~ Methods
  // =========================================================================================================

  public void newTenant() {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.newTenant();
    }
  }

  public void newTenant( final ITenant tenant ) {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.newTenant( tenant );
    }
  }

  public void newUser() {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.newUser();
    }
  }

  public void newUser( final ITenant tenant, final String username ) {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.newUser( tenant, username );
    }
  }

  public synchronized void shutdown() {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.shutdown();
    }
  }

  public synchronized void startup() {
    for ( IBackingRepositoryLifecycleManager manager : managers ) {
      manager.startup();
    }
  }

  @Override
  public void addMetadataToRepository( String arg0 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public Boolean doesMetadataExists( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addLifeCycleManager( IBackingRepositoryLifecycleManager manager ) {
    managers.add( manager );
  }

  public void removeLifeCycleManager( IBackingRepositoryLifecycleManager manager ) {
    managers.remove( manager );
  }  
  
}
