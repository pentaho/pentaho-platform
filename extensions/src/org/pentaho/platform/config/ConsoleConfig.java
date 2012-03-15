package org.pentaho.platform.config;

import java.io.File;

import org.pentaho.platform.config.i18n.Messages;

public class ConsoleConfig {

  
  private static ConsoleConfig instance = new ConsoleConfig();
  private static final String HIBERNATE_CONFIG_PREFIX = "system/hibernate/";//$NON-NLS-1$

  public static final String CONFIG_FILE_PATH = "resource/config/console.xml";
  public static final String DEFAULT_XML_ENCODING = "UTF-8";
  
  private ConsoleConfig() {
  }

  public static ConsoleConfig getInstance() {
    return instance;
  }


  public File getConfigFile() {
    return new File(CONFIG_FILE_PATH);
  }
  
  public EnterpriseConsoleConfigXml getConfig() {
    EnterpriseConsoleConfigXml consoleConfig = null;
    try {
      consoleConfig = new EnterpriseConsoleConfigXml(new File(CONFIG_FILE_PATH));
    } catch (Exception ex) {
      consoleConfig = new EnterpriseConsoleConfigXml();
    }
    return consoleConfig;
  }
  
  public String getSettingsDirectoryPath() {
    return getConfig().getSolutionPath();
  }

  public String getTempDirectoryPath() {
    return getConfig().getTempDirectory();
  }

  public String getBackupDirectory() {
    return getConfig().getBackupDirectory();
  }

  public String getXmlEncoding() {
    String encoding = getConfig().getXmlEncoding();
    return encoding != null ? encoding : DEFAULT_XML_ENCODING;
  }
  
  public String getWebAppPath() {
    return getConfig().getWebAppPath();
  }

  public String getPlatformUsername() {
    return getConfig().getPlatformUserName(); 
  }

  public String getServerStatusCheckPeriod() {
    return Long.toString(getConfig().getServerStatusCheckPeriod());
  }

  public String getSolutionPath() {
    return getConfig().getSolutionPath();
  }
  
  public Double getMetricsExecutionLimit() {
    return getConfig().getMetricsExecutionLimit();
  }
  
  public String getPlatformUserName() {
    return getConfig().getPlatformUserName();
  }
  
  public AnalysisView getMetricsView() {
    return getConfig().getMetricsView();
  }
  
  public Double getMetricsInterval() {
    return getConfig().getMetricsInterval();
  }
  
  public String getSystemPath() {
    return getConfig().getSolutionPath() + File.separator + "system";
  }
  
  public File getSolutionsFolder() {
    return new File(getSolutionPath());
  }
  
  public File getSystemFolder() {
    return new File(getSolutionsFolder().getAbsoluteFile() + File.separator + "system"); //$NON-NLS-1$
  }
  
  public File getWebXmlFile() {
    File webXmlFile = null;
    String warPath = getWebAppPath(); 
    if (warPath != null) {
      webXmlFile = new File(warPath + File.separator + "WEB-INF" + File.separator + "web.xml"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return webXmlFile;
  }
  
  public File getPentahoXmlFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "pentaho.xml"); //$NON-NLS-1$
  }
  
  public File getGoogleMapsConfigFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "google" + File.separator //$NON-NLS-1$
        + "googlesettings.xml"); //$NON-NLS-1$
  }
  
  public File getTestManagerConfigFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "test-suite" //$NON-NLS-1$
        + File.separator + "test-settings.xml"); //$NON-NLS-1$
  }
  
  public PentahoObjectsConfig getPentahoObjectsConfig() {
    PentahoObjectsConfig config = null;
    try {
      config = new PentahoObjectsConfig(getPentahoObjectsConfigFile());
    } catch (Exception ex) {
      config = new PentahoObjectsConfig();
    }
    
    return config;
  }
  
  public File getPentahoObjectsConfigFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator
        + "pentahoObjects.spring.xml"); //$NON-NLS-1$
  }
  
  public File getSystemListenersConfigFile() { 
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "systemListeners.xml"); //$NON-NLS-1$
  }
  
  public File getPentahoSpringBeansConfigFile() throws Exception {
    WebXml webXml = null;
    try {
      webXml = new WebXml(getWebXmlFile());
    } catch (Exception e) {
      throw new Exception(Messages.getErrorString("PacProService.ERROR_0037_UNABLE_TO_READ_WEB_XML"), e);//$NON-NLS-1$
    }
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + webXml.getContextConfigFileName());
  }
  
  public PentahoXml getPentahoXmlConfig() {
    PentahoXml pentahoXml = null;
    try {
      pentahoXml = new PentahoXml(getPentahoXmlFile());
    } catch (Exception ex) {
      pentahoXml = new PentahoXml();
    }
    return pentahoXml;
  }
  public File getEmailConfigFile()   {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "smtp-email" + File.separator + "email_config.xml"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public File getPublisherConfigFile()   {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "publisher_config.xml"); //$NON-NLS-1$
  }

  public File getLdapPropertiesFile()  {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "applicationContext-security-ldap.properties"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public File getMondrianPropertiesFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "mondrian" + File.separator + "mondrian.properties"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public File getHibernateSettingsFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "hibernate" + File.separator + "hibernate-settings.xml"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public PdiConfigXml getPdiConfig() {
    PdiConfigXml pdiConfigXml = null;
    try {
      pdiConfigXml = new PdiConfigXml(getPdiSettingsFile());
    } catch (Exception ex) {
      pdiConfigXml = new PdiConfigXml();
    }
    return pdiConfigXml;
  }
  
  public File getPdiSettingsFile() {
    return new File(getSystemFolder().getAbsoluteFile() + File.separator + "kettle" + File.separator + "settings.xml"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public File getHibernateConfigFile() {
    HibernateSettings hibernateSettings = getHibernateSettings();
    return new File(getSolutionsFolder().getAbsoluteFile() + File.separator + hibernateSettings.getHibernateConfigFile());
  }

  public File getHibernateConfigFile(String hibernateConfigFile) {
    File file = null;
    file = new File(getSolutionsFolder().getAbsoluteFile() + File.separator + hibernateConfigFile);
    if(!file.exists()) {
      file = new File(getSolutionsFolder().getAbsoluteFile() + File.separator + HIBERNATE_CONFIG_PREFIX + hibernateConfigFile);
    }
    return file;
  }
  
  public LdapConfigProperties getLdapConfig() {
    LdapConfigProperties ldapConfig = null;
    try {
      ldapConfig = new LdapConfigProperties(getLdapPropertiesFile());
    } catch (Exception ex) {
      ldapConfig = new LdapConfigProperties();
    }
    
    return ldapConfig;
  }
  
  public MondrianConfig getMondrianConfig() {
    MondrianConfig mondrianConfig = null;
    try {
      MondrianConfigProperties mondrianConfigProperties = new MondrianConfigProperties(getMondrianPropertiesFile());
      
      // Convert the properties backed config. to a serializable config.
      mondrianConfig = new MondrianConfig(mondrianConfigProperties);
    } catch (Exception ex) {
      mondrianConfig = new MondrianConfig();
    }
    
    return mondrianConfig;
  }
  
  public HibernateSettings getHibernateSettings()  {
    HibernateSettingsXml hibernateSettingsXml = null;
    HibernateSettings hibernateSettings = null;
    
    try {
      hibernateSettingsXml = new HibernateSettingsXml(getHibernateSettingsFile());
      // Convert to a config that is not backed by an xml document to allow for serialization.
      hibernateSettings = new HibernateSettings(hibernateSettingsXml);

    } catch (Exception ex) {
      hibernateSettings = new HibernateSettings();
    }
    
    return hibernateSettings;
  }
}
