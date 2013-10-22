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

package org.pentaho.platform.engine.core.system.boot;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is designed to help embedded deployments start the Pentaho system. {@link PentahoSystemBoot} is a
 * self-contained and very easy to configure platform initializer which does not impose the use of configuration
 * files on your filesystem. A booter instance gives you the flexibility to configure and run the platform entirely
 * in memory.
 * <p>
 * In general you will want to
 * <ol>
 * <li>Construct a {@link PentahoSystemBoot}
 * <li>define the system objects that your system requires, by using one of the {@link #define(Class, Class)}
 * variants
 * <li>(optionally) initialize the Pentaho system for processing requests by calling {@link #start()}
 * </ol>
 * An extremely minimal platform might be configured like this:
 * 
 * <pre>
 * &#064;Before
 * public void init() {
 *   PentahoSystemBoot booter = new PentahoSystemBoot();
 *   // setup your required object definitions
 *   booter.define( IUnifiedRepository.class, DefaultUnifiedRepository.class );
 * 
 *   // initialize the minimal platform
 *   booter.init();
 * }
 * </pre>
 * 
 * @author jamesdixon and aphillips
 * 
 */
public class PentahoSystemBoot {

  private IPentahoObjectFactory factory;

  private String filePath;

  private String fullyQualifiedServerUrl;

  // list of the system listeners to hook up
  private List<IPentahoSystemListener> lifecycleListeners = new ArrayList<IPentahoSystemListener>();

  // list of startup actions to execute
  private List<ISessionStartupAction> startupActions = new ArrayList<ISessionStartupAction>();

  // list of admin plugins (aka publishers)
  private List<IPentahoPublisher> adminActions = new ArrayList<IPentahoPublisher>();

  private ISystemSettings settingsProvider = null;

  private boolean initialized = false;

  /**
   * Creates a minimal ready-to-run platform. Use this constructor if you want to accept all the defaults for your
   * in-memory platform.
   */
  public PentahoSystemBoot() {
    configure( null, null, null );
  }

  /**
   * Creates a minimal ready-to-run platform with a specified solution path. Use this constructor if your system
   * needs to access system or other solution files from a particular directory.
   * 
   * @param solutionPath
   *          full path to the pentaho_solutions folder
   */
  public PentahoSystemBoot( String solutionPath ) {
    configure( solutionPath, null, null );
  }

  public PentahoSystemBoot( String solutionPath, String fullyQualifiedServerUrl ) {
    configure( solutionPath, fullyQualifiedServerUrl, null );
  }

  public PentahoSystemBoot( String solutionPath, IPentahoDefinableObjectFactory factory ) {
    configure( solutionPath, null, factory );
  }

  public PentahoSystemBoot( String solutionPath, String fullyQualifiedServerUrl,
                            IPentahoDefinableObjectFactory factory ) {
    configure( solutionPath, fullyQualifiedServerUrl, factory );
  }

  /**
   * Configures this booter to run. Any parameters that are <code>null</code> will be set with default values. The
   * default values are as follows:
   * <ul>
   * <li>solutionPath = "." (current working directory)
   * <li>fullyQualifiedServerUrl = "http://localhost:8080/pentaho/"
   * <li>factory = a new StandaloneObjectFactory instance
   * </ul>
   * Override this method to create a different set of defaults or use the 'setter' methods to override defaults in
   * a more fine-grained manner
   */
  protected void configure( String userFilePath, String userFullyQualifiedServerUrl,
      IPentahoDefinableObjectFactory userFactory ) {
    setFilePath( userFilePath != null ? userFilePath : new File( "." ).getAbsolutePath() ); //$NON-NLS-1$

    setFullyQualifiedServerUrl( userFullyQualifiedServerUrl != null ? userFullyQualifiedServerUrl
        : "http://localhost:8080/pentaho/" ); //$NON-NLS-1$

    setFactory( userFactory != null ? userFactory : new StandaloneObjectFactory() );

    PentahoSystem.setSystemListeners( lifecycleListeners );
    PentahoSystem.setSessionStartupActions( startupActions );
    PentahoSystem.setAdministrationPlugins( adminActions );
  }

  /**
   * Sets the file path that will be used to get to file-based resources
   * 
   * @return
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Sets the file path to be used to find configuration and content files If this is not set the current directory
   * (.) is used.
   * 
   * @param filePath
   */
  public void setFilePath( final String filePath ) {
    this.filePath = filePath;
  }

  /**
   * Sets the URL that the platform uses to generate paths to its own resources
   * 
   * @param baseUrl
   */
  public void setFullyQualifiedServerUrl( final String fullyQualifiedServerUrl ) {
    this.fullyQualifiedServerUrl = fullyQualifiedServerUrl;
  }

  /**
   * Override this method if you want to change the type and state of the application context used to initialize
   * the system.
   * 
   * @return an application context for system initialization
   */
  protected IApplicationContext createApplicationContext() {
    StandaloneApplicationContext appCtxt = new StandaloneApplicationContext( getFilePath(), "" ); //$NON-NLS-1$
    appCtxt.setFullyQualifiedServerURL( fullyQualifiedServerUrl );
    return appCtxt;
  }

  /**
   * @deprecated use {@link #start()}. This method is hanging around for backward compatibility with MicroPlatform
   */
  public void init() {
    try {
      start();
    } catch ( PlatformInitializationException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Starts the Pentaho platform, making it ready to process requests.
   * 
   * @throws PlatformInitializationException
   *           if there was a problem initializing the platform
   */
  public boolean start() throws PlatformInitializationException {
    initialized = false;
    try {
      initialized = PentahoSystem.init( createApplicationContext() );
      // we want to wrap any exception that causes initialization to fail, so we will
      // catch throwable
    } catch ( Throwable t ) {
      throw new PlatformInitializationException( Messages.getInstance().getErrorString(
          "PentahoSystemBoot.ERROR_0001_PLATFORM_INIT_FAILED" ), t ); //$NON-NLS-1$
    }

    if ( !initialized ) {
      throw new PlatformInitializationException( Messages.getInstance().getErrorString(
          "PentahoSystemBoot.ERROR_0001_PLATFORM_INIT_FAILED" ) ); //$NON-NLS-1$
    }

    return initialized;
  }

  /**
   * Stops the Pentaho platform
   * 
   * @return
   */
  public boolean stop() {
    initialized = false;
    PentahoSystem.shutdown();
    return true;
  }

  /**
   * Gets the object factory for the Pentaho platform
   * 
   * @return
   */
  public IPentahoObjectFactory getFactory() {
    return factory;
  }

  /**
   * Sets the object factory for the Pentaho platform, This defaults to the StandaloneObjectFactory
   * 
   * @return
   */
  public void setFactory( IPentahoObjectFactory factory ) {
    this.factory = factory;
    // object factory needs to also be early here so clients that do not need to
    // run the platform can have an object factory available
    PentahoSystem.clearObjectFactory();
    PentahoSystem.registerPrimaryObjectFactory( factory );
  }

  /**
   * Adds an administrative action to the system.
   * 
   * @param adminAction
   */
  public void addAdminAction( final IPentahoPublisher adminAction ) {
    adminActions.add( adminAction );
  }

  public void setAdminActions( final List<IPentahoPublisher> adminActions ) {
    this.adminActions = adminActions;
  }

  /**
   * Adds a lifecycle listener. This object will be notified when the Pentaho platform starts and stops.
   * 
   * @param lifecycleListener
   */
  public void addLifecycleListener( final IPentahoSystemListener lifecycleListener ) {
    lifecycleListeners.add( lifecycleListener );
  }

  /**
   * Returns the list of lifecycle listeners that will be used. These objects will be notified when the Pentaho
   * platform starts and stops.
   * 
   * @return
   */
  public List<IPentahoSystemListener> getLifecycleListeners() {
    return lifecycleListeners;
  }

  /**
   * Returns the list of lifecycle listeners that will be used. These objects will be notified when the Pentaho
   * platform starts and stops.
   * 
   * @return
   */
  public void setLifecycleListeners( final List<IPentahoSystemListener> lifecycleListeners ) {
    this.lifecycleListeners = lifecycleListeners;
  }

  /**
   * Gets the system settings object that will be used by the Pentaho platform
   * 
   * @return
   */
  public ISystemSettings getSettingsProvider() {
    return settingsProvider;
  }

  /**
   * Sets the system settings object that will be used by the Pentaho platform
   * 
   * @return
   */
  public void setSettingsProvider( final ISystemSettings settingsProvider ) {
    PentahoSystem.setSystemSettingsService( settingsProvider );
    this.settingsProvider = settingsProvider;
  }

  /**
   * Returns true if the Pentaho platform has initialized successfully.
   * 
   * @return
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Returns the list of startup actions. These actions will be executed on system startup or on session creation.
   * 
   * @return
   */
  public List<ISessionStartupAction> getStartupActions() {
    return startupActions;
  }

  /**
   * Sets the list of startup actions These actions will be executed on system startup or on session creation.
   * 
   * @param startupActions
   */
  public void setStartupActions( final List<ISessionStartupAction> startupActions ) {
    this.startupActions = startupActions;
  }

  /**
   * Adds a strtup action to the system. These actions will be executed on system startup or on session creation.
   * 
   * @param startupAction
   */
  public void addStartupAction( final ISessionStartupAction startupAction ) {
    startupActions.add( startupAction );
  }

  /**
   * Define an arbitrarily scoped object
   * 
   * @param key
   *          the key to retrieval of this object
   * @param implClassName
   *          the actual type that is served back to you when requested.
   * @param scope
   *          the scope of the object
   * @return the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError
   *           if the object factory does not support runtime object definition
   */
  public PentahoSystemBoot define( String key, String implClassName, Scope scope ) {
    if ( factory instanceof IPentahoDefinableObjectFactory ) {
      IPentahoDefinableObjectFactory definableFactory = (IPentahoDefinableObjectFactory) getFactory();
      definableFactory.defineObject( key, implClassName, scope );
    } else {
      throw new NoSuchMethodError( "define is only supported by IPentahoDefinableObjectFactory" ); //$NON-NLS-1$
    }
    return this;
  }

  /**
   * Define an arbitrarily scoped object
   * 
   * @param interfaceClass
   *          the key to retrieval of this object
   * @param implClass
   *          the actual type that is served back to you when requested.
   * @param scope
   *          the scope of the object
   * @return the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError
   *           if the object factory does not support runtime object definition
   */
  public PentahoSystemBoot define( Class<?> interfaceClass, Class<?> implClass, Scope scope ) {
    return define( interfaceClass.getSimpleName(), implClass.getName(), scope );
  }

  /**
   * Define an arbitrarily scoped object
   * 
   * @param key
   *          the key to retrieval of this object
   * @param implClass
   *          the actual type that is served back to you when requested.
   * @param scope
   *          the scope of the object
   * @return the current {@link PentahoSystemBoot} instance, for chaining
   * @throws NoSuchMethodError
   *           if the object factory does not support runtime object definition
   */
  public PentahoSystemBoot define( String key, Class<?> implClass, Scope scope ) {
    return define( key, implClass.getName(), scope );
  }

  /**
   * Define a locally scoped object (aka prototype scope -- unique instance for each request for the class)
   * 
   * @param interfaceClass
   *          the key to retrieval of this object
   * @param implClass
   *          the actual type that is served back to you when requested.
   * @return the current MicroPlatform instance, for chaining
   */
  public PentahoSystemBoot define( Class<?> interfaceClass, Class<?> implClass ) {
    return define( interfaceClass.getSimpleName(), implClass.getName(), Scope.LOCAL );
  }

  /**
   * Define a locally scoped object (aka prototype scope -- unique instance for each request for the class)
   * 
   * @param key
   *          the key to retrieval of this object
   * @param implClass
   *          the actual type that is served back to you when requested.
   * @return the current MicroPlatform instance, for chaining
   */
  public PentahoSystemBoot define( String key, Class<?> implClass ) {
    return define( key, implClass.getName(), Scope.LOCAL );
  }

  /**
   * Hold an object instance by key name.
   * 
   * @param key
   *          the key to retrieval of this object
   * @param instance
   *          the actual instance that is served back to you when requested.
   * @return the current MicroPlatform instance, for chaining
   */

  public PentahoSystemBoot define( String key, Object instance ) {
    if ( factory instanceof IPentahoDefinableObjectFactory ) {
      IPentahoDefinableObjectFactory definableFactory = (IPentahoDefinableObjectFactory) getFactory();
      definableFactory.defineInstance( key, instance );
    } else {
      throw new NoSuchMethodError( "defineInstance is only supported by IPentahoDefinableObjectFactory" ); //$NON-NLS-1$
    }
    return this;
  }

}
