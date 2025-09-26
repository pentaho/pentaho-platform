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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

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
      final String principalName = getPentahoUserName( aEvent );

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
      logger.info( "The user \"" + principalName + "\"" + " connected to repository" );
    }
  }

  /**
   * Doing this for OAuth without adding any Oauth dependency on CE.
   * This is the best place to set the authenticated user in PentahoSessionHolder,
   * as this event is fired after successful authentication from attemptAuthentication in respective Filter.
   * <p>
   * Resetting the username as per pentaho user details service because from oauth casing may be different for username.
   * In case of Jackrabbit or LDAP the username is loaded from the repository or LDAP and the casing is handled.
   * In case of OAuth the username is fetched from the token and the casing needs to be handled manually.
   * So we are resetting the username as per pentaho user details service.
   * <p>
   * For OAuth, For example, if the username is "SUZY" in the token and "suzy" in pentaho user details service
   * then we will use "suzy" as the username in pentaho session
   * <p>
   * If user is not found then we will use the principal name as is.
   *
   * @param aEvent - AbstractAuthenticationEvent
   * @return pentahoUsername
   */
  private static String getPentahoUserName( AbstractAuthenticationEvent aEvent ) {
    String username = aEvent.getAuthentication().getName();
    try {
      UserDetailsService userDetailsService = PentahoSystem.get( UserDetailsService.class );
      UserDetails userDetails = userDetailsService.loadUserByUsername( username );
      username = StringUtils.isEmpty( userDetails.getUsername() ) ? userDetails.getUsername() : username;

      PentahoSessionHolder.getSession().setAuthenticated( username );
    } catch ( Exception e ) {
      logger.error( e.getLocalizedMessage(), e );
    }
    return username;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder( final int order ) {
    this.order = order;
  }

  /**
   * @return the {@link IBackingRepositoryLifecycleManager} that this instance will use. If none has been
   * specified, it will default to getting the information from {@link PentahoSystem.get()}
   */
  public IBackingRepositoryLifecycleManager getLifecycleManager() {
    // Check ... if we haven't been injected with a lifecycle manager, get one from PentahoSystem
    return ( null != lifecycleManager ? lifecycleManager
      : PentahoSystem.get( IBackingRepositoryLifecycleManager.class ) );
  }

  /**
   * Sets the {@link IBackingRepositoryLifecycleManager} to be used by this instance
   *
   * @param lifecycleManager the lifecycle manager to use (can not be null)
   */
  public void setLifecycleManager( final IBackingRepositoryLifecycleManager lifecycleManager ) {
    assert ( null != lifecycleManager );
    this.lifecycleManager = lifecycleManager;
  }

  /**
   * @return the {@link ISecurityHelper} used by this instance. If none has been specified, it will default to
   * using the {@link SecurityHelper} singleton.
   */
  public ISecurityHelper getSecurityHelper() {
    return ( null != securityHelper ? securityHelper : SecurityHelper.getInstance() );
  }

  /**
   * Sets the {@link ISecurityHelper} to be used by this instance. This can not be {@code null}
   *
   * @param securityHelper the {@link ISecurityHelper} to be used by this instance
   */
  public void setSecurityHelper( final ISecurityHelper securityHelper ) {
    this.securityHelper = securityHelper;
  }
}
