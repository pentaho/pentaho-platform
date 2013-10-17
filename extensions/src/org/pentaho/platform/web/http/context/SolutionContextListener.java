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

package org.pentaho.platform.web.http.context;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.messages.Messages;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

public class SolutionContextListener implements ServletContextListener {

  protected static String solutionPath;

  protected static String contextPath;

  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml"; //$NON-NLS-1$

  public void contextInitialized( final ServletContextEvent event ) {

    ServletContext context = event.getServletContext();

    String encoding = context.getInitParameter( "encoding" ); //$NON-NLS-1$
    if ( encoding != null ) {
      LocaleHelper.setSystemEncoding( encoding );
    }

    String textDirection = context.getInitParameter( "text-direction" ); //$NON-NLS-1$
    if ( textDirection != null ) {
      LocaleHelper.setTextDirection( textDirection );
    }

    String localeLanguage = context.getInitParameter( "locale-language" ); //$NON-NLS-1$
    String localeCountry = context.getInitParameter( "locale-country" ); //$NON-NLS-1$
    boolean localeSet = false;
    if ( ( localeLanguage != null )
      && !"".equals( localeLanguage ) && ( localeCountry != null ) && !""
      .equals( localeCountry ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      Locale[] locales = Locale.getAvailableLocales();
      if ( locales != null ) {
        for ( Locale element : locales ) {
          if ( element.getLanguage().equals( localeLanguage ) && element.getCountry().equals( localeCountry ) ) {
            LocaleHelper.setLocale( element );
            localeSet = true;
            break;
          }
        }
      }
    }
    if ( !localeSet ) {
      // do this thread in the default locale
      LocaleHelper.setLocale( Locale.getDefault() );
    }
    LocaleHelper.setDefaultLocale( LocaleHelper.getLocale() );
    // log everything that goes on here
    Logger.info( SolutionContextListener.class.getName(), Messages.getInstance().getString(
      "SolutionContextListener.INFO_INITIALIZING" ) ); //$NON-NLS-1$
    Logger.info( SolutionContextListener.class.getName(), Messages.getInstance().getString(
      "SolutionContextListener.INFO_SERVLET_CONTEXT" ) + context ); //$NON-NLS-1$
    SolutionContextListener.contextPath = context.getRealPath( "" ); //$NON-NLS-1$
    Logger.info( SolutionContextListener.class.getName(), Messages.getInstance().getString(
      "SolutionContextListener.INFO_CONTEXT_PATH" ) + SolutionContextListener.contextPath ); //$NON-NLS-1$

    SolutionContextListener.solutionPath = PentahoHttpSessionHelper.getSolutionPath( context );
    if ( StringUtils.isEmpty( SolutionContextListener.solutionPath ) ) {
      String errorMsg =
        Messages.getInstance().getErrorString( "SolutionContextListener.ERROR_0001_NO_ROOT_PATH" ); //$NON-NLS-1$
      Logger.error( getClass().getName(), errorMsg );
      /*
       * Since we couldn't find solution repository path there is no point in going forward and the user should know
       * that a major config setting was not found. So we are throwing in a RunTimeException with the requisite message.
       */
      throw new RuntimeException( errorMsg );
    }

    Logger
      .info(
        getClass().getName(),
        Messages.getInstance().getString( "SolutionContextListener.INFO_ROOT_PATH" )
          + SolutionContextListener.solutionPath ); //$NON-NLS-1$

    String fullyQualifiedServerUrl = context.getInitParameter( "fully-qualified-server-url" ); //$NON-NLS-1$
    if ( fullyQualifiedServerUrl == null ) {
      // assume this is a demo installation
      // TODO: Create a servlet that's loaded on startup to set this value
      fullyQualifiedServerUrl = "http://localhost:8080/pentaho/"; //$NON-NLS-1$
    }

    IApplicationContext applicationContext =
      new WebApplicationContext( SolutionContextListener.solutionPath, fullyQualifiedServerUrl, context
        .getRealPath( "" ), context ); //$NON-NLS-1$

    /*
     * Copy out all the initParameter values from the servlet context and put them in the application context.
     */
    Properties props = new Properties();
    Enumeration<?> initParmNames = context.getInitParameterNames();
    String initParmName;
    while ( initParmNames.hasMoreElements() ) {
      initParmName = (String) initParmNames.nextElement();
      props.setProperty( initParmName, context.getInitParameter( initParmName ) );
    }
    ( (WebApplicationContext) applicationContext ).setProperties( props );

    setSystemCfgFile( context );
    setObjectFactory( context );

    boolean initOk = PentahoSystem.init( applicationContext );

    this.showInitializationMessage( initOk, fullyQualifiedServerUrl );
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
    String pentahoObjectFactoryClassName = context.getInitParameter( "pentahoObjectFactory" ); //$NON-NLS-1$
    String pentahoObjectFactoryConfigFile = context.getInitParameter( "pentahoObjectFactoryCfgFile" ); //$NON-NLS-1$

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
      String webSpecifiedSysCfgPath = context.getInitParameter( "pentaho-system-cfg" ); //$NON-NLS-1$
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
    if ( LocaleHelper.getLocale() == null ) {
      LocaleHelper.setLocale( Locale.getDefault() );
    }
    // log everything that goes on here
    Logger.info( SolutionContextListener.class.getName(), Messages.getInstance().getString(
      "SolutionContextListener.INFO_SYSTEM_EXITING" ) ); //$NON-NLS-1$
  }
}
