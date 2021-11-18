/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/21/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoObjectsConfigTest {

  @Mock Document document;
  @Mock Element beanElement;

  PentahoObjectsConfig pentahoObjectsConfig;
  PentahoObjectsConfig poc;

  String className;
  PentahoObjectsConfig.ScopeType scope;

  ArgumentCaptor<PentahoObjectsConfig.ObjectDescriptor> captor;

  @Before
  public void setUp() throws Exception {
    pentahoObjectsConfig = new PentahoObjectsConfig();
    poc = spy( pentahoObjectsConfig );
    poc.setDocument( document );

    when( document.addElement( "default:bean" ) ).thenReturn( beanElement );

    className = "org.pentaho.ClassToUse";
    scope = PentahoObjectsConfig.ScopeType.singleton;

    captor = ArgumentCaptor.forClass( PentahoObjectsConfig.ObjectDescriptor.class );
    doNothing().when( poc ).updateObject( nullable( String.class ), any( PentahoObjectsConfig.ObjectDescriptor.class ) );
    doReturn( className ).when( poc ).getObjectClassName( nullable( String.class ) );
  }

  private void verifySetter( String lookupId ) {
    verify( poc ).updateObject( eq( lookupId ), captor.capture() );

    PentahoObjectsConfig.ObjectDescriptor objectDescriptor = captor.getValue();
    assertEquals( className, objectDescriptor.getClassName() );
    assertEquals( scope, objectDescriptor.getScope() );
  }

  @Test
  public void testSetAclPublisher() throws Exception {
    poc.setAclPublisher( className, scope );
    verifySetter( PentahoObjectsConfig.ACL_PUBLISHER_ID );
  }

  @Test
  public void testGetAclPublisher() throws Exception {
    String result = poc.getAclPublisher();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.ACL_PUBLISHER_ID );
  }

  @Test
  public void testSetAclVoter() throws Exception {
    poc.setAclVoter( className, scope );
    verifySetter( PentahoObjectsConfig.ACL_VOTER_ID );
  }

  @Test
  public void testGetAclVoter() throws Exception {
    String result = poc.getAclVoter();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.ACL_VOTER_ID );
  }

  @Test
  public void testSetAuditFileEntry() throws Exception {
    poc.setAuditFileEntry( className, scope );
    verifySetter( PentahoObjectsConfig.AUDIT_FILE_ENTRY_ID );
  }

  @Test
  public void testGetAuditFileEntry() throws Exception {
    String result = poc.getAuditFileEntry();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.AUDIT_FILE_ENTRY_ID );
  }

  @Test
  public void testSetBackgroundExecutionHelper() throws Exception {
    poc.setBackgroundExecutionHelper( className, scope );
    verifySetter( PentahoObjectsConfig.BACKGROUND_EXECUTION_HELPER_ID );
  }

  @Test
  public void testGetBackgroundExecutionHelper() throws Exception {
    String result = poc.getBackgroundExecutionHelper();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.BACKGROUND_EXECUTION_HELPER_ID );
  }

  @Test
  public void testSetCacheManager() throws Exception {
    poc.setCacheManager( className, scope );
    verifySetter( PentahoObjectsConfig.CACHE_MGR_ID );
  }

  @Test
  public void testGetCacheManager() throws Exception {
    String result = poc.getCacheManager();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.CACHE_MGR_ID );
  }

  @Test
  public void testSetConditionalExecution() throws Exception {
    poc.setConditionalExecution( className, scope );
    verifySetter( PentahoObjectsConfig.CONDITONAL_EXECUTION_ID );
  }

  @Test
  public void testGetConditionalExecution() throws Exception {
    String result = poc.getConditionalExecution();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.CONDITONAL_EXECUTION_ID );
  }
  @Test
  public void testSetContentRepository() throws Exception {
    poc.setContentRepository( className, scope );
    verifySetter( PentahoObjectsConfig.CONTENT_REPOSITORY_ID );
  }

  @Test
  public void testGetContentRepository() throws Exception {
    String result = poc.getContentRepository();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.CONTENT_REPOSITORY_ID );
  }

  @Test
  public void testSetContentRepositoryOutputHandler() throws Exception {
    poc.setContentRepositoryOutputHandler( className, scope );
    verifySetter( PentahoObjectsConfig.CONTENT_REPOSITORY_OUTPUT_HANDLER_ID );
  }

  @Test
  public void testGetContentRepositoryOutputHandler() throws Exception {
    String result = poc.getContentRepositoryOutputHandler();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.CONTENT_REPOSITORY_OUTPUT_HANDLER_ID );
  }
  @Test
  public void testSetDataSource() throws Exception {
    poc.setDataSource( className, scope );
    verifySetter( PentahoObjectsConfig.DATA_SOURCE_ID );
  }

  @Test
  public void testGetDataSource() throws Exception {
    String result = poc.getDataSource();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.DATA_SOURCE_ID );
  }

  @Test
  public void testSetDataSourceService() throws Exception {
    poc.setDataSourceService( className, scope );
    verifySetter( PentahoObjectsConfig.DATA_SOURCE_SERVICE_ID );
  }

  @Test
  public void testGetDataSourceService() throws Exception {
    String result = poc.getDataSourceService();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.DATA_SOURCE_SERVICE_ID );
  }

  @Test
  public void testSetDataSourcMgmtService() throws Exception {
    poc.setDataSourcMgmtService( className, scope );
    verifySetter( PentahoObjectsConfig.DATA_SOURCE_MGMT_SERVICE_ID );
  }

  @Test
  public void testGetDataSourcMgmtService() throws Exception {
    String result = poc.getDataSourcMgmtService();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.DATA_SOURCE_MGMT_SERVICE_ID );
  }

  @Test
  public void testSetFileOutputHandler() throws Exception {
    poc.setFileOutputHandler( className, scope );
    verifySetter( PentahoObjectsConfig.FILE_OUTPUT_HANDLER_ID );
  }

  @Test
  public void testGetFileOutputHandler() throws Exception {
    String result = poc.getFileOutputHandler();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.FILE_OUTPUT_HANDLER_ID );
  }

  @Test
  public void testSetMessageFormatter() throws Exception {
    poc.setMessageFormatter( className, scope );
    verifySetter( PentahoObjectsConfig.MSG_FORMATTER_ID );
  }

  @Test
  public void testGetMessageFormatter() throws Exception {
    String result = poc.getMessageFormatter();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.MSG_FORMATTER_ID );
  }

  @Test
  public void testSetPasswordService() throws Exception {
    poc.setPasswordService( className, scope );
    verifySetter( PentahoObjectsConfig.PASSWORD_SERVICE_ID );
  }

  @Test
  public void testGetPasswordService() throws Exception {
    String result = poc.getPasswordService();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.PASSWORD_SERVICE_ID );
  }

  @Test
  public void testSetRuntimeRepository() throws Exception {
    poc.setRuntimeRepository( className, scope );
    verifySetter( PentahoObjectsConfig.RUNTIME_REPOSITORY_ID );
  }

  @Test
  public void testGetRuntimeRepository() throws Exception {
    String result = poc.getRuntimeRepository();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.RUNTIME_REPOSITORY_ID );
  }

  @Test
  public void testSetScheduler() throws Exception {
    poc.setScheduler( className, scope );
    verifySetter( PentahoObjectsConfig.SCHEDULER_ID );
  }

  @Test
  public void testGetScheduler() throws Exception {
    String result = poc.getScheduler();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.SCHEDULER_ID );
  }

  @Test
  public void testSetSolutionEngine() throws Exception {
    poc.setSolutionEngine( className, scope );
    verifySetter( PentahoObjectsConfig.SOLUTION_ENGINE_ID );
  }

  @Test
  public void testGetSolutionEngine() throws Exception {
    String result = poc.getSolutionEngine();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.SOLUTION_ENGINE_ID );
  }

  @Test
  public void testSetSubscriptionRepository() throws Exception {
    poc.setSubscriptionRepository( className, scope );
    verifySetter( PentahoObjectsConfig.SUBSCRIPTION_REPOSITORY_ID );
  }

  @Test
  public void testGetSubscriptionRepository() throws Exception {
    String result = poc.getSubscriptionRepository();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.SUBSCRIPTION_REPOSITORY_ID );
  }

  @Test
  public void testSetSubscriptionScheduler() throws Exception {
    poc.setSubscriptionScheduler( className, scope );
    verifySetter( PentahoObjectsConfig.SUBSCRIPTION_SCHEDULER_ID );
  }

  @Test
  public void testGetSubscriptionScheduler() throws Exception {
    String result = poc.getSubscriptionScheduler();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.SUBSCRIPTION_SCHEDULER_ID );
  }

  @Test
  public void testSetUiTemplater() throws Exception {
    poc.setUiTemplater( className, scope );
    verifySetter( PentahoObjectsConfig.UI_TEMPLATER_ID );
  }

  @Test
  public void testGetUiTemplater() throws Exception {
    String result = poc.getUiTemplater();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.UI_TEMPLATER_ID );
  }

  @Test
  public void testSetUserFilesComponent() throws Exception {
    poc.setUserFilesComponent( className, scope );
    verifySetter( PentahoObjectsConfig.USER_FILES_COMPONENT_ID );
  }

  @Test
  public void testGetUserFilesComponent() throws Exception {
    String result = poc.getUserFilesComponent();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.USER_FILES_COMPONENT_ID );
  }

  @Test
  public void testSetUserSettingsService() throws Exception {
    poc.setUserSettingsService( className, scope );
    verifySetter( PentahoObjectsConfig.USER_SETTINGS_SERVICE_ID );
  }

  @Test
  public void testGetUserSettingsService() throws Exception {
    String result = poc.getUserSettingsService();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.USER_SETTINGS_SERVICE_ID );
  }

  @Test
  public void testSetVersionHelper() throws Exception {
    poc.setVersionHelper( className, scope );
    verifySetter( PentahoObjectsConfig.VERSION_HELPER_ID );
  }

  @Test
  public void testGetVersionHelper() throws Exception {
    String result = poc.getVersionHelper();
    verify( poc ).getObjectClassName( PentahoObjectsConfig.VERSION_HELPER_ID );
  }

  @Test
  public void testGetObjectBeanElement() throws Exception {
    Element element = pentahoObjectsConfig.getObjectBeanElement( "hello" );
    assertNull( element );
  }

  @Test
  public void testSetObject() throws Exception {
    doReturn( null ).when( poc ).getObjectBeanElement( nullable( String.class ) );
    PentahoObjectsConfig.ObjectDescriptor descriptor = mock( PentahoObjectsConfig.ObjectDescriptor.class );
    when( descriptor.getScope() ).thenReturn( PentahoObjectsConfig.ScopeType.prototype );
    when( descriptor.getClassName() ).thenReturn( className );
    poc.setObject( "hello", descriptor );

    verify( document ).addElement( "default:bean" );
    verify( beanElement ).addAttribute( "id", "hello" );
    verify( beanElement ).addAttribute( "class", className );
    verify( beanElement ).addAttribute( "scope", PentahoObjectsConfig.ScopeType.prototype.name() );
  }

  @Test
  public void testUpdateObject_objectDoesNotExist() throws Exception {
    poc = spy( pentahoObjectsConfig );
    doReturn( null ).when( poc ).getObjectBeanElement( nullable( String.class ) );
    PentahoObjectsConfig.ObjectDescriptor descriptor = mock( PentahoObjectsConfig.ObjectDescriptor.class );

    doNothing().when( poc ).setObject( eq( "hello" ), eq( descriptor ) );

    poc.updateObject( "hello", descriptor );
    verify( poc ).setObject( eq( "hello" ), eq( descriptor ) );
  }

  @Test
  public void testUpdateObject_objectDoesExist() throws Exception {
    poc = spy( pentahoObjectsConfig );
    doReturn( beanElement ).when( poc ).getObjectBeanElement( nullable( String.class ) );
    PentahoObjectsConfig.ObjectDescriptor descriptor = mock( PentahoObjectsConfig.ObjectDescriptor.class );
    when( descriptor.getScope() ).thenReturn( PentahoObjectsConfig.ScopeType.prototype );
    when( descriptor.getClassName() ).thenReturn( className );

    poc.updateObject( "hello", descriptor );
    verify( poc, never() ).setObject( eq( "hello" ), eq( descriptor ) );
    verify( beanElement ).addAttribute( "class", className );
    verify( beanElement ).addAttribute( "scope", PentahoObjectsConfig.ScopeType.prototype.name() );
  }

  @Test ( expected = NullPointerException.class )
  public void testGetObjectClassName() throws Exception {
    pentahoObjectsConfig.setDocument( document );
    String className = pentahoObjectsConfig.getObjectClassName( "hello" );
  }

  @Test ( expected = NullPointerException.class )
  public void testGetObjectScope() throws Exception {
    pentahoObjectsConfig.setDocument( document );
    String scope = pentahoObjectsConfig.getObjectScope( "hello" );
  }

  @Test
  public void testGetObject() throws Exception {
    pentahoObjectsConfig.setDocument( document );
    assertEquals( document, pentahoObjectsConfig.getDocument() );
    when( this.document.selectSingleNode( nullable( String.class ) ) ).thenReturn( beanElement );
    when( beanElement.attributeValue( "class" ) ).thenReturn( "org.pentaho.TestClass" );
    when( beanElement.attributeValue( "scope" ) ).thenReturn( "singleton" );

    PentahoObjectsConfig.ObjectDescriptor result = pentahoObjectsConfig.getObject( "hello" );
    assertNotNull( result );
    assertEquals( "org.pentaho.TestClass", result.getClassName() );
    assertEquals( PentahoObjectsConfig.ScopeType.singleton, result.getScope() );
  }

  @Test
  public void testStringToScope() throws Exception {
    assertEquals( PentahoObjectsConfig.ScopeType.prototype, pentahoObjectsConfig.stringToScopeType( "prototype" ) );
    assertEquals( PentahoObjectsConfig.ScopeType.session, pentahoObjectsConfig.stringToScopeType( "session" ) );
    assertEquals( PentahoObjectsConfig.ScopeType.singleton, pentahoObjectsConfig.stringToScopeType( "singleton" ) );
    assertEquals( PentahoObjectsConfig.ScopeType.undefined, pentahoObjectsConfig.stringToScopeType( null ) );
    assertEquals( PentahoObjectsConfig.ScopeType.undefined, pentahoObjectsConfig.stringToScopeType( "xyz" ) );
    assertEquals( PentahoObjectsConfig.ScopeType.undefined, pentahoObjectsConfig.stringToScopeType( "other" ) );
  }
}
