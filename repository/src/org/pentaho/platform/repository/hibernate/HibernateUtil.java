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

package org.pentaho.platform.repository.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.ConstraintViolationException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSystemEntryPoint;
import org.pentaho.platform.api.engine.IPentahoSystemExitPoint;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ContentException;
import org.pentaho.platform.api.repository.ISearchable;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.MessageUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class HibernateUtil implements IPentahoSystemEntryPoint, IPentahoSystemExitPoint {

  private static final Log log = LogFactory.getLog( HibernateUtil.class );

  private static final boolean debug = PentahoSystem.debug;

  private static boolean useNewDatasourceService = false;

  private static Configuration configuration;

  private static SessionFactory sessionFactory;

  private static final byte[] lock = new byte[0];

  private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();

  private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();

  private static final ThreadLocal<Interceptor> threadInterceptor = new ThreadLocal<Interceptor>();

  // private static final ThreadLocal commitNeeded = new ThreadLocal();
  private static boolean hibernateManaged;

  private static String factoryJndiName;

  private static String dialect;

  private static Context iniCtx;

  private static final String QUERYWILDCARD = "%{0}%"; //$NON-NLS-1$

  static {
    // JIRA case #PLATFORM 150: removed listener and changed to lazy init
    HibernateUtil.initialize();
  }

  public void setUseNewDatasourceService( boolean useNewService ) {
    //
    // The platform should not be calling this method. But, in case someone really
    // really wants to use the new datasource service features to hook up
    // a core service like Hibernate, this is now toggle-able.
    //
    synchronized ( HibernateUtil.lock ) {
      useNewDatasourceService = useNewService;
    }
  }

  //
  private HibernateUtil() {
  }

  protected static boolean initialize() {
    IApplicationContext applicationContext = PentahoSystem.getApplicationContext();
    // Add to entry/exit points list
    HibernateUtil hUtil = new HibernateUtil();
    applicationContext.addEntryPointHandler( hUtil );
    applicationContext.addExitPointHandler( hUtil );

    // Look for some hibernate-specific properties...

    String hibernateConfigurationFile = lookupSetting( applicationContext, "hibernateConfigPath", //$NON-NLS-1$
        "settings/config-file", //$NON-NLS-1$
        "hibernate/hibernateConfigPath" ); //$NON-NLS-1$

    String hibernateManagedString = lookupSetting( applicationContext, "hibernateManaged", //$NON-NLS-1$
        "settings/managed", //$NON-NLS-1$
        "hibernate/hibernateManaged" ); //$NON-NLS-1$

    if ( hibernateManagedString != null ) {
      hibernateManaged = Boolean.parseBoolean( hibernateManagedString );
    }

    try {
      HibernateUtil.configuration = new Configuration();
      HibernateUtil.configuration.setEntityResolver( new PentahoEntityResolver() );
      HibernateUtil.configuration.setListener( "load", new HibernateLoadEventListener() ); //$NON-NLS-1$

      if ( hibernateConfigurationFile != null ) {
        String configPath = applicationContext.getSolutionPath( hibernateConfigurationFile );
        File cfgFile = new File( configPath );
        if ( cfgFile.exists() ) {
          HibernateUtil.configuration.configure( cfgFile );
        } else {
          HibernateUtil.log.error( Messages.getInstance().getErrorString(
              "HIBUTIL.ERROR_0012_CONFIG_NOT_FOUND", configPath ) ); //$NON-NLS-1$
          return false;
        }
      } else {
        // Assume defaults which means we hope Hibernate finds a configuration
        // file in a file named hibernate.cfg.xml
        HibernateUtil.log.error( Messages.getInstance().getErrorString(
            "HIBUTIL.ERROR_0420_CONFIGURATION_ERROR_NO_HIB_CFG_FILE_SETTING" ) ); //$NON-NLS-1$
        HibernateUtil.configuration.configure();
      }
      String dsName = HibernateUtil.configuration.getProperty( "connection.datasource" ); //$NON-NLS-1$
      if ( ( dsName != null ) && dsName.toUpperCase().endsWith( "HIBERNATE" ) ) { //$NON-NLS-1$
        // IDBDatasourceService datasourceService =  (IDBDatasourceService) PentahoSystem.getObjectFactory().getObject("IDBDatasourceService",null);     //$NON-NLS-1$
        IDBDatasourceService datasourceService = getDatasourceService();
        String actualDSName = datasourceService.getDSBoundName( "Hibernate" ); //$NON-NLS-1$
        HibernateUtil.configuration.setProperty( "hibernate.connection.datasource", actualDSName ); //$NON-NLS-1$
      }

      HibernateUtil.dialect = HibernateUtil.configuration.getProperty( "dialect" ); //$NON-NLS-1$

      /*
       * configuration.addResource("org/pentaho/platform/repository/runtime/RuntimeElement.hbm.xml"); //$NON-NLS-1$
       * configuration.addResource("org/pentaho/platform/repository/content/ContentLocation.hbm.xml"); //$NON-NLS-1$
       * configuration.addResource("org/pentaho/platform/repository/content/ContentItem.hbm.xml"); //$NON-NLS-1$
       * configuration.addResource("org/pentaho/platform/repository/content/ContentItemFile.hbm.xml"); //$NON-NLS-1$
       */
      if ( !HibernateUtil.hibernateManaged ) {
        HibernateUtil.log.info( Messages.getInstance().getString( "HIBUTIL.USER_HIBERNATEUNMANAGED" ) ); //$NON-NLS-1$
        HibernateUtil.sessionFactory = HibernateUtil.configuration.buildSessionFactory();
      } else {
        HibernateUtil.factoryJndiName = HibernateUtil.configuration.getProperty( Environment.SESSION_FACTORY_NAME );
        if ( HibernateUtil.factoryJndiName == null ) {
          HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0013_NO_SESSION_FACTORY" ) );
          return false;
        }
        HibernateUtil.log.info( Messages.getInstance().getString( "HIBUTIL.USER_HIBERNATEMANAGED" ) ); //$NON-NLS-1$
        HibernateUtil.configuration.buildSessionFactory(); // Let hibernate Bind it
        // to JNDI...

        // BISERVER-2006: Below content is a community contribution see the JIRA case for more info
        // -------- Begin Contribution --------
        // Build the initial context to use when looking up the session
        Properties contextProperties = new Properties();
        if ( configuration.getProperty( "hibernate.jndi.url" ) != null ) { //$NON-NLS-1$
          contextProperties.put( Context.PROVIDER_URL, configuration.getProperty( "hibernate.jndi.url" ) ); //$NON-NLS-1$
        }

        if ( configuration.getProperty( "hibernate.jndi.class" ) != null ) { //$NON-NLS-1$
          contextProperties.put( Context.INITIAL_CONTEXT_FACTORY, configuration.getProperty( "hibernate.jndi.class" ) ); //$NON-NLS-1$
        }
        iniCtx = new InitialContext( contextProperties );
        // --------- End Contribution ---------

      }
      Dialect.getDialect( HibernateUtil.configuration.getProperties() );
      return true;
    } catch ( Throwable ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0006_BUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
      throw new ExceptionInInitializerError( ex );
    }
  }

  private static IDBDatasourceService getDatasourceService() throws ObjectFactoryException {
    //
    // Our new datasource stuff is provided for running queries and acquiring data. It is
    // NOT there for the inner workings of the platform. So, the Hibernate datasource should ALWAYS
    // be provided by JNDI. However, the class could be twiddled so that it will use the factory.
    //
    // And, since the default shipping condition should be to NOT use the factory (and force JNDI),
    // I've reversed the logic in the class to have the negative condition first (the default execution
    // path).
    //
    // Marc - BISERVER-2004
    //
    if ( !useNewDatasourceService ) {
      return new JndiDatasourceService();
    } else {
      IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, null );
      return datasourceService;
    }
  }

  private static String lookupSetting( IApplicationContext applicationContext, String applicationContextName,
      String hibernateSettingsName, String pentahoXmlName ) {

    // 1- Look in applicationContext
    // 2- Look in PentahoSystem/hibernate-settings.xml
    // 3- Look in pentaho.xml

    String tmp = null;

    tmp = applicationContext.getProperty( applicationContextName, null );
    if ( ( tmp != null ) && ( !StringUtil.isEmpty( tmp ) ) ) {
      return tmp.trim();
    }
    if ( tmp == null ) {
      tmp = PentahoSystem.getSystemSetting( "hibernate/hibernate-settings.xml", hibernateSettingsName, null ); //$NON-NLS-1$
      if ( ( tmp != null ) && ( !StringUtil.isEmpty( tmp ) ) ) {
        return tmp.trim();
      }
    }
    if ( tmp == null ) {
      tmp = PentahoSystem.getSystemSetting( pentahoXmlName, null );
      if ( ( tmp != null ) && ( !StringUtil.isEmpty( tmp ) ) ) {
        return tmp.trim();
      }
    }
    return null;
  }

  /**
   * Returns the SessionFactory used for this static class.
   * 
   * @return SessionFactory
   */
  public static SessionFactory getSessionFactory() {
    if ( !HibernateUtil.hibernateManaged ) {
      return HibernateUtil.sessionFactory;
    }
    SessionFactory sf = null;
    try {
      if ( HibernateUtil.iniCtx == null ) {
        HibernateUtil.iniCtx = new InitialContext();
      }
      String jndiName = HibernateUtil.factoryJndiName;
      try {
        sf = (SessionFactory) HibernateUtil.iniCtx.lookup( jndiName );
      } catch ( Exception ignored ) {
        // CHECKSTYLES IGNORE
      }
      if ( sf == null ) {
        try {
          sf = (SessionFactory) HibernateUtil.iniCtx.lookup( "java:" + jndiName ); //$NON-NLS-1$
        } catch ( Exception ignored ) {
          ignored.printStackTrace();
        }
      }
    } catch ( NamingException ignored ) {
      // CHECKSTYLES IGNORE
    }
    return sf;
  }

  /**
   * Returns the original Hibernate configuration.
   * 
   * @return Configuration
   */
  public static Configuration getConfiguration() {
    return HibernateUtil.configuration;
  }

  /**
   * Rebuild the SessionFactory with the static Configuration.
   * 
   */
  public static void rebuildSessionFactory() throws RepositoryException {
    if ( !HibernateUtil.hibernateManaged ) {
      synchronized ( HibernateUtil.lock ) {
        try {
          HibernateUtil.sessionFactory = HibernateUtil.getConfiguration().buildSessionFactory();
        } catch ( Exception ex ) {
          HibernateUtil.log.error(
              Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
          throw new RepositoryException( Messages.getInstance().getErrorString(
              "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
        }
      }
    } else {
      try {
        HibernateUtil.getConfiguration().buildSessionFactory();
      } catch ( Exception ex ) {
        HibernateUtil.log.error(
            Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
        throw new RepositoryException( Messages.getInstance().getErrorString(
            "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Rebuild the SessionFactory with the given Hibernate Configuration.
   * 
   * @param cfg
   */
  public static void rebuildSessionFactory( final Configuration cfg ) throws RepositoryException {
    if ( !HibernateUtil.hibernateManaged ) {
      synchronized ( HibernateUtil.lock ) {
        try {
          HibernateUtil.sessionFactory = cfg.buildSessionFactory();
          HibernateUtil.configuration = cfg;
        } catch ( Exception ex ) {
          HibernateUtil.log.error(
              Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
          throw new RepositoryException( Messages.getInstance().getErrorString(
              "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
        }
      }
    } else {
      try {
        cfg.buildSessionFactory();
        HibernateUtil.configuration = cfg;
      } catch ( Exception ex ) {
        HibernateUtil.log.error(
            Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
        throw new RepositoryException( Messages.getInstance().getErrorString(
            "HIBUTIL.ERROR_0007_REBUILD_SESSION_FACTORY" ), ex ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Retrieves the current Session local to the thread.
   * <p/>
   * If no Session is open, opens a new Session for the running thread.
   * 
   * @return Session
   */
  public static Session getSession() throws RepositoryException {
    Session s = (Session) HibernateUtil.threadSession.get();
    try {
      if ( s == null ) {
        if ( HibernateUtil.debug ) {
          HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_OPEN_NEW_SESSION" ) ); //$NON-NLS-1$
        }
        if ( HibernateUtil.getInterceptor() != null ) {
          if ( HibernateUtil.debug ) {
            HibernateUtil.log
                .debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_USING_INTERCEPTOR" ) + HibernateUtil.getInterceptor().getClass() ); //$NON-NLS-1$
          }
          s = HibernateUtil.getSessionFactory().openSession( HibernateUtil.getInterceptor() );
        } else {
          s = HibernateUtil.getSessionFactory().openSession();
        }
        HibernateUtil.threadSession.set( s );
      }
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0005_GET_SESSION" ), ex ); //$NON-NLS-1$
      throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0005_GET_SESSION" ), ex ); //$NON-NLS-1$
    }
    return s;
  }

  public static void flushSession() throws RepositoryException {
    try {
      Session s = HibernateUtil.getSession();
      s.flush();
    } catch ( HibernateException ex ) {
      throw new RepositoryException( ex );
    }
  }

  /**
   * Closes the Session local to the thread.
   */
  public static void closeSession() throws RepositoryException {
    try {
      Session s = (Session) HibernateUtil.threadSession.get();
      HibernateUtil.threadSession.set( null );
      if ( ( s != null ) && s.isOpen() ) {
        if ( HibernateUtil.debug ) {
          HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_CLOSING_SESSION" ) ); //$NON-NLS-1$
        }
        s.close();
      }
      HibernateUtil.threadTransaction.set( null );
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0009_CLOSE_SESSION" ), ex ); //$NON-NLS-1$
      HibernateUtil.threadTransaction.set( null );
      throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0009_CLOSE_SESSION" ), ex ); //$NON-NLS-1$
    }

  }

  /**
   * Start a new database transaction.
   */
  public static void beginTransaction() throws RepositoryException {
    // commitNeeded.set(Boolean.TRUE);
    Transaction tx = (Transaction) HibernateUtil.threadTransaction.get();
    try {
      if ( tx == null ) {
        if ( HibernateUtil.debug ) {
          HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_START_TRANS" ) ); //$NON-NLS-1$
        }
        tx = HibernateUtil.getSession().beginTransaction();
        HibernateUtil.threadTransaction.set( tx );
      }
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0004_START_TRANS" ), ex ); //$NON-NLS-1$
      throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0004_START_TRANS" ), ex ); //$NON-NLS-1$
    }
  }

  /**
   * Commit the database transaction.
   */
  public static void commitTransaction() throws RepositoryException {
    // Boolean needed = (Boolean)commitNeeded.get();
    // if (needed.booleanValue()){
    Transaction tx = (Transaction) HibernateUtil.threadTransaction.get();
    try {
      if ( ( tx != null ) && !tx.wasCommitted() && !tx.wasRolledBack() ) {
        if ( HibernateUtil.debug ) {
          HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_COMMIT_TRANS" ) ); //$NON-NLS-1$
        }
        tx.commit();
      }
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0008_COMMIT_TRANS" ), ex ); //$NON-NLS-1$
      try {
        HibernateUtil.rollbackTransaction();
      } catch ( Exception e2 ) {
        // CHECKSTYLES IGNORE
      }
      if ( ex instanceof ConstraintViolationException ) {
        throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0008_COMMIT_TRANS" ), ex ); //$NON-NLS-1$
      }
      // throw new
      // RepositoryException(Messages.getInstance().getErrorString("HIBUTIL.ERROR_0008_COMMIT_TRANS"),
      // ex); //$NON-NLS-1$
    } finally {
      HibernateUtil.threadTransaction.set( null );
    }
    // }
    // commitNeeded.set(Boolean.FALSE);
  }

  /**
   * Commit the database transaction.
   */
  public static void rollbackTransaction() throws RepositoryException {
    Transaction tx = (Transaction) HibernateUtil.threadTransaction.get();
    try {
      HibernateUtil.threadTransaction.set( null );
      if ( ( tx != null ) && !tx.wasCommitted() && !tx.wasRolledBack() ) {
        if ( HibernateUtil.debug ) {
          HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_ROLLBACK" ) ); //$NON-NLS-1$
        }
        tx.rollback();
      }
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0003_ROLLBACK" ), ex ); //$NON-NLS-1$
      throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0003_ROLLBACK" ), ex ); //$NON-NLS-1$
    } finally {
      HibernateUtil.closeSession();
    }
  }

  /**
   * Reconnects a Hibernate Session to the current Thread.
   * 
   * @param session
   *          The Hibernate Session to be reconnected.
   */
  /*
   * public static void reconnect(Session session) throws RepositoryException { try { session.reconnect();
   * threadSession.set(session); } catch (HibernateException ex) {
   * log.error(Messages.getInstance().getErrorString("HIBUTIL.ERROR_0001_RECONNECT"), ex); //$NON-NLS-1$ throw new
   * RepositoryException(ex); } }
   */
  /**
   * Disconnect and return Session from current Thread.
   * 
   * @return Session the disconnected Session
   */
  public static Session disconnectSession() throws RepositoryException {

    Session session = HibernateUtil.getSession();
    try {
      HibernateUtil.threadSession.set( null );
      if ( session.isConnected() && session.isOpen() ) {
        session.disconnect();
      }
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0002_DISCONNECT" ), ex ); //$NON-NLS-1$
      throw new RepositoryException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0002_DISCONNECT" ), ex ); //$NON-NLS-1$
    }
    return session;
  }

  /**
   * Register a Hibernate interceptor with the current thread.
   * <p>
   * Every Session opened is opened with this interceptor after registration. Has no effect if the current Session
   * of the thread is already open, effective on next close()/getSession().
   */
  public static void registerInterceptor( final Interceptor interceptor ) {
    HibernateUtil.threadInterceptor.set( interceptor );
  }

  private static Interceptor getInterceptor() {
    Interceptor interceptor = (Interceptor) HibernateUtil.threadInterceptor.get();
    return interceptor;
  }

  /**
   * Searches an ISearchable object for a search term. The search rules are as follows:
   * 
   * If the searchType is ISearchable.SEARCH_TYPE_PHRASE, then the fields in the table are searched for the exact
   * phrase given.
   * 
   * If the searchType is ISearchable.SEARCH_TYPE_WORDS_AND or ..._OR, then the following happens: a- Each word in
   * the searchTerm is extracted and put into a list of search terms. b- Each search term is surrounded by the SQL
   * wildcard '%'. So each search term becomes %term%. c- A dynamic query is generated searching each of the
   * columns for each search term d- The searchType is used to determine the connector between each search term. e-
   * The AND will match only if all of the terms appear in a specific column - cross-column searching using ..._AND
   * will NOT work. In other words, if your search term is "East Sales", and your search type is ..._AND, a row
   * will be returned if one of the columns contains East and the same column contains Sales. A row will NOT be
   * returned if one column only contains East, and another column only contains Sales. This type of functionality
   * could be obtained using a view that concatenates all of the searchable columns together into one large column,
   * but this would be costly and database-specific.
   * 
   * @param searchable
   *          ISearchable to search
   * @param searchTerm
   *          Search Term - see above for rules
   * @param searchType
   *          One of: ISearchable.SEARCH_TYPE_PHRASE,ISearchable.SEARCH_TYPE_WORDS_AND,
   *          ISearchable.SEARCH_TYPE_WORDS_OR
   * @return A list of objects from Hibernate that met the conditions specified.
   */
  public static List searchForTerm( final ISearchable searchable, final String searchTerm, final int searchType ) {
    Session session = HibernateUtil.getSession();
    if ( searchType == ISearchable.SEARCH_TYPE_PHRASE ) {
      Query qry = session.getNamedQuery( searchable.getPhraseSearchQueryName() );
      String searchWildcard = MessageUtil.formatErrorMessage( HibernateUtil.QUERYWILDCARD, searchTerm );
      qry.setString( "searchTerm", searchWildcard ); //$NON-NLS-1$
      List rtn = qry.list();
      return rtn;
    }
    String connector;
    if ( searchType == ISearchable.SEARCH_TYPE_WORDS_AND ) {
      connector = " and "; //$NON-NLS-1$
    } else {
      connector = " or "; //$NON-NLS-1$
    }
    StringTokenizer st = new StringTokenizer( searchTerm, " " ); //$NON-NLS-1$
    List<String> searchWords = new ArrayList<String>();
    while ( st.hasMoreTokens() ) {
      searchWords.add( MessageUtil.formatErrorMessage( HibernateUtil.QUERYWILDCARD, st.nextToken() ) );
    }
    // Ok, we now have a list of search words.
    StringBuffer assembly =
        HibernateUtil.assembleQuery( searchable.getSearchableTable(), connector, searchWords, searchable
            .getSearchableColumns() );
    Query qry = session.createQuery( assembly.toString() );
    for ( int j = 0; j < searchWords.size(); j++ ) {
      qry.setParameter( "searchTerm" + j, searchWords.get( j ) ); //$NON-NLS-1$
    }
    List rtn = qry.list();
    return rtn;
  }

  private static StringBuffer assembleQuery( final String tableName, final String connector, final List<String> terms,
      final String[] columns ) {
    StringBuffer qry = new StringBuffer();
    qry.append( "from " ).append( tableName ).append( " tbl where " ); //$NON-NLS-1$ //$NON-NLS-2$
    String currCol, term;
    for ( int colno = 0; colno < columns.length; colno++ ) {
      currCol = columns[colno];
      qry.append( "(" ); //$NON-NLS-1$
      for ( int termNo = 0; termNo < terms.size(); termNo++ ) {
        term = (String) terms.get( termNo );
        qry.append( "tbl." ).append( currCol ).append( " like :searchTerm" ).append( term ).append( " " ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if ( termNo < terms.size() - 1 ) {
          qry.append( connector );
        }
      }
      qry.append( ")" ); //$NON-NLS-1$
      if ( colno < columns.length - 1 ) {
        qry.append( " or " ); // Columns are always or'd //$NON-NLS-1$
      }
    }
    return qry;
  }

  public static void clear() {
    HibernateUtil.getSession().clear();
  }

  /**
   * Persists changes to the object. Object must be defined to hibernate.
   * 
   * @param obj
   *          The object to make persistent
   * @throws RepositoryException
   */
  public static void makePersistent( final Object obj ) throws RepositoryException {
    if ( HibernateUtil.debug ) {
      HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_MAKE_PERSISTENT", obj.toString() ) ); //$NON-NLS-1$
    }
    try {
      HibernateUtil.getSession().saveOrUpdate( obj );
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0010_SAVING_UPDATING" ), ex ); //$NON-NLS-1$
      throw new ContentException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0010_SAVING_UPDATING" ), ex ); //$NON-NLS-1$
    }
  }

  /**
   * Deletes the object from Hibernate
   * 
   * @param obj
   *          The object to make transient
   * @throws RepositoryException
   */
  public static void makeTransient( final Object obj ) throws RepositoryException {
    if ( HibernateUtil.debug ) {
      HibernateUtil.log.debug( Messages.getInstance().getString( "HIBUTIL.DEBUG_MAKE_TRANSIENT", obj.toString() ) ); //$NON-NLS-1$
    }
    try {
      HibernateUtil.getSession().delete( obj );
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0011_DELETING_OBJ" ), ex ); //$NON-NLS-1$
      throw new ContentException( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0011_DELETING_OBJ" ), ex ); //$NON-NLS-1$
    }
  }

  /**
   * HACK This method is necessary to determine whether code should execute based on Oracle in use as the RDBMS
   * repository for the platform. Helps us work around Oracle JDBC driver bugs.
   * 
   * @return true if Hibernate dialect for oracle is in use.
   */
  public static boolean isOracleDialect() {
    return ( HibernateUtil.dialect.indexOf( "oracle" ) >= 0 ) || //$NON-NLS-1$
        ( HibernateUtil.dialect.indexOf( "Oracle" ) >= 0 ) || //$NON-NLS-1$
        ( HibernateUtil.dialect.indexOf( "ORACLE" ) >= 0 ); //$NON-NLS-1$
  }

  /**
   * Evicts the object from the Hibernate cache. Call this if you don't believe you'll need this object in the
   * cache. This is also good to call if you're doing semi-mass updates.
   * 
   * @param obj
   */
  public static void evict( final Object obj ) {
    // if (debug)
    // log.debug(Messages.getInstance().getString("HIBUTIL.DEBUG_EVICT", obj.toString())); //$NON-NLS-1$
    try {
      HibernateUtil.getSession().evict( obj );
    } catch ( HibernateException ex ) {
      HibernateUtil.log.error( Messages.getInstance().getErrorString( "HIBUTIL.ERROR_0014_EVICTING_OBJECT" ), ex ); //$NON-NLS-1$
    }

  }

  public void systemEntryPoint() {
    // No need to do anything for Hibernate here.
  }

  public void systemExitPoint() {
    try {
      HibernateUtil.commitTransaction();
    } catch ( Throwable t ) {
      // get some real logging code in here
      t.printStackTrace();
    }

    try {
      HibernateUtil.closeSession();
    } catch ( Throwable t ) {
      // get some real logging code in here
      t.printStackTrace();
    }
  }

}
