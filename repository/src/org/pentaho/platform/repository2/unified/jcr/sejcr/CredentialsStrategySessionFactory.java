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

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.jcr.EventListenerDefinition;
import org.springframework.extensions.jcr.JcrSessionFactory;
import org.springframework.extensions.jcr.JcrUtils;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.extensions.jcr.SessionHolder;
import org.springframework.extensions.jcr.SessionHolderProvider;
import org.springframework.extensions.jcr.SessionHolderProviderManager;
import org.springframework.extensions.jcr.support.GenericSessionHolderProvider;
import org.springframework.util.Assert;

import javax.jcr.Credentials;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Copy-and-paste of {@link JcrSessionFactory} except that this implementation delegates to a {@link
 * CredentialsStrategy} implementation for getting a {@link Credentials} instance. Also has fixes from <a
 * href="http://jira.springframework.org/browse/SEJCR-18">SEJCR-18</a>. Also getBareSession changed to getAdminSession
 * and runs as Jackrabbit admin.
 *
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class CredentialsStrategySessionFactory implements InitializingBean, DisposableBean, SessionFactory {

  private static final Logger LOG = LoggerFactory.getLogger( JcrSessionFactory.class );

  private Repository repository;

  private String workspaceName;

  private CredentialsStrategy credentialsStrategy = new ConstantCredentialsStrategy();

  private CredentialsStrategy adminCredentialsStrategy = new ConstantCredentialsStrategy();

  private EventListenerDefinition[] eventListeners = new EventListenerDefinition[] { };

  private Properties namespaces;

  private Map<String, String> overwrittenNamespaces;

  private boolean forceNamespacesRegistration = false;

  private boolean keepNewNamespaces = true;

  private boolean skipExistingNamespaces = true;

  /**
   * session holder provider manager - optional.
   */
  private SessionHolderProviderManager sessionHolderProviderManager;

  /**
   * session holder provider - determined and used internally.
   */
  private SessionHolderProvider sessionHolderProvider;

  private List<NodeTypeDefinitionProvider> nodeTypeDefinitionProviders;

  protected PentahoJcrSessionFactory sessionFactory;

  /**
   * Constructor with all the required fields.
   *
   * @param repository
   * @param workspaceName
   * @param credentials
   */
  public CredentialsStrategySessionFactory( Repository repository, CredentialsStrategy credentialsStrategy ) {
    this( repository, null, credentialsStrategy, null, null );
  }

  /**
   * Constructor with all the required fields.
   *
   * @param repository
   * @param workspaceName
   * @param credentials
   */
  public CredentialsStrategySessionFactory( Repository repository, CredentialsStrategy credentialsStrategy,
                                            CredentialsStrategy adminCredentialsStrategy ) {
    this( repository, null, credentialsStrategy, adminCredentialsStrategy, null );
  }

  /**
   * Constructor with all the required fields.
   *
   * @param repository
   * @param workspaceName
   * @param credentials
   */
  public CredentialsStrategySessionFactory( Repository repository, String workspaceName,
                                            CredentialsStrategy credentialsStrategy,
                                            CredentialsStrategy adminCredentialsStrategy ) {
    this( repository, workspaceName, credentialsStrategy, adminCredentialsStrategy, null );
  }

  /**
   * Constructor containing all the fields available.
   *
   * @param repository
   * @param workspaceName
   * @param credentials
   * @param sessionHolderProviderManager
   */
  public CredentialsStrategySessionFactory( Repository repository, String workspaceName,
                                            CredentialsStrategy credentialsStrategy,
                                            CredentialsStrategy adminCredentialsStrategy,
                                            SessionHolderProviderManager sessionHolderProviderManager ) {
    this.repository = repository;
    this.workspaceName = workspaceName;
    this.credentialsStrategy = credentialsStrategy;
    this.adminCredentialsStrategy = adminCredentialsStrategy;
    this.sessionHolderProviderManager = sessionHolderProviderManager;
  }


  public PentahoJcrSessionFactory getSessionFactory() {
    if ( sessionFactory == null ) {
      // use default
      sessionFactory = new GuavaCachePoolPentahoJcrSessionFactory( this.repository, this.workspaceName );
    }
    return sessionFactory;
  }

  public void setSessionFactory( PentahoJcrSessionFactory sessionFactory ) {
    this.sessionFactory = sessionFactory;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( getRepository(), "repository is required" );

    if ( eventListeners != null && eventListeners.length > 0 && !JcrUtils.supportsObservation( getRepository() ) ) {
      throw new IllegalArgumentException( "repository " + getRepositoryInfo()
        + " does NOT support Observation; remove Listener definitions" );
    }

    if ( this.adminCredentialsStrategy != null ) {
      registerNamespaces();
      registerNodeTypes();
    }

    // determine the session holder provider
    if ( sessionHolderProviderManager == null ) {
      if ( LOG.isDebugEnabled() ) {
        LOG.debug( "no session holder provider manager set; using the default one" );
      }
      sessionHolderProvider = new GenericSessionHolderProvider();
    } else {
      sessionHolderProvider = sessionHolderProviderManager.getSessionProvider( getRepository() );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.extensions.jcr.JcrSessionFactory#registerNodeTypes()
   */
  protected void registerNodeTypes() throws Exception {
    if ( !nodeTypeDefinitionProviders.isEmpty() ) {
      Session session = null;
      try {
        session = getAdminSession();
        Workspace ws = session.getWorkspace();
        NodeTypeManager ntMgr = ws.getNodeTypeManager();
        ValueFactory vFac = session.getValueFactory();
        List<NodeTypeDefinition> ntds = new ArrayList<NodeTypeDefinition>();
        for ( NodeTypeDefinitionProvider nodeTypeDefinitionProvider : nodeTypeDefinitionProviders ) {
          ntds.add( nodeTypeDefinitionProvider.getNodeTypeDefinition( ntMgr, vFac ) );
        }
        ntMgr.registerNodeTypes( ntds.toArray( new NodeTypeDefinition[ 0 ] ), true );
      } catch ( RepositoryException ex ) {
        LOG.error( "Error registering nodetypes ", ex.getCause() );
      } finally {
        if ( session != null ) {
          session.logout();
        }
      }
    }
  }

  public void setNodeTypeDefinitionProviders( final List<NodeTypeDefinitionProvider> nodeTypeDefinitionProviders ) {
    this.nodeTypeDefinitionProviders = nodeTypeDefinitionProviders;
  }

  /**
   * Hook for un-registering node types on the underlying repository. Since this process is not covered by the spec,
   * each implementation requires its own subclass. By default, this method doesn't do anything.
   */
  protected void unregisterNodeTypes() throws Exception {
    // do nothing
  }

  /**
   * Register the namespaces.
   *
   * @throws RepositoryException
   */
  protected void registerNamespaces() throws Exception {

    if ( namespaces == null || namespaces.isEmpty() ) {
      return;
    }

    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "registering custom namespaces " + namespaces );
    }

    Session session = getAdminSession();
    NamespaceRegistry registry = session.getWorkspace().getNamespaceRegistry();

    // do the lookup, so we avoid exceptions
    String[] prefixes = registry.getPrefixes();
    // sort the array
    Arrays.sort( prefixes );

    // unregister namespaces if told so
    if ( forceNamespacesRegistration ) {

      // save the old namespace only if it makes sense
      if ( !keepNewNamespaces ) {
        overwrittenNamespaces = new HashMap<String, String>( namespaces.size() );
      }

      // search occurences
      for ( Object key : namespaces.keySet() ) {
        String prefix = (String) key;
        int position = Arrays.binarySearch( prefixes, prefix );
        if ( position >= 0 ) {
          if ( LOG.isDebugEnabled() ) {
            LOG.debug( "prefix " + prefix + " was already registered; unregistering it" );
          }
          if ( !keepNewNamespaces ) {
            // save old namespace
            overwrittenNamespaces.put( prefix, registry.getURI( prefix ) );
          }
          registry.unregisterNamespace( prefix );
          // postpone registration for later
        }
      }
    }

    // do the registration
    for ( Map.Entry entry : namespaces.entrySet() ) {
      Map.Entry<String, String> namespace = (Map.Entry<String, String>) entry;
      String prefix = (String) namespace.getKey();
      String ns = (String) namespace.getValue();

      int position = Arrays.binarySearch( prefixes, prefix );

      if ( skipExistingNamespaces && position >= 0 ) {
        LOG.debug( "namespace already registered under [" + prefix + "]; skipping registration" );
      } else {
        LOG.debug( "registering namespace [" + ns + "] under [" + prefix + "]" );
        registry.registerNamespace( prefix, ns );
      }
    }

    session.logout();
  }

  /**
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  public void destroy() throws Exception {
    if ( this.adminCredentialsStrategy != null ) {
      unregisterNamespaces();
      unregisterNodeTypes();
    }
  }

  /**
   * Removes the namespaces.
   */
  protected void unregisterNamespaces() throws Exception {

    if ( namespaces == null || namespaces.isEmpty() || keepNewNamespaces ) {
      return;
    }

    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "unregistering custom namespaces " + namespaces );
    }

    NamespaceRegistry registry = getSession().getWorkspace().getNamespaceRegistry();

    for ( Object key : namespaces.keySet() ) {
      String prefix = (String) key;
      registry.unregisterNamespace( prefix );
    }

    if ( forceNamespacesRegistration ) {
      if ( LOG.isDebugEnabled() ) {
        LOG.debug( "reverting back overwritten namespaces " + overwrittenNamespaces );
      }
      if ( overwrittenNamespaces != null ) {
        for ( Map.Entry<String, String> entry : overwrittenNamespaces.entrySet() ) {
          Map.Entry<String, String> namespace = (Map.Entry<String, String>) entry;
          registry.registerNamespace( (String) namespace.getKey(), (String) namespace.getValue() );
        }
      }
    }
  }

  public Session getAdminSession() throws RepositoryException {
    return adminCredentialsStrategy != null ? repository.login( adminCredentialsStrategy.getCredentials(),
      workspaceName ) : null;
  }

  /**
   * @see org.springframework.extensions.jcr.SessionFactory#getSession()
   */
  public Session getSession() throws RepositoryException {
    Credentials creds = credentialsStrategy.getCredentials();
    if ( LOG.isDebugEnabled() ) {
      LOG.debug( "using credentials:" + creds );
    }
    Session session = getSessionFactory().getSession( creds );
    return addListeners( session );
  }

  /**
   * @see org.springframework.extensions.jcr.SessionFactory#getSessionHolder(javax.jcr.Session)
   */
  public SessionHolder getSessionHolder( Session session ) {
    return sessionHolderProvider.createSessionHolder( session );
  }

  /**
   * Hook for adding listeners to the newly returned session. We have to treat exceptions manually and can't rely on the
   * template.
   *
   * @param session JCR session
   * @return the listened session
   */
  protected Session addListeners( Session session ) throws RepositoryException {
    if ( eventListeners != null && eventListeners.length > 0 ) {
      Workspace ws = session.getWorkspace();
      ObservationManager manager = ws.getObservationManager();
      if ( LOG.isDebugEnabled() ) {
        LOG.debug( "adding listeners " + Arrays.asList( eventListeners ).toString() + " for session " + session );
      }

      for ( int i = 0; i < eventListeners.length; i++ ) {
        manager
          .addEventListener( eventListeners[ i ].getListener(), eventListeners[ i ].getEventTypes(), eventListeners[ i ]
              .getAbsPath(), eventListeners[ i ].isDeep(), eventListeners[ i ].getUuid(),
            eventListeners[ i ].getNodeTypeName(), eventListeners[ i ].isNoLocal()
          );
      }
    }
    return session;
  }

  /**
   * @return Returns the repository.
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * @param repository The repository to set.
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  /**
   * @param workspaceName The workspaceName to set.
   */
  public void setWorkspaceName( String workspaceName ) {
    this.workspaceName = workspaceName;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj instanceof JcrSessionFactory ) {
      return ( this.hashCode() == obj.hashCode() );
    }
    return false;

  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int result = 17;
    result = 37 * result + repository.hashCode();
    // add the optional params (can be null)
    if ( workspaceName != null ) {
      result = 37 * result + workspaceName.hashCode();
    }

    return result;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append( "SessionFactory for " );
    buffer.append( getRepositoryInfo() );
    buffer.append( "|workspace=" );
    buffer.append( workspaceName );
    return buffer.toString();
  }

  /**
   * @return Returns the eventListenerDefinitions.
   */
  public EventListenerDefinition[] getEventListeners() {
    return eventListeners;
  }

  /**
   * @param eventListenerDefinitions The eventListenerDefinitions to set.
   */
  public void setEventListeners( EventListenerDefinition[] eventListenerDefinitions ) {
    this.eventListeners = eventListenerDefinitions;
  }

  /**
   * A toString representation of the Repository.
   *
   * @return
   */
  private String getRepositoryInfo() {
    // in case toString() is called before afterPropertiesSet()
    if ( getRepository() == null ) {
      return "<N/A>";
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append( getRepository().getDescriptor( Repository.REP_NAME_DESC ) );
    buffer.append( " " );
    buffer.append( getRepository().getDescriptor( Repository.REP_VERSION_DESC ) );
    return buffer.toString();
  }

  /**
   * @return Returns the namespaces.
   */
  public Properties getNamespaces() {
    return namespaces;
  }

  /**
   * @param namespaces The namespaces to set.
   */
  public void setNamespaces( Properties namespaces ) {
    this.namespaces = namespaces;
  }

  /**
   * Used internally.
   *
   * @return Returns the sessionHolderProvider.
   */
  protected SessionHolderProvider getSessionHolderProvider() {
    return sessionHolderProvider;
  }

  /**
   * Used internally.
   *
   * @param sessionHolderProvider The sessionHolderProvider to set.
   */
  protected void setSessionHolderProvider( SessionHolderProvider sessionHolderProvider ) {
    this.sessionHolderProvider = sessionHolderProvider;
  }

  /**
   * @return Returns the sessionHolderProviderManager.
   */
  public SessionHolderProviderManager getSessionHolderProviderManager() {
    return sessionHolderProviderManager;
  }

  /**
   * @param sessionHolderProviderManager The sessionHolderProviderManager to set.
   */
  public void setSessionHolderProviderManager( SessionHolderProviderManager sessionHolderProviderManager ) {
    this.sessionHolderProviderManager = sessionHolderProviderManager;
  }

  /**
   * Indicate if the given namespace registrations will be kept (the default) when the application context closes down
   * or if they will be unregistered. If unregistered, the namespace mappings that were overriden are registered back to
   * the repository.
   *
   * @param keepNamespaces The keepNamespaces to set.
   * @see #forceNamespacesRegistration
   */
  public void setKeepNewNamespaces( boolean keepNamespaces ) {
    this.keepNewNamespaces = keepNamespaces;
  }

  /**
   * Indicate if the given namespace registrations will override the namespace already registered in the repository
   * under the same prefix. This will cause unregistration for the namespaces that will be modified.
   * <p/>
   * However, depending on the {@link #setKeepNewNamespaces(boolean)} setting, the old namespaces can be registered back
   * once the application context is destroyed. False by default.
   *
   * @param forceNamespacesRegistration The forceNamespacesRegistration to set.
   */
  public void setForceNamespacesRegistration( boolean forceNamespacesRegistration ) {
    this.forceNamespacesRegistration = forceNamespacesRegistration;
  }

  /**
   * Indicate if the given namespace registrations will skip already registered namespaces or not. If true (default),
   * the new namespace will not be registered and the old namespace kept in place. If not skipped, registration of new
   * namespaces will fail if there are already namespace registered under the same prefix.
   * <p/>
   * This flag is required for JCR implementations which do not support namespace unregistration which render the {@link
   * #setForceNamespacesRegistration(boolean)} method useless (as namespace registration cannot be forced).
   *
   * @param skipRegisteredNamespace The skipRegisteredNamespace to set.
   */
  public void setSkipExistingNamespaces( boolean skipRegisteredNamespace ) {
    this.skipExistingNamespaces = skipRegisteredNamespace;
  }

  /**
   * @return Returns the forceNamespacesRegistration.
   */
  public boolean isForceNamespacesRegistration() {
    return forceNamespacesRegistration;
  }

  /**
   * @return Returns the keepNewNamespaces.
   */
  public boolean isKeepNewNamespaces() {
    return keepNewNamespaces;
  }

  /**
   * @return Returns the skipExistingNamespaces.
   */
  public boolean isSkipExistingNamespaces() {
    return skipExistingNamespaces;
  }

  /**
   * @return Returns the workspaceName.
   */
  public String getWorkspaceName() {
    return workspaceName;
  }


}
