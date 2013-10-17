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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;
import org.pentaho.platform.config.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

public class PentahoObjectsConfig {
  private static final String DEFAULT_NAMESPACE = "http://www.springframework.org/schema/beans"; //$NON-NLS-1$
  private static final String DEFAULT = "default"; //$NON-NLS-1$
  private static final String ROOT = "beans"; //$NON-NLS-1$
  private static final String ROOT_ELEMENT = DEFAULT + ":beans"; //$NON-NLS-1$
  private static final String BEAN_ELEMENT = DEFAULT + ":bean"; //$NON-NLS-1$
  private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
  private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
  private static final String SCOPE_ATTRIBUTE = "scope"; //$NON-NLS-1$
  private static final String BEAN_ID_XPATH = ROOT_ELEMENT + "/" + BEAN_ELEMENT + "[@" + ID_ATTRIBUTE + "=\"{0}\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  public static final String SOLUTION_ENGINE_ID = "ISolutionEngine"; //$NON-NLS-1$
  public static final String CONTENT_REPOSITORY_ID = "IContentRepository"; //$NON-NLS-1$ 
  public static final String RUNTIME_REPOSITORY_ID = "IRuntimeRepository"; //$NON-NLS-1$
  public static final String AUDIT_FILE_ENTRY_ID = "IAuditEntry"; //$NON-NLS-1$
  public static final String UI_TEMPLATER_ID = "IUITemplater"; //$NON-NLS-1$
  public static final String USER_FILES_COMPONENT_ID = "IUserFilesComponent"; //$NON-NLS-1$
  public static final String BACKGROUND_EXECUTION_HELPER_ID = "IBackgroundExecution"; //$NON-NLS-1$
  public static final String SUBSCRIPTION_REPOSITORY_ID = "ISubscriptionRepository"; //$NON-NLS-1$ 
  public static final String SUBSCRIPTION_SCHEDULER_ID = "ISubscriptionScheduler"; //$NON-NLS-1$
  public static final String USER_SETTINGS_SERVICE_ID = "IUserSettingService"; //$NON-NLS-1$
  public static final String FILE_OUTPUT_HANDLER_ID = "file"; //$NON-NLS-1$
  public static final String CONTENT_REPOSITORY_OUTPUT_HANDLER_ID = "contentrepo"; //$NON-NLS-1$
  public static final String ACL_PUBLISHER_ID = "IAclPublisher"; //$NON-NLS-1$
  public static final String ACL_VOTER_ID = "IAclVoter"; //$NON-NLS-1$
  public static final String VERSION_HELPER_ID = "IVersionHelper"; //$NON-NLS-1$
  public static final String CACHE_MGR_ID = "ICacheManager"; //$NON-NLS-1$
  public static final String SCHEDULER_ID = "IScheduler"; //$NON-NLS-1$
  public static final String CONDITONAL_EXECUTION_ID = "IConditionalExecution"; //$NON-NLS-1$
  public static final String MSG_FORMATTER_ID = "IMessageFormatter"; //$NON-NLS-1$
  public static final String DATA_SOURCE_SERVICE_ID = "IDatasourceService"; //$NON-NLS-1$
  public static final String PASSWORD_SERVICE_ID = "IPasswordService"; //$NON-NLS-1$
  public static final String DATA_SOURCE_ID = "IDatasource"; //$NON-NLS-1$
  public static final String DATA_SOURCE_MGMT_SERVICE_ID = "IDatasourceMgmtService"; //$NON-NLS-1$
  public static final String PROTOTYPE = "prototype"; //$NON-NLS-1$
  public static final String SESSION = "session"; //$NON-NLS-1$
  public static final String SINGLETON = "singleton"; //$NON-NLS-1$

  public enum ScopeType {
    undefined, prototype, session, singleton
  }

  Document document;

  public PentahoObjectsConfig( File pentahoXmlFile ) throws IOException, DocumentException {
    setDocument( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, null ) );
  }

  public PentahoObjectsConfig( String xml ) throws DocumentException {
    SAXReader reader = new SAXReader();
    reader.setValidation( false );
    setDocument( reader.read( new ByteArrayInputStream( xml.getBytes() ) ) );
  }

  public void setDocument( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT ) ) {
      throw new DocumentException( Messages.getInstance().getErrorString(
          "PentahoObjectsConfig.ERROR_0001_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$
    }
    document = doc;
  }

  public PentahoObjectsConfig() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  public String getSolutionEngine() {
    return getObjectClassName( SOLUTION_ENGINE_ID );
  }

  public void setSolutionEngine( String solutionEngine, ScopeType scope ) {
    updateObject( SOLUTION_ENGINE_ID, new ObjectDescriptor( solutionEngine, scope ) );
  }

  public String getContentRepository() {
    return getObjectClassName( CONTENT_REPOSITORY_ID );
  }

  public void setContentRepository( String contentRepository, ScopeType scope ) {
    updateObject( CONTENT_REPOSITORY_ID, new ObjectDescriptor( contentRepository, scope ) );
  }

  public String getRuntimeRepository() {
    return getObjectClassName( RUNTIME_REPOSITORY_ID );
  }

  public void setRuntimeRepository( String runtimeRepository, ScopeType scope ) {
    updateObject( RUNTIME_REPOSITORY_ID, new ObjectDescriptor( runtimeRepository, scope ) );
  }

  public String getAuditFileEntry() {
    return getObjectClassName( AUDIT_FILE_ENTRY_ID );
  }

  public void setAuditFileEntry( String auditFileEntry, ScopeType scope ) {
    updateObject( AUDIT_FILE_ENTRY_ID, new ObjectDescriptor( auditFileEntry, scope ) );
  }

  public String getUiTemplater() {
    return getObjectClassName( UI_TEMPLATER_ID );
  }

  public void setUiTemplater( String uiTemplater, ScopeType scope ) {
    updateObject( UI_TEMPLATER_ID, new ObjectDescriptor( uiTemplater, scope ) );
  }

  public String getUserFilesComponent() {
    return getObjectClassName( USER_FILES_COMPONENT_ID );
  }

  public void setUserFilesComponent( String userFilesComponent, ScopeType scope ) {
    updateObject( USER_FILES_COMPONENT_ID, new ObjectDescriptor( userFilesComponent, scope ) );
  }

  public String getBackgroundExecutionHelper() {
    return getObjectClassName( BACKGROUND_EXECUTION_HELPER_ID );
  }

  public void setBackgroundExecutionHelper( String backgroundExecutionHelper, ScopeType scope ) {
    updateObject( BACKGROUND_EXECUTION_HELPER_ID, new ObjectDescriptor( backgroundExecutionHelper, scope ) );
  }

  public String getSubscriptionRepository() {
    return getObjectClassName( SUBSCRIPTION_REPOSITORY_ID );
  }

  public void setSubscriptionRepository( String subscriptionRepository, ScopeType scope ) {
    updateObject( SUBSCRIPTION_REPOSITORY_ID, new ObjectDescriptor( subscriptionRepository, scope ) );
  }

  public String getSubscriptionScheduler() {
    return getObjectClassName( SUBSCRIPTION_SCHEDULER_ID );
  }

  public void setSubscriptionScheduler( String subscriptionScheduler, ScopeType scope ) {
    updateObject( SUBSCRIPTION_SCHEDULER_ID, new ObjectDescriptor( subscriptionScheduler, scope ) );
  }

  public String getUserSettingsService() {
    return getObjectClassName( USER_SETTINGS_SERVICE_ID );
  }

  public void setUserSettingsService( String userSettingsService, ScopeType scope ) {
    updateObject( USER_SETTINGS_SERVICE_ID, new ObjectDescriptor( userSettingsService, scope ) );
  }

  public String getFileOutputHandler() {
    return getObjectClassName( FILE_OUTPUT_HANDLER_ID );
  }

  public void setFileOutputHandler( String fileOutputHandler, ScopeType scope ) {
    updateObject( FILE_OUTPUT_HANDLER_ID, new ObjectDescriptor( fileOutputHandler, scope ) );
  }

  public String getContentRepositoryOutputHandler() {
    return getObjectClassName( CONTENT_REPOSITORY_OUTPUT_HANDLER_ID );
  }

  public void setContentRepositoryOutputHandler( String contentRepositoryOutputHandler, ScopeType scope ) {
    updateObject( CONTENT_REPOSITORY_OUTPUT_HANDLER_ID, new ObjectDescriptor( contentRepositoryOutputHandler, scope ) );
  }

  public String getAclPublisher() {
    return getObjectClassName( ACL_PUBLISHER_ID );
  }

  public void setAclPublisher( String aclPublisher, ScopeType scope ) {
    updateObject( ACL_PUBLISHER_ID, new ObjectDescriptor( aclPublisher, scope ) );
  }

  public String getAclVoter() {
    return getObjectClassName( ACL_VOTER_ID );
  }

  public void setAclVoter( String aclVoter, ScopeType scope ) {
    updateObject( ACL_VOTER_ID, new ObjectDescriptor( aclVoter, scope ) );
  }

  public String getVersionHelper() {
    return getObjectClassName( VERSION_HELPER_ID );
  }

  public void setVersionHelper( String versionHelper, ScopeType scope ) {
    updateObject( VERSION_HELPER_ID, new ObjectDescriptor( versionHelper, scope ) );
  }

  public String getCacheManager() {
    return getObjectClassName( CACHE_MGR_ID );
  }

  public void setCacheManager( String cacheManager, ScopeType scope ) {
    updateObject( CACHE_MGR_ID, new ObjectDescriptor( cacheManager, scope ) );
  }

  public String getScheduler() {
    return getObjectClassName( SCHEDULER_ID );
  }

  public void setScheduler( String scheduler, ScopeType scope ) {
    updateObject( SCHEDULER_ID, new ObjectDescriptor( scheduler, scope ) );
  }

  public String getConditionalExecution() {
    return getObjectClassName( CONDITONAL_EXECUTION_ID );
  }

  public void setConditionalExecution( String conditionalExecution, ScopeType scope ) {
    updateObject( CONDITONAL_EXECUTION_ID, new ObjectDescriptor( conditionalExecution, scope ) );
  }

  public String getMessageFormatter() {
    return getObjectClassName( MSG_FORMATTER_ID );
  }

  public void setMessageFormatter( String messageFormatter, ScopeType scope ) {
    updateObject( MSG_FORMATTER_ID, new ObjectDescriptor( messageFormatter, scope ) );
  }

  public String getDataSourceService() {
    return getObjectClassName( DATA_SOURCE_SERVICE_ID );
  }

  public void setDataSourceService( String dataSourceService, ScopeType scope ) {
    updateObject( DATA_SOURCE_SERVICE_ID, new ObjectDescriptor( dataSourceService, scope ) );
  }

  public String getPasswordService() {
    return getObjectClassName( PASSWORD_SERVICE_ID );
  }

  public void setPasswordService( String passwordService, ScopeType scope ) {
    updateObject( PASSWORD_SERVICE_ID, new ObjectDescriptor( passwordService, scope ) );
  }

  public String getDataSource() {
    return getObjectClassName( DATA_SOURCE_ID );
  }

  public void setDataSource( String dataSource, ScopeType scope ) {
    updateObject( DATA_SOURCE_ID, new ObjectDescriptor( dataSource, scope ) );
  }

  public String getDataSourcMgmtService() {
    return getObjectClassName( DATA_SOURCE_MGMT_SERVICE_ID );
  }

  public void setDataSourcMgmtService( String dataSourcMgmtService, ScopeType scope ) {
    updateObject( DATA_SOURCE_MGMT_SERVICE_ID, new ObjectDescriptor( dataSourcMgmtService, scope ) );
  }

  protected Element getObjectBeanElement( String objectId ) {
    try {
      String xPath = MessageFormat.format( BEAN_ID_XPATH, objectId );
      HashMap<String, String> map = new HashMap<String, String>();
      map.put( "default", DEFAULT_NAMESPACE ); //$NON-NLS-1$
      Dom4jXPath xpath = new Dom4jXPath( xPath );
      xpath.setNamespaceContext( new SimpleNamespaceContext( map ) );
      Element element = (Element) xpath.selectSingleNode( document );
      return element;
    } catch ( JaxenException jex ) {
      return null;
    }
  }

  public void setObject( String objectId, ObjectDescriptor objectDescriptor ) {
    Element beanDefinitionElement = getObjectBeanElement( objectId );
    if ( objectDescriptor == null ) {
      if ( beanDefinitionElement != null ) {
        beanDefinitionElement.detach();
      }
    } else {
      if ( beanDefinitionElement == null ) {
        beanDefinitionElement = document.addElement( BEAN_ELEMENT );
        beanDefinitionElement.addAttribute( ID_ATTRIBUTE, objectId );
        beanDefinitionElement.addAttribute( CLASS_ATTRIBUTE, objectDescriptor.getClassName() );
        String scope = objectDescriptor.getScope().name();
        // If scope is not specified then use prototype scope as default
        if ( scope == null || scope.length() > 0 ) {
          scope = ScopeType.prototype.name();
        }
        beanDefinitionElement.addAttribute( SCOPE_ATTRIBUTE, scope );
      }
    }
  }

  public void updateObject( String objectId, ObjectDescriptor objectDescriptor ) {
    Element beanDefinitionElement = getObjectBeanElement( objectId );
    if ( beanDefinitionElement != null ) {
      String className = objectDescriptor.getClassName();
      ScopeType scope = objectDescriptor.getScope();
      if ( className != null && className.length() > 0 ) {
        beanDefinitionElement.addAttribute( CLASS_ATTRIBUTE, className );
      }
      if ( scope != null && scope.name() != null ) {
        beanDefinitionElement.addAttribute( SCOPE_ATTRIBUTE, scope.name() );
      }
    } else {
      setObject( objectId, objectDescriptor );
    }
  }

  public String getObjectClassName( String objectId ) {
    try {
      String xPath = MessageFormat.format( BEAN_ID_XPATH, objectId );
      HashMap<String, String> map = new HashMap<String, String>();
      map.put( "default", DEFAULT_NAMESPACE ); //$NON-NLS-1$
      Dom4jXPath xpath = new Dom4jXPath( xPath );
      xpath.setNamespaceContext( new SimpleNamespaceContext( map ) );
      Element element = (Element) xpath.selectSingleNode( document );
      return element.attributeValue( CLASS_ATTRIBUTE );
    } catch ( JaxenException jex ) {
      return null;
    }
  }

  public String getObjectScope( String objectId ) {
    try {
      HashMap<String, String> map = new HashMap<String, String>();
      map.put( "default", DEFAULT_NAMESPACE ); //$NON-NLS-1$
      Dom4jXPath xpath = new Dom4jXPath( BEAN_ID_XPATH );
      xpath.setNamespaceContext( new SimpleNamespaceContext( map ) );
      Element element = (Element) xpath.selectSingleNode( document );
      return element.attributeValue( SCOPE_ATTRIBUTE );
    } catch ( JaxenException jex ) {
      return null;
    }

  }

  public ObjectDescriptor getObject( String objectId ) {
    String xPathClass = MessageFormat.format( BEAN_ID_XPATH, objectId );
    Element elementClass = (Element) document.selectSingleNode( xPathClass );
    String xPathScope = MessageFormat.format( BEAN_ID_XPATH, objectId );
    Element elementScope = (Element) document.selectSingleNode( xPathScope );
    return new ObjectDescriptor( elementClass.attributeValue( CLASS_ATTRIBUTE ), stringToScopeType( elementScope
        .attributeValue( SCOPE_ATTRIBUTE ) ) );
  }

  public Document getDocument() {
    return document;
  }

  private class ObjectDescriptor {
    private String className;
    private ScopeType scope;

    public ObjectDescriptor() {

    }

    public ObjectDescriptor( String className, ScopeType scope ) {
      this.className = className;
      this.scope = scope;
    }

    public String getClassName() {
      return this.className;
    }

    public ScopeType getScope() {
      return this.scope;
    }
  }

  public ScopeType stringToScopeType( String scopeTypeString ) {
    if ( scopeTypeString == null ) {
      return ScopeType.undefined;
    }
    if ( scopeTypeString.equals( PROTOTYPE ) ) {
      return ScopeType.prototype;
    }
    if ( scopeTypeString.equals( SESSION ) ) {
      return ScopeType.session;
    }
    if ( scopeTypeString.equals( SINGLETON ) ) {
      return ScopeType.singleton;
    }
    return ScopeType.undefined;
  }
}
