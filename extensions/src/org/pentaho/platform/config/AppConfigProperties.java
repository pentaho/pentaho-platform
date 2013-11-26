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

package org.pentaho.platform.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.config.i18n.Messages;
import org.springframework.security.providers.encoding.PasswordEncoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * By default, this class will initialize itself from <code>resource/config/console.xml</code> (relative to the current
 * working directory).
 * 
 * @author Steven Barkdull
 * @author mlowery
 * 
 */
public class AppConfigProperties {

  // ~ Static fields/initializers ======================================================================================

  public static final String CONFIG_FILE_NAME = "console.xml"; //$NON-NLS-1$
  public static final String WEB_XML_PATH = "/WEB-INF/web.xml"; //$NON-NLS-1$
  public static final String HIBERNATE_MANAGED_XML_PATH = "/system/hibernate/hibernate-settings.xml"; //$NON-NLS-1$
  public static final String PENTAHO_OBJECTS_SPRING_XML = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$
  public static final String SPRING_SECURITY_HIBERNATE_XML = "/system/applicationContext-spring-security-hibernate.xml"; //$NON-NLS-1$
  public static final String JDBC_DRIVER_PATH = "./jdbc"; //$NON-NLS-1$
  public static final String KEY_BISERVER_STATUS_CHECK_PERIOD = "biserver-status-check-period"; //$NON-NLS-1$
  public static final String KEY_BISERVER_BASE_URL = "biserver-base-url"; //$NON-NLS-1$
  public static final String KEY_BISERVER_CONTEXT_PATH = "biserver-context-path"; //$NON-NLS-1$
  public static final String KEY_PLATFORM_USERNAME = "platform-username"; //$NON-NLS-1$
  public static final String DEFAULT_VALUE_PASSWORD_SERVICE_CLASS = "org.pentaho.platform.util.Base64PasswordService"; //$NON-NLS-1$
  public static final String DEFAULT_BISERVER_BASE_URL = "http://localhost:8080/pentaho"; //$NON-NLS-1$
  public static final String DEFAULT_BISERVER_CONTEXT_PATH = "/pentaho"; //$NON-NLS-1$
  public static final String DEFAULT_PLATFORM_USERNAME = "admin"; //$NON-NLS-1$
  public static final String DEFAULT_BISERVER_STATUS_CHECK_PERIOD = "30000"; //$NON-NLS-1$
  public static final String DEFAULT_HOMEPAGE_TIMEOUT = "15000"; //$NON-NLS-1$
  public static final String DEFAULT_HIBERNATE_CONFIG_PATH = "system/hibernate/hsql.hibernate.cfg.xml"; //$NON-NLS-1$
  public static final String DEFAULT_HELP_URL =
      "http://wiki.pentaho.com/display/ServerDoc2x/The+Pentaho+Administration+Console"; //$NON-NLS-1$
  public static final String DEFAULT_HOMEPAGE_URL = "http://www.pentaho.com/console_home"; //$NON-NLS-1$

  private IConsoleConfig consoleConfig = null;
  private HibernateSettingsXml hibernateSettingXml = null;
  private PentahoObjectsConfig pentahoObjectsConfig = null;
  private static SpringSecurityHibernateConfig springSecurityHibernateConfig = null;

  // ~ Instance fields =================================================================================================
  private static AppConfigProperties instance = new AppConfigProperties();

  private static final Log logger = LogFactory.getLog( AppConfigProperties.class );

  // ~ Constructors ====================================================================================================

  protected AppConfigProperties() {
  }

  // ~ Methods =========================================================================================================

  public static synchronized AppConfigProperties getInstance() {
    return instance;
  }

  public void refreshConfig() throws AppConfigException {
    consoleConfig = null;
    hibernateSettingXml = null;
    pentahoObjectsConfig = null;
    try {
      PasswordServiceFactory.init( getPasswordServiceClass() );
    } catch ( Exception e ) {
      throw new AppConfigException( Messages.getErrorString(
          "AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + PENTAHO_OBJECTS_SPRING_XML ), e ); //$NON-NLS-1$
    }
  }

  private String getDefaultInstallDir() {
    String defaultInstallDir = "./.."; //$NON-NLS-1$
    if ( ( getConsoleConfig().getDefaultBiServerDir() != null )
        && ( getConsoleConfig().getDefaultBiServerDir().trim().length() > 0 ) ) {
      defaultInstallDir = defaultInstallDir + "/" + getConsoleConfig().getDefaultBiServerDir(); //$NON-NLS-1$
    }
    return defaultInstallDir;
  }

  public boolean isValidConfiguration() {
    boolean solutionPathValid = false;
    boolean warPathValid = false;
    File solutionPathFile = new File( getSolutionPath() );
    if ( solutionPathFile != null && solutionPathFile.isDirectory() ) {
      solutionPathValid = true;
    }
    File warPathFile = new File( getWarPath() );
    if ( warPathFile != null && warPathFile.isDirectory() ) {
      warPathValid = true;
    }
    return solutionPathValid && warPathValid;
  }

  public PasswordEncoder getPasswordEncoder() {
    return getSpringSecurityHibernateConfig().getPasswordEncoder();
  }

  public String getPlatformUsername() {
    String platormUserName = getConsoleConfig().getPlatformUserName();
    if ( ( platormUserName == null ) || ( platormUserName.trim().length() == 0 ) ) {
      platormUserName = DEFAULT_PLATFORM_USERNAME;
    }
    return platormUserName;
  }

  public String getBiServerContextPath() {
    String baseUrl = getBiServerBaseUrl();
    int start = baseUrl.lastIndexOf( ":" ); //$NON-NLS-1$
    int middle = baseUrl.indexOf( "/", start ); //$NON-NLS-1$

    String biserverContextPath = baseUrl.substring( middle, baseUrl.length() - 1 );
    if ( !( biserverContextPath != null && biserverContextPath.length() > 0 ) ) {
      biserverContextPath = DEFAULT_BISERVER_CONTEXT_PATH;
    }
    return biserverContextPath;
  }

  public String getBiServerBaseUrl() {
    String baseUrl = DEFAULT_BISERVER_BASE_URL;
    // If this setting existe in console.xml, use it
    String consoleXmlBaseUrl = getConsoleConfig().getBaseUrl();
    if ( consoleXmlBaseUrl != null ) {
      return consoleXmlBaseUrl;
    }

    try {
      WebXml webXml = new WebXml( new File( getWarPath() + WEB_XML_PATH ) );
      baseUrl = webXml.getBaseUrl();
      if ( !( baseUrl != null && baseUrl.length() > 0 ) ) {
        baseUrl = DEFAULT_BISERVER_BASE_URL;
      }
    } catch ( Exception e ) {
      // Do nothing;
    }
    return baseUrl;
  }

  public String getBiServerStatusCheckPeriod() {
    Long period = getConsoleConfig().getServerStatusCheckPeriod();
    return period != null ? period.toString() : DEFAULT_BISERVER_STATUS_CHECK_PERIOD;
  }

  /**
   * Returns a comma-separated list of roles to apply to newly created users.
   */
  public String getDefaultRolesString() {
    return getConsoleConfig().getDefaultRoles();
  }

  /**
   * Convenience wrapper around getDefaultRolesString that parses the default roles string into individual roles.
   */
  public List<String> getDefaultRoles() {
    String defaultRolesString = getDefaultRolesString();
    List<String> defaultRoles = new ArrayList<String>();
    if ( ( defaultRolesString != null ) && ( defaultRolesString.trim().length() > 0 ) ) {
      StringTokenizer tokenizer = new StringTokenizer( defaultRolesString, "," ); //$NON-NLS-1$
      while ( tokenizer.hasMoreTokens() ) {
        defaultRoles.add( tokenizer.nextToken() );
      }
    }
    return defaultRoles;
  }

  public String getHomepageUrl() {
    String homepageUrl = getConsoleConfig().getHomePageUrl();
    if ( ( homepageUrl == null ) || ( homepageUrl.trim().length() == 0 ) ) {
      homepageUrl = DEFAULT_HOMEPAGE_URL;
    }
    return homepageUrl;
  }

  public String getHomepageTimeout() {
    Integer timeout = getConsoleConfig().getHomePageTimeout();
    return timeout != null ? timeout.toString() : DEFAULT_HOMEPAGE_TIMEOUT;
  }

  public String getHibernateConfigPath() {
    String hibernateConfigPath = DEFAULT_HIBERNATE_CONFIG_PATH;
    String hibernateConfigFile = getHibernateSettingsXml().getHibernateConfigFile();
    if ( hibernateConfigFile != null && hibernateConfigFile.length() > 0 ) {
      hibernateConfigPath = hibernateConfigFile;
    }
    return hibernateConfigPath;
  }

  public boolean isHibernateManaged() {
    return getHibernateSettingsXml().getHibernateManaged();
  }

  public String getSolutionPath() {
    String pentahoSolutionPath = getConsoleConfig().getSolutionPath();
    if ( ( pentahoSolutionPath == null ) || ( pentahoSolutionPath.trim().length() == 0 ) ) {
      pentahoSolutionPath = getDefaultInstallDir() + "/pentaho-solutions"; //$NON-NLS-1$

    }
    return pentahoSolutionPath;
  }

  public String getWarPath() {
    String pentahoWarPath = getConsoleConfig().getWebAppPath();
    if ( ( pentahoWarPath == null ) || ( pentahoWarPath.trim().length() == 0 ) ) {
      pentahoWarPath = getDefaultInstallDir() + "/tomcat/webapps/pentaho"; //$NON-NLS-1$
    }
    return pentahoWarPath;
  }

  public String getPasswordServiceClass() {
    String passwordServiceClass = getPentahoObjectsConfig().getPasswordService();
    if ( StringUtils.isEmpty( passwordServiceClass ) ) {
      passwordServiceClass = DEFAULT_VALUE_PASSWORD_SERVICE_CLASS;
    }
    return passwordServiceClass;
  }

  public String getJdbcDriverPath() {
    return JDBC_DRIVER_PATH;
  }

  public String getHelpUrl() {
    String helpUrl = getConsoleConfig().getHelpUrl();
    if ( ( helpUrl == null ) || ( helpUrl.trim().length() == 0 ) ) {
      helpUrl = DEFAULT_HELP_URL;
    }
    return helpUrl;
  }

  IConsoleConfig getConsoleConfig() {
    if ( consoleConfig == null ) {
      try {
        consoleConfig = new ConsoleConfigXml( new File( ClassLoader.getSystemResource( CONFIG_FILE_NAME ).toURI() ) );
      } catch ( Exception ex ) {
        logger.warn( Messages.getErrorString( "AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", CONFIG_FILE_NAME ) ); //$NON-NLS-1$
        consoleConfig = new ConsoleConfigXml();
      }
    }
    return consoleConfig;
  }

  HibernateSettingsXml getHibernateSettingsXml() {
    if ( hibernateSettingXml == null ) {
      try {
        hibernateSettingXml = new HibernateSettingsXml( new File( getSolutionPath() + HIBERNATE_MANAGED_XML_PATH ) );
      } catch ( Exception e ) {
        hibernateSettingXml = new HibernateSettingsXml();
      }
    }
    return hibernateSettingXml;
  }

  PentahoObjectsConfig getPentahoObjectsConfig() {
    if ( pentahoObjectsConfig == null ) {
      try {
        pentahoObjectsConfig = new PentahoObjectsConfig( new File( getSolutionPath() + PENTAHO_OBJECTS_SPRING_XML ) );
      } catch ( Exception e ) {
        pentahoObjectsConfig = new PentahoObjectsConfig();
      }
    }
    return pentahoObjectsConfig;
  }

  SpringSecurityHibernateConfig getSpringSecurityHibernateConfig() {
    if ( springSecurityHibernateConfig == null ) {
      try {
        springSecurityHibernateConfig =
            new SpringSecurityHibernateConfig( new File( getSolutionPath() + SPRING_SECURITY_HIBERNATE_XML ) );
      } catch ( Exception e ) {
        logger.warn( Messages.getErrorString(
            "AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + SPRING_SECURITY_HIBERNATE_XML ) ); //$NON-NLS-1$
        springSecurityHibernateConfig = new SpringSecurityHibernateConfig();
      }
    }
    return springSecurityHibernateConfig;
  }

}
