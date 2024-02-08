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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.http.context;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IServerStatusProvider;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.PentahoSystemPublisher;
import org.pentaho.platform.engine.core.system.status.PeriodicStatusLogger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

public class SolutionContextListener implements ServletContextListener {

  protected static String solutionPath;

  protected static String contextPath;

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml"; //$NON-NLS-1$

  private ServletContext context;

  private static final IServerStatusProvider serverStatusProvider = IServerStatusProvider.LOCATOR.getProvider();

  Logger logger = LoggerFactory.getLogger( getClass() );

  public void contextInitialized( final ServletContextEvent event ) {

    context = event.getServletContext();

    String encoding = getServerParameter( "encoding" ); //$NON-NLS-1$
    if ( encoding != null ) {
      LocaleHelper.setSystemEncoding( encoding );
    }

    String textDirection = getServerParameter( "text-direction" ); //$NON-NLS-1$
    if ( textDirection != null ) {
      LocaleHelper.setTextDirection( textDirection );
    }

    Locale defaultLocale = null;
    String localeLanguage = getServerParameter( "locale-language" ); //$NON-NLS-1$
    String localeCountry = getServerParameter( "locale-country" ); //$NON-NLS-1$
    if ( !StringUtils.isEmpty( localeLanguage ) && !StringUtils.isEmpty( localeCountry ) ) {
      Locale[] locales = Locale.getAvailableLocales();
      for ( Locale element : locales ) {
        if ( element.getLanguage().equals( localeLanguage ) && element.getCountry().equals( localeCountry ) ) {
          defaultLocale = element;
          break;
        }
      }
    }

    LocaleHelper.setDefaultLocale( defaultLocale );

    // do this thread in the default locale
    LocaleHelper.setThreadLocaleBase( LocaleHelper.getDefaultLocale() );

    // log everything that goes on here
    logger.info( Messages.getInstance().getString( "SolutionContextListener.INFO_INITIALIZING" ) ); //$NON-NLS-1$
    logger.info( Messages.getInstance().getString( "SolutionContextListener.INFO_SERVLET_CONTEXT", context ) ); //$NON-NLS-1$
    SolutionContextListener.contextPath = context.getRealPath( "" ); //$NON-NLS-1$
    logger
        .info(
            Messages.getInstance().getString( "SolutionContextListener.INFO_CONTEXT_PATH", SolutionContextListener.contextPath ) ); //$NON-NLS-1$

    SolutionContextListener.solutionPath = PentahoHttpSessionHelper.getSolutionPath( context );
    if ( StringUtils.isEmpty( SolutionContextListener.solutionPath ) ) {
      String errorMsg = Messages.getInstance().getErrorString( "SolutionContextListener.ERROR_0001_NO_ROOT_PATH" ); //$NON-NLS-1$
      logger.error( errorMsg );
      /*
       * Since we couldn't find solution repository path there is no point in going forward and the user should know
       * that a major config setting was not found. So we are throwing in a RunTimeException with the requisite message.
       */
      throw new RuntimeException( errorMsg );
    }

    logger.info( Messages.getInstance().getString( "SolutionContextListener.INFO_ROOT_PATH",
        SolutionContextListener.solutionPath ) ); //$NON-NLS-1$

    String fullyQualifiedServerUrl = getServerParameter( "fully-qualified-server-url" ); //$NON-NLS-1$
    if ( fullyQualifiedServerUrl == null ) {
      // assume this is a demo installation
      // TODO: Create a servlet that's loaded on startup to set this value
      fullyQualifiedServerUrl = "http://localhost:8080/pentaho/"; //$NON-NLS-1$
    }

    IApplicationContext applicationContext =
      new WebApplicationContext( SolutionContextListener.solutionPath, fullyQualifiedServerUrl, context
        .getRealPath( "" ), context ); //$NON-NLS-1$

    /*
     * Copy out all the Server.properties from to the application context
     */
    Properties props = new Properties();
    ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );
    if ( systemConfig != null ) {
      IConfiguration config = systemConfig.getConfiguration( "server" );
      if ( config != null ) {
        try {
          props.putAll( config.getProperties() );
        } catch ( IOException e ) {
          logger.error( "Could not find/read the server.properties file." );
        }
      }
    }

    /*
     * Copy out all the initParameter values from the servlet context and put them in the application context.
     */
    Enumeration<?> initParmNames = context.getInitParameterNames();
    String initParmName;
    while ( initParmNames.hasMoreElements() ) {
      initParmName = (String) initParmNames.nextElement();
      props.setProperty( initParmName, getServerParameter( initParmName, true ) );
    }
    ( (WebApplicationContext) applicationContext ).setProperties( props );

    setSystemCfgFile( context );
    setObjectFactory( context );

    serverStatusProvider.setStatus( IServerStatusProvider.ServerStatus.STARTING );
    serverStatusProvider.setStatusMessages( new String[] { "Caution, the system is initializing. Do not shut down or restart the system at this time." } );
    PeriodicStatusLogger.start();

    boolean initOk = false;
    try {
      initOk = PentahoSystem.init( applicationContext );
    } finally {
      updateStatusMessages( initOk );
      PeriodicStatusLogger.stop();
    }
    // This line signals to the scheduler that the system has initialized and that the scheduler can
    // start processing jobs
    PentahoSystemPublisher.getInstance().publish( PentahoSystemPublisher.START_UP_TOPIC, true );

    this.showInitializationMessage( initOk, fullyQualifiedServerUrl );
  }

  private void updateStatusMessages( boolean initOk ) {
    if ( initOk ) {
      serverStatusProvider.setStatusMessages( new String[] { "The system has finished initializing." } );
    } else {
      serverStatusProvider.setStatusMessages( new String[] { "Warning, one or more errors occurred during the initialization process." } );
    }
  }

  /**
   * Provide a simple extension point for someone to be able to override the behavior of the WebApplicationContext. To
   * extend or change behavior, you will need to extend WebApplicationContext, and extend SolutionContextListener to
   * override the createWebApplicationContext method. The subclassing is currently required because the initialization
   * code above makes a specific setProperties call on the returned ApplicationContext method by casting it to a
   * WebApplicationContext.
   * <p/>
   * Tangible example where this would be needed - context.getRealPath("") doesn't work the same way on all platforms.
   * In some cases, you need to pass in a null, not an empty string. For other servers that don't unpack the war, the
   * realPath call may need to be replaced with a parameter defined in the web.xml
   *
   * @param fullyQualifiedServerUrl
   * @param context
   * @return
   */
  protected WebApplicationContext createWebApplicationContext( String fullyQualifiedServerUrl,
                                                               ServletContext context ) {
    return new WebApplicationContext( SolutionContextListener.solutionPath, fullyQualifiedServerUrl, context
      .getRealPath( "" ), context ); //$NON-NLS-1$
  }

  private void setObjectFactory( final ServletContext context ) {

    final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$
    String pentahoObjectFactoryClassName = getServerParameter( "pentahoObjectFactory" ); //$NON-NLS-1$
    String pentahoObjectFactoryConfigFile = getServerParameter( "pentahoObjectFactoryCfgFile" ); //$NON-NLS-1$

    // if web.xml doesnt specify a config file, use the default path.
    if ( StringUtils.isEmpty( pentahoObjectFactoryConfigFile ) ) {
      pentahoObjectFactoryConfigFile =
        solutionPath + SYSTEM_FOLDER + "/" + DEFAULT_SPRING_CONFIG_FILE_NAME; //$NON-NLS-1$
    } else if ( -1 == pentahoObjectFactoryConfigFile.indexOf( "/" ) ) { //$NON-NLS-1$
      pentahoObjectFactoryConfigFile =
        solutionPath + SYSTEM_FOLDER + "/" + pentahoObjectFactoryConfigFile; //$NON-NLS-1$
    }
    // else objectFactoryCreatorCfgFile contains the full path.
    IPentahoObjectFactory pentahoObjectFactory;
    try {
      Class<?> classObject = Class.forName( pentahoObjectFactoryClassName );
      pentahoObjectFactory = (IPentahoObjectFactory) classObject.newInstance();
    } catch ( Exception e ) {
      String msg =
        Messages.getInstance().getErrorString(
          "SolutionContextListener.ERROR_0002_BAD_OBJECT_FACTORY", pentahoObjectFactoryClassName ); //$NON-NLS-1$
      // Cannot proceed without an object factory, so we'll put some context around what
      // we were trying to do throw a runtime exception
      throw new RuntimeException( msg, e );
    }
    pentahoObjectFactory.init( pentahoObjectFactoryConfigFile, context );
    PentahoSystem.registerPrimaryObjectFactory( pentahoObjectFactory );
  }

  /**
   * Look for a parameter called "pentaho-system-cfg". If found, use its value to set the the value of the System
   * property "SYSTEM_CFG_PATH_KEY". This value is used by a LiberatedSystemSettings class to determine the location of
   * the system configuration file. This is typically pentaho.xml.
   *
   * @param context ServletContext
   */
  private void setSystemCfgFile( final ServletContext context ) {
    String jvmSpecifiedSysCfgPath = System.getProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY );
    if ( StringUtils.isBlank( jvmSpecifiedSysCfgPath ) ) {
      String webSpecifiedSysCfgPath = getServerParameter( "pentaho-system-cfg" ); //$NON-NLS-1$
      if ( StringUtils.isNotBlank( webSpecifiedSysCfgPath ) ) {
        System.setProperty( PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY, webSpecifiedSysCfgPath );
      }
    }
    // if it is blank, no big deal, we'll simply fall back on defaults
  }

  public void showInitializationMessage( final boolean initOk, final String fullyQualifiedServerUrl ) {
    if ( PentahoSystem.getObjectFactory().objectDefined( IVersionHelper.class.getSimpleName() ) ) {
      IVersionHelper helper = PentahoSystem.get( IVersionHelper.class, null ); // No session yet
      if ( initOk ) {
        System.out
          .println( Messages
            .getInstance()
            .getString(
              "SolutionContextListener.INFO_SYSTEM_READY",
              "(" + helper.getVersionInformation( PentahoSystem.class ) + ")", fullyQualifiedServerUrl,
              SolutionContextListener.solutionPath ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } else {
        System.err
          .println( Messages
            .getInstance()
            .getString(
              "SolutionContextListener.INFO_SYSTEM_NOT_READY",
              "(" + helper.getVersionInformation( PentahoSystem.class ) + ")", fullyQualifiedServerUrl,
              SolutionContextListener.solutionPath ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
  }

  protected String getContextPath() {
    return SolutionContextListener.contextPath;
  }

  protected String getRootPath() {
    return SolutionContextListener.solutionPath;
  }

  public void contextDestroyed( final ServletContextEvent event ) {

    PentahoSystem.shutdown();

    // log everything that goes on here
    logger.info( Messages.getInstance().getString(
      "SolutionContextListener.INFO_SYSTEM_EXITING" ) ); //$NON-NLS-1$
  }

  private String getServerParameter( String paramName ) {
    return getServerParameter( paramName, false );
  }

  private String getServerParameter( String paramName, boolean suppressWarning ) {
    String result = context.getInitParameter( paramName );
    if ( result == null ) {
      ISystemConfig config = PentahoSystem.get( ISystemConfig.class );
      result = config.getProperty( "server." + paramName );
    } else {
      if ( !suppressWarning ) {
        logger.warn( Messages.getInstance().getString(
            "SolutionContextListener.WARN_WEB_XML_PARAM_DEPRECATED", paramName, result ) ); //$NON-NLS-1$
      }
    }
    return result;
  }
}
