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

package org.pentaho.platform.engine.core.system;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.PentahoSessionParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.objfac.AggregateObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.RuntimeObjectFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

public class PentahoSystem {

  public static final boolean debug = true;

  public static final boolean trace = false;

  public static final boolean ignored = false; // used to suppress compiler
  private static final String securityContextHolderStrategy =
    "org.pentaho.platform.engine.security.PentahoSecurityContextHolderStrategy";

  public static int loggingLevel = ILogger.ERROR;

  private static IApplicationContext applicationContext;

  protected static final String CONTENT_REPOSITORY = "IContentRepository"; //$NON-NLS-1$

  protected static final String RUNTIME_REPOSITORY = "IRuntimeRepository"; //$NON-NLS-1$

  private static final String SOLUTION_REPOSITORY = "IUnifiedRepository"; //$NON-NLS-1$

  protected static final String SOLUTION_ENGINE = "ISolutionEngine"; //$NON-NLS-1$

  public static final String BACKGROUND_EXECUTION = "IBackgroundExecution"; //$NON-NLS-1$

  // TODO: Read the Conditional Execution information from Pentaho XML.
  public static final String CONDITIONAL_EXECUTION = "IConditionalExecution"; //$NON-NLS-1$
  public static String DEFAULT_CONDITIONAL_EXECUTION_PROVIDER;
  public static String DEFAULT_MESSAGE_FORMATTER;

  // TODO: Read the Scheduler Class from Pentaho XML.
  public static final String SCHEDULER = "IScheduler"; //$NON-NLS-1$

  public static final String MESSAGE_FORMATTER = "IMessageFormatter"; //$NON-NLS-1$

  public static final String SCOPE_GLOBAL = "global"; //$NON-NLS-1$

  public static final String SCOPE_SESSION = "session"; //$NON-NLS-1$

  public static final String SCOPE_LOCAL = "local"; //$NON-NLS-1$

  public static final String SCOPE = "scope"; //$NON-NLS-1$

  public static final String PENTAHO_SESSION_KEY = "pentaho-session-context"; //$NON-NLS-1$

  private static Map globalAttributes;

  private static SimpleParameterProvider globalParameters;

  private static ISystemSettings systemSettingsService;

  private static List<IPentahoPublisher> administrationPlugins = new ArrayList<IPentahoPublisher>();

  private static List<IPentahoSystemListener> listeners = new ArrayList<IPentahoSystemListener>();

  private static List<ISessionStartupAction> sessionStartupActions = new ArrayList<ISessionStartupAction>();

  private static AggregateObjectFactory aggObjectFactory = new AggregateObjectFactory();

  private static RuntimeObjectFactory runtimeObjectFactory = new RuntimeObjectFactory();

  private static final Map initializationFailureDetailsMap = Collections.synchronizedMap( new HashMap() );

  private static final List<String> RequiredObjects = new ArrayList<String>();

  private static final List<String> KnownOptionalObjects = new ArrayList<String>();

  private static final List<String> IgnoredObjects = new ArrayList<String>();

  public static final int SYSTEM_NOT_INITIALIZED = -1;

  public static final int SYSTEM_INITIALIZED_OK = 0;

  public static final int SYSTEM_LISTENERS_FAILED = (int) Math.pow( 2, 0 ); // 1

  public static final int SYSTEM_OBJECTS_FAILED = (int) Math.pow( 2, 1 ); // 2

  public static final int SYSTEM_PUBLISHERS_FAILED = (int) Math.pow( 2, 2 ); // 4

  public static final int SYSTEM_AUDIT_FAILED = (int) Math.pow( 2, 3 ); // 8

  public static final int SYSTEM_PENTAHOXML_FAILED = (int) Math.pow( 2, 4 ); // 16

  public static final int SYSTEM_SETTINGS_FAILED = (int) Math.pow( 2, 5 ); // 32

  public static final int SYSTEM_OTHER_FAILED = (int) Math.pow( 2, 6 ); // 64

  private static int initializedStatus = PentahoSystem.SYSTEM_NOT_INITIALIZED;

  private static final String USERSETTING_SERVICE = "IUserSettingService"; //$NON-NLS-1$

  private static final String ACL_VOTER = "IAclVoter"; //$NON-NLS-1$

  private static final String CACHE_MANAGER = "ICacheManager"; //$NON-NLS-1$

  private static final List ACLFileExtensionList = new ArrayList();

  private static final List UnmodifiableACLFileExtensionList = UnmodifiableList
    .decorate( PentahoSystem.ACLFileExtensionList );

  private static final List logoutListeners = Collections.synchronizedList( new ArrayList() );

  // TODO even if logging is not configured messages need to make it out to
  // the console

  static {

    PentahoSystem.aggObjectFactory.registerObjectFactory( PentahoSystem.runtimeObjectFactory );

    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.SOLUTION_REPOSITORY );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.ACL_VOTER );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.CONDITIONAL_EXECUTION );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.RUNTIME_REPOSITORY );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.CACHE_MANAGER );

    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.CONTENT_REPOSITORY );
    PentahoSystem.KnownOptionalObjects.add( "IUITemplater" ); //$NON-NLS-1$
    PentahoSystem.KnownOptionalObjects.add( "IUserFilesComponent" ); //$NON-NLS-1$
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.BACKGROUND_EXECUTION );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.SCHEDULER );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.MESSAGE_FORMATTER );
    PentahoSystem.KnownOptionalObjects.add( PentahoSystem.USERSETTING_SERVICE );

    PentahoSystem.IgnoredObjects.add( "IAuditEntry" ); //$NON-NLS-1$
  }

  public static boolean init() {
    return PentahoSystem.init( new StandaloneApplicationContext( ".", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static boolean init( final IApplicationContext pApplicationContext ) {
    return PentahoSystem.init( pApplicationContext, null );
  }

  public static boolean init( final IApplicationContext pApplicationContext, final Map listenerMap ) {
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "PentahoSystem init start" ); //$NON-NLS-1$
    }
    PentahoSystem.initializedStatus = PentahoSystem.SYSTEM_INITIALIZED_OK;

    // PDI-3438 Scheduled job fails to open a transformation
    // Kettle jobs spawn threads which may require authentication to load transformations from
    // the kettle repository, by using the INHERITABLETHREADLOCAL strategy, spawned threads will
    // enjoy the same SecurityContext as their parent!
    SecurityContextHolder.setStrategyName( securityContextHolderStrategy );

    PentahoSystem.globalAttributes = Collections.synchronizedMap( new HashMap() );
    PentahoSystem.globalParameters = new SimpleParameterProvider( PentahoSystem.globalAttributes );

    PentahoSystem.applicationContext = pApplicationContext;

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Setting property path" ); //$NON-NLS-1$
    }
    System.setProperty( "pentaho.solutionpath", "solution:" ); //$NON-NLS-1$
    if ( LocaleHelper.getLocale() == null ) {
      LocaleHelper.setLocale( Locale.getDefault() );
    }

    if ( PentahoSystem.systemSettingsService != null ) {
      if ( debug ) {
        Logger.debug( PentahoSystem.class, "Reading ACL list from pentaho.xml" ); //$NON-NLS-1$
      }
      // Set Up ACL File Extensions by reading pentaho.xml for acl-files
      //
      // Read the files that are permitted to have ACLs on them from
      // the pentaho.xml.
      //
      String aclFiles = PentahoSystem.getSystemSetting( "acl-files", "xaction,url" ); //$NON-NLS-1$ //$NON-NLS-2$
      StringTokenizer st = new StringTokenizer( aclFiles, "," ); //$NON-NLS-1$
      String extn;
      while ( st.hasMoreElements() ) {
        extn = st.nextToken();
        if ( !extn.startsWith( "." ) ) { //$NON-NLS-1$
          extn = "." + extn; //$NON-NLS-1$
        }
        PentahoSystem.ACLFileExtensionList.add( extn );
      }
    }

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Initialize XML Factories" ); //$NON-NLS-1$
    }
    PentahoSystem.initXMLFactories();

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Set Logging Level from pentaho.xml setting" ); //$NON-NLS-1$
    }
    PentahoSystem.loggingLevel = ILogger.ERROR;
    if ( PentahoSystem.systemSettingsService != null ) {
      PentahoSystem.loggingLevel =
        Logger.getLogLevel(
          PentahoSystem.systemSettingsService.getSystemSetting( "log-level", "ERROR" ) ); //$NON-NLS-1$//$NON-NLS-2$
    }

    Logger.setLogLevel( PentahoSystem.loggingLevel );

    // to guarantee hostnames in SSL mode are not being spoofed
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Register host name verifier" ); //$NON-NLS-1$
    }
    PentahoSystem.registerHostnameVerifier();

    assert null != aggObjectFactory : "aggObjectFactory must be non-null"; //$NON-NLS-1$
    try {
      if ( debug ) {
        Logger.debug( PentahoSystem.class, "Validating object factory" ); //$NON-NLS-1$
      }
      PentahoSystem.validateObjectFactory();
    } catch ( PentahoSystemException e1 ) {
      throw new RuntimeException( e1 ); // this is fatal
    }

    // store a list of the system listeners
    try {
      if ( debug ) {
        Logger.debug( PentahoSystem.class, "Start System Listeners" ); //$NON-NLS-1$
      }
      PentahoSystem.notifySystemListenersOfStartup();
    } catch ( PentahoSystemException e ) {
      String msg = e.getLocalizedMessage();
      Logger.error( PentahoSystem.class.getName(), msg, e );
      PentahoSystem.initializedStatus |= PentahoSystem.SYSTEM_LISTENERS_FAILED;
      PentahoSystem.addInitializationFailureMessage( PentahoSystem.SYSTEM_LISTENERS_FAILED, msg );
      return false;
    }

    // once everything else is initialized, start global actions
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Global startup" ); //$NON-NLS-1$
    }
    PentahoSystem.globalStartup();

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "PentahoSystem Init Complete" ); //$NON-NLS-1$
    }
    return true;
  }

  private static void notifySystemListenersOfStartup() throws PentahoSystemException {

    if ( listeners == null || listeners.size() == 0 ) {
      // nothing to do
      return;
    }

    try {
      runAsSystem( new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          PentahoSystem.notifySystemListenersOfStartup( PentahoSessionHolder.getSession() );
          return null;
        }
      } );
    } catch ( Exception e ) {
      throw new PentahoSystemException( e );
    }

  }

  /**
   * Runs code as system with full privileges. <p/> <p> Unfortunate copy and paste from SecurityHelper due to
   * dependencies. </p>
   */
  private static <T> T runAsSystem( final Callable<T> callable ) throws Exception {

    final String name =
      StringUtils.defaultIfEmpty( PentahoSystem.get( String.class, "singleTenantAdminUserName", null ), "admin" );
    IPentahoSession origSession = PentahoSessionHolder.getSession();
    Authentication origAuth = SecurityContextHolder.getContext().getAuthentication();
    try {
      // create pentaho session
      StandaloneSession session = new StandaloneSession( name );
      session.setAuthenticated( name );
      // create authentication

      GrantedAuthority[] roles;

      ISystemSettings settings = PentahoSystem.getSystemSettings();
      String roleName = ( settings != null ) ? settings.getSystemSetting( "acl-voter/admin-role", "Admin" ) : "Admin";

      roles = new GrantedAuthority[ 1 ];
      roles[ 0 ] = new GrantedAuthorityImpl( roleName );

      User user = new User( name, "", true, true, true, true, roles );
      UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken( user, "", roles ); //$NON-NLS-1$


      // set holders
      PentahoSessionHolder.setSession( session );
      SecurityContextHolder.getContext().setAuthentication( auth );
      return callable.call();
    } finally {
      IPentahoSession sessionToDestroy = PentahoSessionHolder.getSession();
      if ( sessionToDestroy != null && sessionToDestroy != origSession ) {
        try {
          sessionToDestroy.destroy();
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
      PentahoSessionHolder.setSession( origSession );
      SecurityContextHolder.getContext().setAuthentication( origAuth );
    }
  }

  private static void notifySystemListenersOfStartup( final IPentahoSession session ) throws PentahoSystemException {
    if ( listeners != null && listeners.size() > 0 ) {

      for ( final IPentahoSystemListener systemListener : listeners ) {
        try {
          // ensure that the Authentication/IPentahoSession is correct between ISystemListeners
          runAsSystem( new Callable<Void>() {
            @Override public Void call() throws Exception {
              PentahoSystem.systemEntryPoint(); // make sure all startups occur in the context of a transaction

              try {
                if ( debug ) {
                  Logger.debug( PentahoSystem.class,
                    "System Listener Start: " + systemListener.getClass().getName() ); //$NON-NLS-1$
                }
                if ( !systemListener.startup( session ) ) {
                  throw new PentahoSystemException( Messages.getInstance().getErrorString(
                    "PentahoSystem.ERROR_0014_STARTUP_FAILURE", systemListener.getClass().getName() ) ); //$NON-NLS-1$
                }
                if ( debug ) {
                  Logger.debug( PentahoSystem.class,
                    "System Listener Complete: " + systemListener.getClass().getName() ); //$NON-NLS-1$
                }
              } catch ( Throwable e ) {
                throw new PentahoSystemException( Messages.getInstance().getErrorString(
                  "PentahoSystem.ERROR_0014_STARTUP_FAILURE", systemListener.getClass().getName() ), e ); //$NON-NLS-1$
              } finally {
                PentahoSystem.systemExitPoint(); // commit transaction
              }
              return null;
            }
          } );
        } catch ( Exception e ) {
          if ( e instanceof PentahoSystemException ) {
            throw (PentahoSystemException) e;
          } else {
            throw new PentahoSystemException( e );
          }
        }

      }
    }
  }

  /**
   * Using data in the systemSettings (this data typically originates in the pentaho.xml file), initialize 3 System
   * properties to explicitly identify the Transformer, SAX, and DOM factory implementations. (i.e. Crimson, Xerces,
   * Xalan, Saxon, etc.)
   * <p/>
   * For background on the purpose of this method, take a look at the notes/URLs below:
   * <p/>
   * Java[tm] API for XML Processing (JAXP):Frequently Asked Questions http://java.sun
   * .com/webservices/jaxp/reference/faqs/index.html
   * <p/>
   * Plugging in a Transformer and XML parser http://xml.apache.org/xalan-j/usagepatterns.html#plug
   * <p/>
   * http://marc2.theaimsgroup.com/?l=xml-apache-general&m=101344910514822&w=2 Q. How do I use a different JAXP
   * compatible implementation?
   * <p/>
   * The JAXP 1.1 API allows applications to plug in different JAXP compatible implementations of parsers or XSLT
   * processors. For example, when an application wants to create a new JAXP DocumentBuilderFactory instance, it calls
   * the staic method DocumentBuilderFactory.newInstance(). This causes a search for the name of a concrete subclass of
   * DocumentBuilderFactory using the following order: - The value of a system property like
   * javax.xml.parsers.DocumentBuilderFactory if it exists and is accessible. - The contents of the file
   * $JAVA_HOME/jre/lib/jaxp.properties if it exists. - The Jar Service Provider mechanism specified in the Jar File
   * Specification. A jar file can have a resource (i.e. an embedded file) such as
   * META-INF/javax/xml/parsers/DocumentBuilderFactory containing the name of the concrete class to instantiate. - The
   * fallback platform default implementation.
   * <p/>
   * Of the above ways to specify an implementation, perhaps the most useful is the jar service provider mechanism. To
   * use this mechanism, place the implementation jar file on your classpath. For example, to use Xerces 1.4.4 instead
   * of the version of Crimson which is bundled with JDK 1.4 (Java Development Kit version 1.4), place xerces.jar in
   * your classpath. This mechanism also works with older versions of the JDK which do not bundle JAXP. If you are using
   * JDK 1.4 and above, see the following question for potential problems. see http://java.sun.com/j2se/1
   * .3/docs/guide/jar/jar.html#Service%20Provider
   */
  private static void initXMLFactories() {
    // assert systemSettings != null : "systemSettings property must be set
    // before calling initXMLFactories.";

    if ( PentahoSystem.systemSettingsService != null ) {
      String xpathToXMLFactoryNodes = "xml-factories/factory-impl"; //$NON-NLS-1$
      List nds = PentahoSystem.systemSettingsService.getSystemSettings( xpathToXMLFactoryNodes );
      if ( null != nds ) {
        for ( Iterator it = nds.iterator(); it.hasNext(); ) {
          Node nd = (Node) it.next();
          Node nameAttr = nd.selectSingleNode( "@name" ); //$NON-NLS-1$
          Node implAttr = nd.selectSingleNode( "@implementation" ); //$NON-NLS-1$
          if ( ( null != nameAttr ) && ( null != implAttr ) ) {
            String name = nameAttr.getText();
            String impl = implAttr.getText();
            System.setProperty( name, impl );
          } else {
            Logger.error( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
              "PentahoSystem.ERROR_0025_LOAD_XML_FACTORY_PROPERTIES_FAILED", //$NON-NLS-1$
              xpathToXMLFactoryNodes ) );
          }
        }
      }
    }
  }

  public static boolean getInitializedOK() {
    return PentahoSystem.initializedStatus == PentahoSystem.SYSTEM_INITIALIZED_OK;
  }

  public static int getInitializedStatus() {
    return PentahoSystem.initializedStatus;
  }

  private static List<String> getAdditionalInitializationFailureMessages( final int failureBit ) {
    List<String> l = (List) PentahoSystem.initializationFailureDetailsMap.get( new Integer( failureBit ) );
    return l;
  }

  public static List<String> getInitializationFailureMessages() {
    List<String> rtn = new ArrayList<String>();
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_SETTINGS_FAILED ) ) {
      rtn.add( Messages.getInstance().getString(
        "PentahoSystem.USER_INITIALIZATION_SYSTEM_SETTINGS_FAILED",
        PathBasedSystemSettings.SYSTEM_CFG_PATH_KEY ) ); //$NON-NLS-1$
      List<String> l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_SETTINGS_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_PUBLISHERS_FAILED ) ) {
      rtn.add( Messages.getInstance()
        .getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_PUBLISHERS_FAILED" ) ); //$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_PUBLISHERS_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_OBJECTS_FAILED ) ) {
      rtn.add(
        Messages.getInstance().getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_OBJECTS_FAILED" ) ); //$NON-NLS-1$
      List<String> l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_OBJECTS_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_AUDIT_FAILED ) ) {
      rtn.add(
        Messages.getInstance().getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_AUDIT_FAILED" ) ); //$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_AUDIT_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_LISTENERS_FAILED ) ) {
      rtn.add(
        Messages.getInstance().getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_LISTENERS_FAILED" ) ); //$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_LISTENERS_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_PENTAHOXML_FAILED ) ) {
      rtn.add( Messages.getInstance()
        .getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_PENTAHOXML_FAILED" ) ); //$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_PENTAHOXML_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    if ( PentahoSystem.hasFailed( PentahoSystem.SYSTEM_OTHER_FAILED ) ) {
      rtn.add(
        Messages.getInstance().getString( "PentahoSystem.USER_INITIALIZATION_SYSTEM_OTHER_FAILED" ) ); //$NON-NLS-1$
      List l = PentahoSystem.getAdditionalInitializationFailureMessages( PentahoSystem.SYSTEM_OTHER_FAILED );
      if ( l != null ) {
        rtn.addAll( l );
      }
    }
    return rtn;
  }

  public static void addInitializationFailureMessage( final int failureBit, final String message ) {
    Integer i = new Integer( failureBit );
    List l = (List) PentahoSystem.initializationFailureDetailsMap.get( i );
    if ( l == null ) {
      l = Collections.synchronizedList( new ArrayList() );
      PentahoSystem.initializationFailureDetailsMap.put( i, l );
    }
    final String msg = "&nbsp;&nbsp;&nbsp;" + message; //$NON-NLS-1$
    if ( !l.contains( msg ) ) {
      l.add( msg );
    }
  }

  private static final boolean hasFailed( final int errorToCheck ) {
    return ( ( PentahoSystem.initializedStatus & errorToCheck ) == errorToCheck );
  }

  // TODO: is this method needed? See if we can use the factory directly and delete this method.
  public static IContentOutputHandler getOutputDestinationFromContentRef( final String contentTag,
                                                                          final IPentahoSession session ) {

    int pos = contentTag.indexOf( ':' );
    if ( pos == -1 ) {
      Logger.error( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
        "PentahoSystem.ERROR_0029_OUTPUT_HANDLER_NOT_SPECIFIED", contentTag ) ); //$NON-NLS-1$
      return null;
    }
    String handlerId = contentTag.substring( 0, pos );
    String contentRef = contentTag.substring( pos + 1 );
    IContentOutputHandler output = PentahoSystem.get( IContentOutputHandler.class, handlerId, session );
    if ( output != null ) {
      output.setHandlerId( handlerId );
      output.setSession( session );
      output.setContentRef( contentRef );
    }
    return output;
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory. Looks up an object by using the
   * name of the <code>interfaceClass</code> as the object key in {@link PentahoSystem#get(Class, String,
   * IPentahoSession)}. NOTE: session will be derived for you by using PentahoSessionHolder, so a session must already
   * have been bound to the thread local in PentahoSessionHolder in order for you to be able to access session-bound
   * objects.
   */
  public static <T> T get( Class<T> interfaceClass ) {
    return get( interfaceClass, null, PentahoSessionHolder.getSession() );
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory. Looks up an object by using the
   * name of the <code>interfaceClass</code> as the object key in {@link PentahoSystem#get(Class, IPentahoSession, Map)}
   * . NOTE: session will be derived for you by using PentahoSessionHolder, so a session must already have been bound to
   * the thread local in PentahoSessionHolder in order for you to be able to access session-bound objects.
   */
  public static <T> T
  get( Class<T> interfaceClass, final IPentahoSession session, final Map<String, String> properties ) {
    try {
      if ( !aggObjectFactory.objectDefined( interfaceClass ) ) {
        // this may not be a failure case, but we should log a warning in case the object is truly required
        Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED", interfaceClass.getSimpleName() ) ); //$NON-NLS-1$
        return null;
      }
      IPentahoSession curSession = ( session == null ) ? PentahoSessionHolder.getSession() : session;
      return aggObjectFactory.get( interfaceClass, curSession, properties );
    } catch ( ObjectFactoryException e ) {
      // something went wrong, we need to log this
      Logger.debug( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT", interfaceClass.getSimpleName() ),
        e ); //$NON-NLS-1$
      // for backwards compatibility: callers expect a null return even in an error case
      return null;
    }
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory. Looks up an object by using the
   * name of the <code>interfaceClass</code> as the object key in {@link PentahoSystem#getAll(Class, IPentahoSession)}.
   * NOTE: session will be derived for you by using PentahoSessionHolder, so a session must already have been bound to
   * the thread local in PentahoSessionHolder in order for you to be able to access session-bound objects.
   */
  public static <T> List<T> getAll( Class<T> interfaceClass ) {
    return getAll( interfaceClass, PentahoSessionHolder.getSession(), null );
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory. Looks up an object by using the
   * name of the <code>interfaceClass</code> as the object key in {@link PentahoSystem#getAll(Class, IPentahoSession)}.
   * NOTE: session will be derived for you by using PentahoSessionHolder, so a session must already have been bound to
   * the thread local in PentahoSessionHolder in order for you to be able to access session-bound objects.
   */
  public static <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession session ) {
    return getAll( interfaceClass, session, null );
  }

  /**
   * Retrieves objects from the object factory registered to the PentahoSystem implementing or extending the given Class
   * literal
   */
  public static <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession session, Map<String, String> props ) {
    try {
      if ( !aggObjectFactory.objectDefined( interfaceClass ) ) {
        // this may not be a failure case, but we should log a warning in case the object is truly required
        Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED", interfaceClass.getSimpleName() ) ); //$NON-NLS-1$
        return Collections.emptyList();
      }
      IPentahoSession curSession = ( session == null ) ? PentahoSessionHolder.getSession() : session;
      return aggObjectFactory.getAll( interfaceClass, curSession, props );
    } catch ( ObjectFactoryException e ) {
      // something went wrong, we need to log this
      Logger.debug( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT", interfaceClass.getSimpleName() ),
        e ); //$NON-NLS-1$
      // for backwards compatibility: callers expect a null return even in an error case
      return null;
    }
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory implementing or extending the
   * given Class literal
   * <p/>
   * {@link PentahoSystem#get(Class, String, IPentahoSession)}.
   */
  public static <T> T get( Class<T> interfaceClass, final IPentahoSession session ) {
    IPentahoSession curSession = ( session == null ) ? PentahoSessionHolder.getSession() : session;
    return get( interfaceClass, null, curSession );
  }

  /**
   * A convenience method for retrieving Pentaho system objects from the object factory. Returns an instance of a
   * configured object of the Pentaho system. This method will return <code>null</code> if the object could not be
   * retrieved for any reason. If the object is defined but for some reason can not be retrieved, an error message will
   * be logged.
   *
   * @return An instance of the requested object or <code>null</code> if either the object was not configured or it was
   * configured but there was a problem retrieving it.
   * @see PentahoSystem#getObjectFactory()
   * @see IPentahoObjectFactory#get(Class, String, IPentahoSession)
   */
  public static <T> T get( Class<T> interfaceClass, String key, final IPentahoSession session ) {
    try {
      IPentahoSession curSession = ( session == null ) ? PentahoSessionHolder.getSession() : session;

      if ( key == null ) { // some people are calling this method with null
        return aggObjectFactory.get( interfaceClass, curSession );
      }
      if ( !aggObjectFactory.objectDefined( key ) && !aggObjectFactory.objectDefined( interfaceClass ) ) {
        // this may not be a failure case, but we should log a warning in case the object is truly required
        Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED", key ) ); //$NON-NLS-1$
        return null;
      }

      return aggObjectFactory.get( interfaceClass, key, curSession );
    } catch ( ObjectFactoryException e ) {
      // something went wrong, we need to log this
      Logger.debug( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
        "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT", key ), e ); //$NON-NLS-1$
      // for backwards compatibility: callers expect a null return even in an error case
      return null;
    }
  }

  /**
   * Returns an IPentahoObjectReference for the requested Object containing registered Object Properties.
   *
   * @param interfaceClass Interface or Class literal for which implementations of will be found
   * @param curSession     current session to be used for session-based implementations
   * @return IPentahoObjectReference for the matching Object or null if no Object is found
   */
  public static <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass,
                                                                   IPentahoSession curSession ) {
    return getObjectReference( interfaceClass, curSession, null );
  }

  /**
   * Returns an IPentahoObjectReference for the requested Object containing registered Object Properties.
   *
   * @param interfaceClass Interface or Class literal for which implementations of will be found
   * @param curSession     current session to be used for session-based implementations
   * @param properties     Map of properties to filter matches in the ObjectFactory by
   * @return IPentahoObjectReference for the matching Object or null if no Object is found
   */
  public static <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
                                                                   Map<String, String> properties ) {

    if ( !aggObjectFactory.objectDefined( interfaceClass ) ) {
      // this may not be a failure case, but we should log a warning in case the object is truly required
      Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
        "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED", interfaceClass.getSimpleName() ) ); //$NON-NLS-1$
      return null;
    }
    curSession = ( curSession == null ) ? PentahoSessionHolder.getSession() : curSession;
    try {
      return aggObjectFactory.getObjectReference( interfaceClass, curSession, properties );
    } catch ( ObjectFactoryException e ) {

      Logger.debug( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT", interfaceClass.getSimpleName() ),
        e ); //$NON-NLS-1$
      return null;
    }

  }

  public static <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass,
                                                                          IPentahoSession curSession ) {
    return getObjectReferences( interfaceClass, curSession, null );
  }

  public static <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass,
                                                                          IPentahoSession curSession,
                                                                          Map<String, String> properties ) {

    if ( !aggObjectFactory.objectDefined( interfaceClass ) ) {
      // this may not be a failure case, but we should log a warning in case the object is truly required
      Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
        "PentahoSystem.WARN_OBJECT_NOT_CONFIGURED", interfaceClass.getSimpleName() ) ); //$NON-NLS-1$
      return Collections.emptyList();
    }
    curSession = ( curSession == null ) ? PentahoSessionHolder.getSession() : curSession;
    try {
      return aggObjectFactory.getObjectReferences( interfaceClass, curSession, properties );
    } catch ( ObjectFactoryException e ) {

      Logger.debug( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0026_COULD_NOT_RETRIEVE_CONFIGURED_OBJECT", interfaceClass.getSimpleName() ),
        e ); //$NON-NLS-1$
      return Collections.emptyList();
    }

  }

  public static String getSystemName() {
    return Messages.getInstance().getString( "PentahoSystem.USER_SYSTEM_TITLE" ); //$NON-NLS-1$;
  }

  public static IParameterProvider getGlobalParameters() {
    return PentahoSystem.globalParameters;
  }

  public static void sessionStartup( final IPentahoSession session ) {
    PentahoSystem.sessionStartup( session, null );
  }

  public static void clearGlobals() {
    PentahoSystem.globalAttributes.clear();
  }

  public static Object putInGlobalAttributesMap( final Object key, final Object value ) {
    return PentahoSystem.globalAttributes.put( key, value );
  }

  public static Object removeFromGlobalAttributesMap( final Object key ) {
    return PentahoSystem.globalAttributes.remove( key );
  }

  public static void sessionStartup( final IPentahoSession session, IParameterProvider sessionParameters ) {

    List<ISessionStartupAction> sessionStartupActions =
      PentahoSystem.getSessionStartupActionsForType( session.getClass().getName() );
    if ( sessionStartupActions == null ) {
      // nothing to do...
      return;
    }

    if ( !session.isAuthenticated() ) {
      return;
    }

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Process session startup actions" ); //$NON-NLS-1$
    }
    // TODO this needs more validation
    if ( sessionStartupActions != null ) {
      for ( ISessionStartupAction sessionStartupAction : sessionStartupActions ) {
        // parse the actionStr out to identify an action
        // now execute the action...

        SimpleOutputHandler outputHandler = null;

        String instanceId = null;

        ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
        solutionEngine.setLoggingLevel( PentahoSystem.loggingLevel );
        solutionEngine.init( session );

        String baseUrl = ""; //$NON-NLS-1$
        HashMap parameterProviderMap = new HashMap();
        if ( sessionParameters == null ) {
          sessionParameters = new PentahoSessionParameterProvider( session );
        }

        parameterProviderMap.put( SCOPE_SESSION, sessionParameters );

        IPentahoUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

        ArrayList messages = new ArrayList();

        IRuntimeContext context = null;
        try {
          context =
            solutionEngine
              .execute(
                sessionStartupAction.getActionPath(),
                "Session startup actions", false, true, instanceId, false, parameterProviderMap, outputHandler, null,
                urlFactory, messages ); //$NON-NLS-1$

          // if context is null, then we cannot check the status
          if ( null == context ) {
            return;
          }

          if ( context.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
            // now grab any outputs
            Iterator outputNameIterator = context.getOutputNames().iterator();
            while ( outputNameIterator.hasNext() ) {

              String attributeName = (String) outputNameIterator.next();
              IActionParameter output = context.getOutputParameter( attributeName );

              Object data = output.getValue();
              if ( data != null ) {
                session.removeAttribute( attributeName );
                session.setAttribute( attributeName, data );
              }
            }
          }
        } catch ( Throwable th ) {
          Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getString(
            "PentahoSystem.WARN_UNABLE_TO_EXECUTE_SESSION_ACTION", th.getLocalizedMessage() ), th ); //$NON-NLS-1$
        } finally {
          if ( context != null ) {
            context.dispose();
          }
        }
      }
    }
  }

  public static void globalStartup() {

    List<ISessionStartupAction> globalStartupActions = PentahoSystem.getGlobalStartupActions();
    if ( globalStartupActions == null || globalStartupActions.size() == 0 ) {
      // nothing to do...
      return;
    }

    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Process global startup actions" ); //$NON-NLS-1$
    }

    try {
      SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          PentahoSystem.globalStartup( PentahoSessionHolder.getSession() );
          return null;
        }
      } );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  public static void globalStartup( final IPentahoSession session ) {
    // getGlobalStartupActions doesn't pay any attention to session class
    List<ISessionStartupAction> globalStartupActions = PentahoSystem.getGlobalStartupActions();
    if ( globalStartupActions == null ) {
      // nothing to do...
      return;
    }

    boolean doGlobals = PentahoSystem.globalAttributes.size() == 0;
    // see if this has been done already
    if ( !doGlobals ) {
      return;
    }

    if ( globalStartupActions != null ) {
      for ( ISessionStartupAction globalStartupAction : globalStartupActions ) {
        // now execute the action...

        SimpleOutputHandler outputHandler = null;

        String instanceId = null;

        ISolutionEngine solutionEngine = PentahoSystem.get( ISolutionEngine.class, session );
        solutionEngine.setLoggingLevel( PentahoSystem.loggingLevel );
        solutionEngine.init( session );

        String baseUrl = ""; //$NON-NLS-1$
        HashMap parameterProviderMap = new HashMap();
        IPentahoUrlFactory urlFactory = new SimpleUrlFactory( baseUrl );

        ArrayList messages = new ArrayList();

        IRuntimeContext context = null;
        try {
          context =
            solutionEngine
              .execute(
                globalStartupAction.getActionPath(),
                "Global startup actions", false, true, instanceId, false, parameterProviderMap, outputHandler, null,
                urlFactory, messages ); //$NON-NLS-1$

          // if context is null, then we cannot check the status
          if ( null == context ) {
            return;
          }

          if ( context.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS ) {
            // now grab any outputs
            Iterator outputNameIterator = context.getOutputNames().iterator();
            while ( outputNameIterator.hasNext() ) {

              String attributeName = (String) outputNameIterator.next();
              IActionParameter output = context.getOutputParameter( attributeName );

              Object data = output.getValue();
              if ( data != null ) {
                PentahoSystem.globalAttributes.remove( attributeName );
                PentahoSystem.globalAttributes.put( attributeName, data );
              }
            }
          }
        } catch ( Throwable th ) {
          Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getString(
            "PentahoSystem.WARN_UNABLE_TO_EXECUTE_GLOBAL_ACTION", th.getLocalizedMessage() ), th ); //$NON-NLS-1$
        } finally {
          if ( context != null ) {
            context.dispose();
          }
        }

      }
    }
  }

  public static void shutdown() {
    if ( LocaleHelper.getLocale() == null ) {
      LocaleHelper.setLocale( Locale.getDefault() );
    }
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Shutdown Listeners" ); //$NON-NLS-1$
    }
    if ( PentahoSystem.listeners != null ) {
      Iterator systemListenerIterator = PentahoSystem.listeners.iterator();
      while ( systemListenerIterator.hasNext() ) {
        IPentahoSystemListener listener = (IPentahoSystemListener) systemListenerIterator.next();
        if ( listener != null ) {
          if ( debug ) {
            Logger.debug( PentahoSystem.class, "Shutdown Listener: " + listener.getClass().getName() ); //$NON-NLS-1$
          }
          try {
            listener.shutdown();
          } catch ( Throwable e ) {
            Logger.error( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
              "PentahoSystem.ERROR_0015_SHUTDOWN_FAILURE", listener.getClass().getName() ), e ); //$NON-NLS-1$
          }
        }
      }
    }
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Listeners Shut Down" ); //$NON-NLS-1$
    }
  }

  public static IApplicationContext getApplicationContext() {
    return PentahoSystem.applicationContext;
  }

  public static void setApplicationContext( IApplicationContext pApplicationContext ) {
    PentahoSystem.applicationContext = pApplicationContext;
  }

  // use PentahoSystem.get(...) to retrieve pentaho system objects
  @Deprecated
  public static Object createObject( final String className, final ILogger logger ) {

    Object object = null;
    try {

      Class componentClass = Class.forName( className.trim() );
      object = componentClass.newInstance();

    } catch ( Throwable t ) {
      String msg =
        Messages.getInstance()
          .getErrorString( "PentahoSystem.ERROR_0013_COULD_NOT_CREATE_OBEJCT", className ); //$NON-NLS-1$
      if ( null == logger ) {
        Logger.fatal( PentahoSystem.class.getName(), msg, t );
      } else {
        logger.fatal( msg, t );
      }
    }
    return object;
  }

  // use PentahoSystem.get(...) to retrieve pentaho system objects
  @Deprecated
  public static Object createObject( final String className ) {

    return PentahoSystem.createObject( className, null );
  }

  public static String getSystemSetting( final String path, final String settingName, final String defaultValue ) {
    if ( PentahoSystem.systemSettingsService == null ) {
      return defaultValue;
    }
    return PentahoSystem.systemSettingsService.getSystemSetting( path, settingName, defaultValue );
  }

  public static String getSystemSetting( final String settingName, final String defaultValue ) {
    // TODO make this more efficient using caching
    if ( PentahoSystem.systemSettingsService == null ) {
      return defaultValue;
    }
    return PentahoSystem.systemSettingsService.getSystemSetting( settingName, defaultValue );
  }

  public static ISystemSettings getSystemSettings() {
    return PentahoSystem.systemSettingsService;
  }

  public static void refreshSettings() {
    PentahoSystem.systemSettingsService.resetSettingsCache();
  }

  // TODO: shouldn't this be called execute or something like that?
  public static String publish( final IPentahoSession session, final String className ) {
    if ( debug ) {
      Logger.debug( PentahoSystem.class, "Publisher execution" ); //$NON-NLS-1$
    }
    Iterator<IPentahoPublisher> publisherIterator = PentahoSystem.administrationPlugins.iterator();
    // TODO: audit this
    while ( publisherIterator.hasNext() ) {
      IPentahoPublisher publisher = publisherIterator.next();
      if ( ( publisher != null ) && ( ( className == null ) || className.equals( publisher.getClass().getName() ) ) ) {
        try {
          if ( debug ) {
            Logger.debug( PentahoSystem.class, "Publisher publish: " + publisher.getClass().getName() ); //$NON-NLS-1$
          }
          return publisher.publish( session, PentahoSystem.loggingLevel );
        } catch ( Throwable e ) {
          e.printStackTrace();
        }
      }
    }
    return Messages.getInstance().getErrorString( "PentahoSystem.ERROR_0017_PUBLISHER_NOT_FOUND" ); //$NON-NLS-1$
  }

  // FIXME: should be named getAdministrationPlugins
  public static List getPublisherList() {
    return new ArrayList( PentahoSystem.administrationPlugins );
  }

  public static Document getPublishersDocument() {

    Document document = DocumentHelper.createDocument();
    Element root = document.addElement( "publishers" ); //$NON-NLS-1$
    if ( administrationPlugins != null ) {
      Iterator publisherIterator = PentahoSystem.administrationPlugins.iterator();
      // TODO: audit this
      // refresh the system settings
      while ( publisherIterator.hasNext() ) {
        IPentahoPublisher publisher = (IPentahoPublisher) publisherIterator.next();
        if ( publisher != null ) {
          try {
            Element publisherNode = root.addElement( "publisher" ); //$NON-NLS-1$
            publisherNode.addElement( "name" ).setText( publisher.getName() ); //$NON-NLS-1$
            publisherNode.addElement( "description" ).setText( publisher.getDescription() ); //$NON-NLS-1$
            publisherNode.addElement( "class" ).setText( publisher.getClass().getName() ); //$NON-NLS-1$

          } catch ( Throwable e ) {
            //ignore
          }
        }
      }
    }
    return document;

  }

  public static void systemEntryPoint() {
    if ( PentahoSystem.applicationContext != null ) {
      PentahoSystem.applicationContext.invokeEntryPoints();
    }
  }

  public static void systemExitPoint() {
    if ( PentahoSystem.applicationContext != null ) {
      PentahoSystem.applicationContext.invokeExitPoints();
    }
  }

  private static void registerHostnameVerifier() {
    try {
      final String LOCALHOST = "localhost"; //$NON-NLS-1$
      String tmphost = "localhost"; //$NON-NLS-1$
      try {
        String fullyQualifiedServerURL = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
        if ( null == fullyQualifiedServerURL ) {
          return;
        }
        URL url = new URL( fullyQualifiedServerURL );
        tmphost = url.getHost();
      } catch ( MalformedURLException e ) {
        Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0030_VERIFIER_FAILED" ), e ); //$NON-NLS-1$

      }
      final String host = tmphost;

      javax.net.ssl.HostnameVerifier myHv = new javax.net.ssl.HostnameVerifier() {
        public boolean verify( String hostName, javax.net.ssl.SSLSession session ) {
          if ( hostName.equals( host ) || hostName.equals( LOCALHOST ) ) {
            return true;
          }
          return false;
        }
      };
      javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier( myHv );
    } catch ( Throwable t ) {
      Logger.warn( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
        "PentahoSystem.ERROR_0030_VERIFIER_FAILED" ), t ); //$NON-NLS-1$
    }
  }

  // Security Helpers
  // TODO: clean this up so we don't have a fallback impl
  public static ICacheManager getCacheManager( IPentahoSession session ) {
    try {
      // TODO get the SimpleMapCacheManager into the object map somehow
      // we will try to use a simple map cache manager if one has not been configured
      ICacheManager cacheManager = aggObjectFactory.get( ICacheManager.class, session );
      return cacheManager;
    } catch ( ObjectFactoryException e ) {
      ICacheManager cacheManager = SimpleMapCacheManager.getInstance();
      Logger.warn( PentahoSystem.class.getName(), "Using default cache manager" ); //$NON-NLS-1$
      return cacheManager;
    }
  }

  public static List getACLFileExtensionList() {
    return PentahoSystem.UnmodifiableACLFileExtensionList;
  }

  // Stuff for the logout listener subsystem
  public static void addLogoutListener( final ILogoutListener listener ) {
    // add items to vector of listeners
    if ( PentahoSystem.logoutListeners.contains( listener ) ) {
      return;
    }
    PentahoSystem.logoutListeners.add( listener );
  }

  public static ILogoutListener remove( final ILogoutListener listener ) {
    if ( PentahoSystem.logoutListeners.remove( listener ) ) {
      return listener;
    }
    return null;
  }

  public static void invokeLogoutListeners( final IPentahoSession session ) {
    Iterator iter = PentahoSystem.logoutListeners.iterator();
    while ( iter.hasNext() ) {
      ILogoutListener listener = (ILogoutListener) iter.next();
      listener.onLogout( session );
    }
  }

  /**
   * Gets the factory that will create and manage Pentaho system objects.
   *
   * @return the factory
   */
  public static IPentahoObjectFactory getObjectFactory() {
    return aggObjectFactory;
  }

  /**
   * Registers the factory that will create and manage Pentaho system objects.
   *
   * @param pentahoObjectFactory the factory
   */
  public static void registerObjectFactory( IPentahoObjectFactory pentahoObjectFactory ) {
    PentahoSystem.aggObjectFactory.registerObjectFactory( pentahoObjectFactory );
  }

  /**
   * De-Register an ObjectFactory
   *
   * @param factory
   * @return true if the factory was registered and successfully removed.
   */
  public static boolean deregisterObjectFactory( IPentahoObjectFactory factory ) {
    return PentahoSystem.aggObjectFactory.deregisterObjectFactory( factory );
  }

  /**
   * Registers the primary factory that will create and manage Pentaho system objects.
   *
   * @param pentahoObjectFactory the factory
   */
  public static void registerPrimaryObjectFactory( IPentahoObjectFactory pentahoObjectFactory ) {
    PentahoSystem.aggObjectFactory.registerObjectFactory( pentahoObjectFactory, true );
  }

  /**
   * Registers administrative capabilities that can be invoked later via {@link PentahoSystem#publish(IPentahoSession,
   * String)}
   *
   * @param administrationPlugins a list of admin functions to register
   */
  public static void setAdministrationPlugins( List<IPentahoPublisher> administrationPlugins ) {
    PentahoSystem.administrationPlugins = administrationPlugins;
  }

  /**
   * Registers custom handlers that are notified of both system startup and system shutdown events.
   *
   * @param systemListeners the system event handlers
   */
  public static void setSystemListeners( List<IPentahoSystemListener> systemListeners ) {
    listeners = systemListeners;
  }

  /**
   * Registers server actions that will be invoked when a session is created. NOTE: it is completely up to the {@link
   * IPentahoSession} implementation whether to advise the system of it's creation via {@link
   * PentahoSystem#sessionStartup(IPentahoSession)}.
   *
   * @param actions the server actions to execute on session startup
   */
  public static void setSessionStartupActions( List<ISessionStartupAction> actions ) {
    sessionStartupActions = actions;
  }

  /**
   * Sets the system settings service: the means by which the platform obtains it's overall system settings.
   *
   * @param systemSettingsService the settings service
   */
  public static void setSystemSettingsService( ISystemSettings systemSettingsService ) {
    PentahoSystem.systemSettingsService = systemSettingsService;
  }

  // TODO: move this to a helper
  private static List<ISessionStartupAction> getSessionStartupActionsForType( String sessionClassName ) {
    ArrayList<ISessionStartupAction> startupActions = new ArrayList<ISessionStartupAction>();
    if ( sessionStartupActions != null ) {
      for ( ISessionStartupAction sessionStartupAction : sessionStartupActions ) {
        if ( sessionStartupAction.getSessionType().equals( sessionClassName )
          && sessionStartupAction.getActionOutputScope().equals( SCOPE_SESSION ) ) {
          startupActions.add( sessionStartupAction );
        }
      }
    }
    return startupActions;
  }

  // TODO: if a ISessionStartupAction is called on something other than a session, should
  // we be using it here in a global context?
  private static List<ISessionStartupAction> getGlobalStartupActions() {
    ArrayList<ISessionStartupAction> startupActions = new ArrayList<ISessionStartupAction>();
    if ( sessionStartupActions != null ) {
      for ( ISessionStartupAction sessionStartupAction : sessionStartupActions ) {
        if ( sessionStartupAction.getActionOutputScope().equals( SCOPE_GLOBAL ) ) {
          startupActions.add( sessionStartupAction );
        }
      }
    }
    return startupActions;
  }

  // End of transitional methods.

  /**
   * Make sure all required objects exist in the object factory. If not, throw an exception. If any optional objects are
   * missing, simply log it to the logger.
   *
   * @throws PentahoSystemException if a required object is missing.
   */
  private static void validateObjectFactory() throws PentahoSystemException {
    boolean isRequiredValid = true;
    for ( String interfaceName : PentahoSystem.RequiredObjects ) {
      boolean isValid = aggObjectFactory.objectDefined( interfaceName );
      isRequiredValid &= isValid;
      if ( !isValid ) {
        Logger.fatal( PentahoSystem.class.getName(), Messages.getInstance().getErrorString(
          "PentahoSystem.ERROR_0021_OBJECT_NOT_SPECIFIED", interfaceName ) ); //$NON-NLS-1$
      }
    }
    for ( String interfaceName : PentahoSystem.KnownOptionalObjects ) {
      boolean isValid = aggObjectFactory.objectDefined( interfaceName );
      if ( !isValid ) {
        Logger.info( PentahoSystem.class.getName(), Messages.getInstance().getString(
          "PentahoSystem.ERROR_0021_OBJECT_NOT_SPECIFIED", interfaceName ) ); //$NON-NLS-1$
      }
    }
    if ( !isRequiredValid ) {
      throw new PentahoSystemException( Messages.getInstance().getErrorString(
        "PentahoSystem.ERROR_0420_MISSING_REQUIRED_OBJECT" ) ); //$NON-NLS-1$
    }
  }

  public static void clearObjectFactory() {
    aggObjectFactory.clear();
    runtimeObjectFactory = new RuntimeObjectFactory();
    aggObjectFactory.registerObjectFactory( runtimeObjectFactory );
  }

  /**
   * This static method shadows the one defined here: {@link org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory#registerReference( org.pentaho.platform.api.engine.IPentahoObjectReference,
   * org.pentaho.platform.api.engine .IPentahoRegistrableObjectFactory.Types)}
   *
   * @param reference
   * @param types
   * @param <T>
   */
  public static <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference,
                                                                  IPentahoRegistrableObjectFactory.Types types ) {
    return PentahoSystem.runtimeObjectFactory.registerReference( reference, types );
  }

  /**
   * This static method shadows the one defined here: {@link org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory#registerObject(Object)}
   *
   * @param obj
   */
  public static IPentahoObjectRegistration registerObject( Object obj ) {
    return PentahoSystem.runtimeObjectFactory.registerObject( obj );
  }

  /**
   * This static method shadows the one defined here: {@link org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory#registerObject( Object, org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory.Types)}
   *
   * @param obj
   * @param types
   */
  public static IPentahoObjectRegistration registerObject( Object obj, IPentahoRegistrableObjectFactory.Types types ) {
    return PentahoSystem.runtimeObjectFactory.registerObject( obj, types );
  }

  /**
   * {@link org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory#registerReference(
   *org.pentaho.platform.api.engine.IPentahoObjectReference)}
   *
   * @param reference
   * @param <T>
   */
  public static <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference ) {
    return PentahoSystem.runtimeObjectFactory.registerReference( reference );
  }

  /**
   * This static method shadows the one defined here: {@link org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory#registerObject(Object, Class[])}
   *
   * @param obj
   * @param classes
   */
  public static <T> IPentahoObjectRegistration registerObject( T obj, Class<?>... classes ) {
    return PentahoSystem.runtimeObjectFactory.registerObject( obj, classes );
  }


  /**
   * This static method shadows the one defined here: {@link org.pentaho.platform.api.engine
   * .IPentahoRegistrableObjectFactory#registerReference( org.pentaho.platform.api.engine.IPentahoObjectReference,
   * Class[])}
   *
   * @param reference
   * @param classes
   * @param <T>
   */
  public static <T> IPentahoObjectRegistration registerReference( IPentahoObjectReference<T> reference,
                                                                  Class<?>... classes ) {
    return PentahoSystem.runtimeObjectFactory.registerReference( reference, classes );
  }
}
