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


package org.pentaho.platform.repository2.unified.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import java.util.concurrent.Callable;

/**
 * {@link OrderedAuthenticationListener} that invokes {@link IBackingRepositoryLifecycleManager#newTenant()} and
 * {@link IBackingRepositoryLifecycleManager#newUser()}. This listener fires either on interactive or
 * non-interactive logins.
 *
 * @author mlowery
 */
public class BackingRepositoryLifecycleManagerAuthenticationSuccessListener implements ApplicationListener, Ordered {
  // ~ Static fields/initializers
  // ======================================================================================

  private static final Log logger = LogFactory
      .getLog( BackingRepositoryLifecycleManagerAuthenticationSuccessListener.class );

  // ~ Instance fields
  // =================================================================================================

  private int order = 200;

  private IBackingRepositoryLifecycleManager lifecycleManager;
  private ISecurityHelper securityHelper;

  // ~ Constructors
  // ====================================================================================================

  public BackingRepositoryLifecycleManagerAuthenticationSuccessListener() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void onApplicationEvent( final ApplicationEvent event ) {
    if ( event instanceof AuthenticationSuccessEvent || event instanceof InteractiveAuthenticationSuccessEvent ) {
      logger.debug( "received AbstractAuthenticationEvent" ); //$NON-NLS-1$

      // Get the lifecycle manager for this event
      final IBackingRepositoryLifecycleManager lifecycleManager = getLifecycleManager();
      // Execute new tenant with the tenant id from the logged in user
      final AbstractAuthenticationEvent aEvent = (AbstractAuthenticationEvent) event;
      final String principalName = getPrincipalName( aEvent.getAuthentication() );

      try {
        getSecurityHelper().runAsSystem( new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            // Execute new tenant with the tenant id from the logged in user
            lifecycleManager.newTenant( JcrTenantUtils.getTenant( principalName, true ) );
            return null;
          }
        } );
      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }

      try {
        getSecurityHelper().runAsSystem( new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            // Execute new tenant with the tenant id from the logged in user
            lifecycleManager.newUser( JcrTenantUtils.getTenant( principalName, true ), JcrTenantUtils.getPrincipalName(
                principalName, true ) );
            return null;
          }
        } );
      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }

      try {
        // The newTenant() call should be executed as the system (or more correctly the tenantAdmin)
        getSecurityHelper().runAsSystem( new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            lifecycleManager.newTenant();
            return null;
          }
        } );
      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }

      try {
        // run as user to populate SecurityContextHolder and PentahoSessionHolder since Spring Security events are
        // fired
        // before SecurityContextHolder is set
        getSecurityHelper().runAsUser( principalName, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            lifecycleManager.newUser();
            return null;
          }
        } );

      } catch ( Exception e ) {
        logger.error( e.getLocalizedMessage(), e );
      }
      logger.info( "The user \"" + principalName +"\"" + " connected to repository" );
    }
  }

  protected String getPrincipalName( Authentication authentication ) {
    return authentication.getName();
  }

  public int getOrder() {
    return order;
  }

  public void setOrder( final int order ) {
    this.order = order;
  }

  /**
   * @return the {@link IBackingRepositoryLifecycleManager} that this instance will use. If none has been
   *         specified, it will default to getting the information from {@link PentahoSystem.get()}
   */
  public IBackingRepositoryLifecycleManager getLifecycleManager() {
    // Check ... if we haven't been injected with a lifecycle manager, get one from PentahoSystem
    return ( null != lifecycleManager ? lifecycleManager
        : PentahoSystem.get( IBackingRepositoryLifecycleManager.class ) );
  }

  /**
   * Sets the {@link IBackingRepositoryLifecycleManager} to be used by this instance
   *
   * @param lifecycleManager
   *          the lifecycle manager to use (can not be null)
   */
  public void setLifecycleManager( final IBackingRepositoryLifecycleManager lifecycleManager ) {
    assert ( null != lifecycleManager );
    this.lifecycleManager = lifecycleManager;
  }

  /**
   * @return the {@link ISecurityHelper} used by this instance. If none has been specified, it will default to
   *         using the {@link SecurityHelper} singleton.
   */
  public ISecurityHelper getSecurityHelper() {
    return ( null != securityHelper ? securityHelper : SecurityHelper.getInstance() );
  }

  /**
   * Sets the {@link ISecurityHelper} to be used by this instance. This can not be {@code null}
   *
   * @param securityHelper
   *          the {@link ISecurityHelper} to be used by this instance
   */
  public void setSecurityHelper( final ISecurityHelper securityHelper ) {
    this.securityHelper = securityHelper;
  }
}
