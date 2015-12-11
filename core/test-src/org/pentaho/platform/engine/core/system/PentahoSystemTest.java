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
 * Copyright 2006 - 2015 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.invoke;
import static mockit.Deencapsulation.setField;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import mockit.Mock;
import mockit.MockUp;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.pentaho.platform.api.engine.IActionCompleteListener;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.actionsequence.ActionParameter;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.MessagesBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

//"@RunWith" very important for verify invocations
@RunWith( JMockit.class )
public class PentahoSystemTest extends Assert {

  private final static String DEF_USERNAME = "myuser";
  private final static IPentahoSession SESSION = new StandaloneSession( DEF_USERNAME );

  private final static String TEST_SYSTEM_PROPERTY_NAME = "testSystemPropertyName";
  private final static String TEST_XML_VALUE = "testXMLValue";
  private final static String TEST_PROPERTY_VALUE = "testPropertyValue";

  private final static String TEST_ERROR = "TestError";
  private final static String TEST_MESSAGE = "TestMessage";

  private static MockUp<MessagesBase> messagesBaseMockUp;

  private final Node testNode = mock( Node.class );

  private static class BackUpField<T> {

    private T oldValue;
    private String fieldName;
    private Class<?> cl;

    public BackUpField( Class<?> cl, String fieldName ) {
      this.cl = cl;
      this.fieldName = fieldName;
    }

    public void setValue( T fieldValue ) {
      oldValue = getField( cl, fieldName );
      setField( cl, fieldName, fieldValue );
    }

    public void recovery() {
      setField( cl, fieldName, oldValue );
    };
  }

  private static abstract class Mocker<MockedClass> {
    MockedClass mock;

    public Mocker( MockedClass mock ) {
      this.mock = mock;
    }

    abstract void execute( MockedClass mock );

    void setRule( Stubber stubber ) {
      execute( stubber.when( mock ) );
    }
  }

  public PentahoSystemTest() {
    mockPentahoXml();
  }

  private void mockPentahoXml() {
    final Node testNodeName = mock( Node.class );
    when( testNodeName.getText() ).thenReturn( TEST_SYSTEM_PROPERTY_NAME );
    when( testNode.selectSingleNode( eq( "@name" ) ) ).thenReturn( testNodeName );
    final Node testNodeImplementation = mock( Node.class );
    when( testNodeImplementation.getText() ).thenReturn( TEST_XML_VALUE );
    when( testNode.selectSingleNode( eq( "@implementation" ) ) ).thenReturn( testNodeImplementation );
  }

  private static MockUp<MessagesBase> getMessagesBaseMockUp() {
    return new MockUp<MessagesBase>() {

      @Mock
      public String getString( final String key ) {
        return key;
      }

      @Mock
      public String getString( final String key, final Object... params ) {
        return getString( key );
      }

      @Mock
      public String getErrorString( final String key ) {
        return getString( key );
      }

      @Mock
      public String getErrorString( final String key, final Object... params ) {
        return getString( key, params );
      }
    };
  }

  private static void tearDown( MockUp<?> mockUp ) {
    if ( mockUp != null ) {
      mockUp.tearDown();
    }
  }

  @BeforeClass
  public static void beforeClass() {
    messagesBaseMockUp = getMessagesBaseMockUp();
  }

  @AfterClass
  public static void afterClass() {
    tearDown( messagesBaseMockUp );
  }

  private static void shutdown() {
    PentahoSystem.shutdown();
    System.clearProperty( TEST_SYSTEM_PROPERTY_NAME );
  }

  private void expectedError_NotConfigured( final int timesP ) {
    new NonStrictExpectations( Logger.class ) {
      {
        Logger.warn( anyString, withSubstring( "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED" ) );
        times = timesP;
      }
    };
  }

  private void expectedError_26( final int timesP ) {
    new NonStrictExpectations( Logger.class ) {
      {
        Logger.debug( anyString, withSubstring( "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT" ),
            withInstanceOf( ObjectFactoryException.class ) );
        times = timesP;
      }
    };
  }

  /**
   * When there are settings in pentaho.xml, we should use it overwriting properties file
   */
  @Test
  public void initXMLFactoriesXMLTest() throws Exception {
    initXMLFactories( true );
    assertEquals( TEST_XML_VALUE, System.getProperty( TEST_SYSTEM_PROPERTY_NAME ) );
    shutdown();
  }

  /**
   * Use properties file when no settings in pentaho.xml
   */
  @Test
  public void initXMLFactoriesPropertiesTest() throws Exception {
    initXMLFactories( false );
    assertEquals( TEST_PROPERTY_VALUE, System.getProperty( TEST_SYSTEM_PROPERTY_NAME ) );
    shutdown();
  }

  @Test
  public void validateObjectFactoryErrorTest() throws Exception {
    new NonStrictExpectations( PentahoSystem.class ) {
      {
        invoke( PentahoSystem.class, "validateObjectFactory" );
        times = 1;
        result = new PentahoSystemException();
      }
    };
    try {
      // execute
      initXMLFactories( false );
      fail( "RuntimeException expected" );
    } catch ( Exception e ) {
      assertTrue( PentahoSystemException.class.isInstance( e.getCause() ) );
    }
    shutdown();
  }

  @Test
  public void notifySystemListenersOfStartupReturnErrorTest() throws Exception {
    new NonStrictExpectations( PentahoSystem.class ) {
      {
        invoke( PentahoSystem.class, "notifySystemListenersOfStartup", new Class<?>[] { IPentahoSession.class },
            withInstanceOf( IPentahoSession.class ) );
        times = 1;
        result = new Exception();
        PentahoSystem.addInitializationFailureMessage( anyInt, anyString );
        times = 1;
      }
    };
    new NonStrictExpectations( Logger.class ) {
      {
        Logger.error( anyString, anyString, withInstanceOf( PentahoSystemException.class ) );
        times = 1;
      }
    };
    List<IPentahoSystemListener> systemListeners = new ArrayList<>();
    systemListeners.add( mock( IPentahoSystemListener.class ) );
    PentahoSystem.setSystemListeners( systemListeners );
    try {
      // execute
      assertFalse( initXMLFactories( false ) );
      shutdown();
    } finally {
      PentahoSystem.setSystemListeners( null );
    }
  }

  @Test
  public void xmlFactoryErrorTest() throws Exception {
    new NonStrictExpectations( Logger.class ) {
      {
        Logger.error( anyString, "PentahoSystem.ERROR_0025_LOAD_XML_FACTORY_PROPERTIES_FAILED" );
        times = 1; // expected
      }
    };
    when( testNode.selectSingleNode( eq( "@name" ) ) ).thenReturn( null );
    initXMLFactories( true ); // execute
    shutdown();
    mockPentahoXml(); // recovery @name value
  }

  @Test
  public void systemSetPropertyErrorTest() throws Exception {
    new NonStrictExpectations( System.class ) {
      {
        System.setProperty( TEST_SYSTEM_PROPERTY_NAME, anyString );
        times = 1; // expected
        result = new IOException();
      }
    };

    initXMLFactories( false ); // execute
    shutdown();
  }

  private ISystemSettings mockFactoryImpl( List<?> list ) {
    final ISystemSettings settingsService = spy( new SimpleSystemSettings() );
    when( settingsService.getSystemSettings( eq( "xml-factories/factory-impl" ) ) ).thenReturn( list );
    return settingsService;
  }

  @SuppressWarnings( { "serial", "unchecked" } )
  private boolean initXMLFactories( boolean factoriesInPentahoXML ) throws Exception {
    ISystemSettings settingsService = null;
    if ( factoriesInPentahoXML ) {
      settingsService = mockFactoryImpl( Collections.singletonList( testNode ) );
    } else {
      settingsService = mockFactoryImpl( Collections.emptyList() );
    }
    PentahoSystem.setSystemSettingsService( settingsService );

    // mock java-system-properties.properties
    final IPentahoObjectFactory objectFactory = mock( IPentahoObjectFactory.class );
    final ISystemConfig systemConfig = mock( ISystemConfig.class );
    final IConfiguration configuration = mock( IConfiguration.class );
    when( configuration.getProperties() ).thenReturn( new Properties() {
      {
        setProperty( TEST_SYSTEM_PROPERTY_NAME, TEST_PROPERTY_VALUE );
      }
    } );
    when( systemConfig.getConfiguration( eq( PentahoSystem.JAVA_SYSTEM_PROPERTIES ) ) ).thenReturn( configuration );

    when( objectFactory.objectDefined( eq( ISystemConfig.class ) ) ).thenReturn( true );
    final IPentahoObjectReference<ISystemConfig> pentahoObjectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectReference.getObject() ).thenReturn( systemConfig );
    when( objectFactory.getObjectReferences( eq( ISystemConfig.class ), any( IPentahoSession.class ), any( Map.class ) ) )
        .thenReturn( Collections.singletonList( pentahoObjectReference ) );

    PentahoSystem.registerObjectFactory( objectFactory );

    return PentahoSystem.init(); // execute
  }

  @Test
  public void notifySystemListenersOfStartupErrorTest() throws Exception {
    List<IPentahoSystemListener> systemListeners = new ArrayList<>();
    IPentahoSystemListener listener = mock( IPentahoSystemListener.class );
    doReturn( false ).when( listener ).startup( any( IPentahoSession.class ) );
    systemListeners.add( listener );
    PentahoSystem.setSystemListeners( systemListeners );
    try {
      invoke( PentahoSystem.class, "notifySystemListenersOfStartup", SESSION ); // execute
      fail( "PentahoSystemException expected" );
    } catch ( Exception e ) {
      assertTrue( PentahoSystemException.class.isInstance( e.getCause() ) ); // expected
    } finally {
      PentahoSystem.setSystemListeners( null );
    }
  }

  @Test
  public void getAdditionalInitializationFailureMessagesTest() {
    Integer errorKey = PentahoSystem.SYSTEM_OBJECTS_FAILED;
    String messageKey = "USER_INITIALIZATION_SYSTEM_PUBLISHERS_FAILED";

    BackUpField<Messages> instanceField = new BackUpField<Messages>( Messages.class, "instance" );
    Messages messagesMock = mock( Messages.class );
    instanceField.setValue( messagesMock );
    try {
      List<String> resultErrors = new ArrayList<String>();

      doReturn( TEST_MESSAGE ).when( messagesMock ).getString( anyString(), ArrayUtils.EMPTY_OBJECT_ARRAY );
      // executes
      invoke( PentahoSystem.class, "addInitializationFailureMessage", errorKey, TEST_ERROR );
      invoke( PentahoSystem.class, "getFailureMessages", resultErrors, errorKey, messageKey,
          ArrayUtils.EMPTY_OBJECT_ARRAY );
      String resultError = StringUtils.join( resultErrors, ';' );
      // expected
      assertTrue( resultError.contains( TEST_ERROR ) );
      assertTrue( resultError.contains( TEST_MESSAGE ) );
    } finally {
      instanceField.recovery();
    }

    BackUpField<Integer> initializedStatusField = new BackUpField<Integer>( PentahoSystem.class, "initializedStatus" );
    initializedStatusField.setValue( 0 );
    try {
      setField( PentahoSystem.class, "initializedStatus", 0 );
      List<String> resultErrors = new ArrayList<String>();
      invoke( PentahoSystem.class, "getFailureMessages", resultErrors, errorKey, messageKey,
          ArrayUtils.EMPTY_OBJECT_ARRAY ); // execute
      assertTrue( resultErrors.isEmpty() ); // expected
    } finally {
      initializedStatusField.recovery();
    }
  }

  private void mock_getFailureMessages( final Integer failureBit ) {
    new NonStrictExpectations( PentahoSystem.class ) {
      {
        invoke( PentahoSystem.class, "getFailureMessages", withAny( List.class ), withEqual( failureBit ), anyString,
            withAny( Object[].class ) );
        times = 1;
      }
    };
  }

  @Test
  public void getInitializationFailureMessagesTest() {
    mock_getFailureMessages( PentahoSystem.SYSTEM_SETTINGS_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_PUBLISHERS_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_OBJECTS_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_AUDIT_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_LISTENERS_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_PENTAHOXML_FAILED );
    mock_getFailureMessages( PentahoSystem.SYSTEM_OTHER_FAILED );
    invoke( PentahoSystem.class, "getInitializationFailureMessages" );
  }

  @Test
  public void getNotDefinedObjectTest() {
    expectedError_NotConfigured( 2 );
    assertNull( PentahoSystem.get( this.getClass(), SESSION, null ) );
    assertTrue( PentahoSystem.getAll( this.getClass() ).isEmpty() );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void getObjectReferenceErrorsTest() throws ObjectFactoryException {
    BackUpField<AggregateObjectFactory> aggObjectFactoryField =
        new BackUpField<AggregateObjectFactory>( PentahoSystem.class, "aggObjectFactory" );
    AggregateObjectFactory factoryMock = mock( AggregateObjectFactory.class );
    aggObjectFactoryField.setValue( factoryMock );

    try {
      doReturn( true ).when( factoryMock ).objectDefined( this.getClass() );
      doThrow( new ObjectFactoryException() ).when( factoryMock ).getAll( any( Class.class ),
          any( IPentahoSession.class ), any( Map.class ) );
      expectedError_26( 1 ); // expected

      // execute
      PentahoSystem.getAll( this.getClass() );

      doReturn( false ).when( factoryMock ).objectDefined( this.getClass() );
      expectedError_26( 2 ); // expected

      // executes
      PentahoSystem.getObjectReference( this.getClass(), SESSION );
      PentahoSystem.getObjectReferences( this.getClass(), SESSION );

      doReturn( true ).when( factoryMock ).objectDefined( this.getClass() );
      doThrow( new ObjectFactoryException() ).when( factoryMock ).getObjectReference( any( Class.class ),
          any( IPentahoSession.class ), (Map<String, String>) isNull() );
      doThrow( new ObjectFactoryException() ).when( factoryMock ).getObjectReferences( any( Class.class ),
          any( IPentahoSession.class ), (Map<String, String>) isNull() );
      expectedError_26( 2 ); // expected

      // executes
      PentahoSystem.getObjectReference( this.getClass(), SESSION );
      PentahoSystem.getObjectReferences( this.getClass(), SESSION );
    } finally {
      aggObjectFactoryField.recovery();
    }
  }

  @Test
  public void moveParametersFromRuntimeContextToSessionTest() {
    String name = "testValue";
    String value = "value";
    Set<String> names = Collections.singleton( name );
    ActionParameter actionParameter = new ActionParameter( name, null, value, null, null );

    IRuntimeContext context = mock( IRuntimeContext.class );
    doReturn( names ).when( context ).getOutputNames();
    doReturn( actionParameter ).when( context ).getOutputParameter( name );

    try {
      // execute
      invoke( PentahoSystem.class, "moveParametersFromRuntimeContextToSession", context, SESSION );
      assertEquals( value, SESSION.getAttribute( name ) ); // expected
    } finally {
      SESSION.removeAttribute( name );
    }
  }

  @Test
  public void createParameterProviderMapTest() {
    IParameterProvider parameterProvider = mock( IParameterProvider.class );
    // execute
    Map<String, IParameterProvider> map =
        invoke( PentahoSystem.class, "createParameterProviderMap", SESSION, parameterProvider );
    assertEquals( parameterProvider, map.get( PentahoSystem.SCOPE_SESSION ) ); // expected

    // execute
    map = invoke( PentahoSystem.class, "createParameterProviderMap", SESSION, IParameterProvider.class );
    assertNotNull( map.get( PentahoSystem.SCOPE_SESSION ) ); // expected
  }

  @Test
  public void sessionStartupTest() throws Exception {
    List<String> roles = Collections.singletonList( "Role" );
    final IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    doReturn( roles ).when( userRoleListService ).getRolesForUser( null, DEF_USERNAME );
    final ISolutionEngine solutionEngine = mock( ISolutionEngine.class );

    final Map<String, IParameterProvider> parameterProviderMap = new HashMap<String, IParameterProvider>();

    new NonStrictExpectations( PentahoSystem.class ) {
      {
        PentahoSystem.get( IUserRoleListService.class ); // cached by SecurityHelper
        result = userRoleListService;

        PentahoSystem.get( ISolutionEngine.class, withInstanceOf( IPentahoSession.class ) );
        result = solutionEngine;

        invoke( PentahoSystem.class, "createParameterProviderMap", withAny( IPentahoSession.class ),
            withAny( IParameterProvider.class ) );
        result = parameterProviderMap;
      }
    };

    final String actionPath = "actionPath";
    ISessionStartupAction startupAction = new SessionStartupAction();
    startupAction.setActionPath( actionPath );
    startupAction.setActionOutputScope( PentahoSystem.SCOPE_SESSION );
    startupAction.setSessionType( UserSession.class.getName() );

    Callable<String> callable = new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "ok";
      }
    };

    IRuntimeContext contex = mock( IRuntimeContext.class );

    Mocker<ISolutionEngine> solutionEngineMocker = new Mocker<ISolutionEngine>( solutionEngine ) {
      @Override
      void execute( ISolutionEngine mock ) {
        mock.execute( eq( actionPath ), anyString(), anyBoolean(), anyBoolean(), anyString(), anyBoolean(),
            eq( parameterProviderMap ), any( IOutputHandler.class ), any( IActionCompleteListener.class ),
            any( IPentahoUrlFactory.class ), anyList() );
      }
    };

    try {
      PentahoSystem.setSessionStartupActions( Collections.singletonList( startupAction ) );
      solutionEngineMocker.setRule( doReturn( contex ) );

      // execute sessionStartup()
      SecurityHelper.getInstance().runAsUser( DEF_USERNAME, callable );
      verify( solutionEngine ).init( isA( UserSession.class ) ); // expected

      solutionEngineMocker.setRule( doThrow( new RuntimeException() ) );
      new NonStrictExpectations( Logger.class ) {
        {
          Logger.warn( anyString, "PentahoSystem.WARN_UNABLE_TO_EXECUTE_SESSION_ACTION",
              withInstanceOf( Throwable.class ) );
          times = 1; // expected
        }
      };

      // execute sessionStartup()
      SecurityHelper.getInstance().runAsUser( DEF_USERNAME, callable );
    } finally {
      PentahoSystem.setSessionStartupActions( null );
    }
  }

  @Test
  public void publishTest() {
    IPentahoPublisher publisher = mock( IPentahoPublisher.class );
    doAnswer( new Answer<String>() {
      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return ( (IPentahoSession) invocation.getArguments()[0] ).getName();
      }
    } ).when( publisher ).publish( any( IPentahoSession.class ), anyInt() );

    List<IPentahoPublisher> publishers = Collections.singletonList( publisher );

    BackUpField<List<IPentahoPublisher>> publishersField =
        new BackUpField<List<IPentahoPublisher>>( PentahoSystem.class, "administrationPlugins" );
    try {
      publishersField.setValue( publishers );

      String result = PentahoSystem.publish( SESSION, publisher.getClass().getName() ); // execute
      assertEquals( SESSION.getName(), result ); // expected

      result = PentahoSystem.publish( SESSION, "nonexistentClass" ); // execute
      assertEquals( "PentahoSystem.ERROR_0017_PUBLISHER_NOT_FOUND", result ); // expected
    } finally {
      publishersField.recovery();
    }
  }

  @Test
  public void getPublishersDocumentTest() {
    Document doc = PentahoSystem.getPublishersDocument(); // execute
    assertNotNull( doc );
    assertNotNull( doc.getRootElement() );
    assertEquals( "publishers", doc.getRootElement().getName() );

    final String publisherName = "publisherName";
    final String description = "description";
    IPentahoPublisher publisher = new IPentahoPublisher() {
      @Override
      public String publish( IPentahoSession paramIPentahoSession, int paramInt ) {
        return null;
      }

      @Override
      public String getName() {
        return publisherName;
      }

      @Override
      public String getDescription() {
        return description;
      }
    };
    List<IPentahoPublisher> publishers = Collections.singletonList( publisher );

    BackUpField<List<IPentahoPublisher>> publishersField =
        new BackUpField<List<IPentahoPublisher>>( PentahoSystem.class, "administrationPlugins" );
    try {
      publishersField.setValue( publishers );

      doc = PentahoSystem.getPublishersDocument(); // execute

      Element publisherElement = doc.getRootElement().element( "publisher" );
      assertNotNull( publisherElement );
      assertEquals( publisherName, publisherElement.element( "name" ).getText() );
      assertEquals( description, publisherElement.element( "description" ).getText() );
      assertEquals( publisher.getClass().getName(), publisherElement.element( "class" ).getText() );
    } finally {
      publishersField.recovery();
    }
  }

  @Test
  public void registerHostnameVerifierTest() {
    IApplicationContext contextBackUp = PentahoSystem.getApplicationContext();
    IApplicationContext appCtxt = new StandaloneApplicationContext( "FilePath", "" );
    try {
      PentahoSystem.setApplicationContext( appCtxt );

      invoke( PentahoSystem.class, "registerHostnameVerifier" ); // verifier didn't registered
      assertTrue( HttpsURLConnection.getDefaultHostnameVerifier().getClass().getSimpleName().equals(
          "DefaultHostnameVerifier" ) );
      assertFalse( HttpsURLConnection.getDefaultHostnameVerifier().verify( "any.com", null ) ); // default verifier

      new NonStrictExpectations( Logger.class ) {
        {
          Logger.warn( anyString, "PentahoSystem.ERROR_0030_VERIFIER_FAILED", withInstanceOf( Throwable.class ) );
          times = 2; // expected
        }
      };

      appCtxt.setFullyQualifiedServerURL( "MalformedURL" );
      invoke( PentahoSystem.class, "registerHostnameVerifier" ); // first exception
      HostnameVerifier verifier = HttpsURLConnection.getDefaultHostnameVerifier();
      assertTrue( verifier.verify( PentahoSystem.LOCALHOST, null ) ); // localhost verifier

      appCtxt.setFullyQualifiedServerURL( "http://any.com/any.html" );
      invoke( PentahoSystem.class, "registerHostnameVerifier" ); // verifier registered

      verifier = HttpsURLConnection.getDefaultHostnameVerifier();
      assertTrue( verifier.verify( "any.com", null ) ); // specific verifier
      assertTrue( verifier.verify( PentahoSystem.LOCALHOST, null ) );

      appCtxt = mock( IApplicationContext.class );
      doThrow( new RuntimeException() ).when( appCtxt ).getFullyQualifiedServerURL();
      PentahoSystem.setApplicationContext( appCtxt );
      invoke( PentahoSystem.class, "registerHostnameVerifier" ); // second exception
      verifier = HttpsURLConnection.getDefaultHostnameVerifier();
      assertTrue( verifier.verify( PentahoSystem.LOCALHOST, null ) ); // localhost verifier
    } finally {
      PentahoSystem.setApplicationContext( contextBackUp );
    }
  }

  @Test
  public void globalStartupTest() {
    List<String> roles = Collections.singletonList( "Role" );
    final IUserRoleListService userRoleListService = mock( IUserRoleListService.class );
    doReturn( roles ).when( userRoleListService ).getRolesForUser( null, DEF_USERNAME );

    new NonStrictExpectations( PentahoSystem.class ) {
      {
        PentahoSystem.globalStartup( withInstanceOf( IPentahoSession.class ) );
        times = 1; // expected

        PentahoSystem.get( IUserRoleListService.class );
        result = userRoleListService;

        PentahoSystem.get( String.class, "singleTenantAdminUserName", null );
        result = "Admin";
      }
    };

    ISessionStartupAction startupAction = new SessionStartupAction();
    startupAction.setActionOutputScope( PentahoSystem.SCOPE_GLOBAL );
    try {
      PentahoSystem.setSessionStartupActions( Collections.singletonList( startupAction ) );
      PentahoSystem.globalStartup(); // exception
    } finally {
      PentahoSystem.setSessionStartupActions( null );
    }
  }
}
