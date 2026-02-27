/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRuntimeElement;
import org.pentaho.platform.api.repository.IRuntimeRepository;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.SimpleRuntimeElement;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author James Dixon
 * 
 *         This is a lightweight version on the runtime repository that will not persist any runtime elements. This
 *         class is intended for simple standalone applications that do not require workflow
 */
public class SimpleRuntimeRepository extends PentahoBase implements IRuntimeRepository {

  /**
   * 
   */
  private static final long serialVersionUID = -6093228119094501691L;

  private static final boolean debug = PentahoSystem.debug;

  private static final Log log = LogFactory.getLog( SimpleRuntimeRepository.class );

  private static final ThreadLocal threadSession = new ThreadLocal();

  /**
   * @return Returns the userSession.
   */
  public static IPentahoSession getUserSession() {
    IPentahoSession userSession = (IPentahoSession) SimpleRuntimeRepository.threadSession.get();
    return userSession;
  }

  public SimpleRuntimeRepository() {

  }

  public List getMessages() {
    return null;
  }

  public void setSession( final IPentahoSession sess ) {
    SimpleRuntimeRepository.threadSession.set( sess );
    genLogIdFromSession( SimpleRuntimeRepository.getUserSession() );
  }

  /**
   * Loads an existing RuntimeElement
   * 
   * @param instId
   *          The instance Id
   * @return the RuntimeElement
   * @throws RepositoryException
   */
  public IRuntimeElement loadElementById( final String instanceId, final Collection allowableReadAttributeNames )
    throws RepositoryException {
    if ( SimpleRuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_CREATE_INSTANCE", instanceId ) ); //$NON-NLS-1$
    }
    SimpleRuntimeElement re = new SimpleRuntimeElement( instanceId );
    return re;
  }

  /**
   * 
   * Creates a new RuntimeElement
   * 
   * @param parId
   *          Parent ID of this instance
   * @param parType
   *          Parent type of the instance
   * @return the created runtime element
   */
  public IRuntimeElement newRuntimeElement( final String parId, final String parType, final boolean transientOnly ) {
    if ( SimpleRuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_NEW_ELEMENT_PARENT", parId, parType ) ); //$NON-NLS-1$
    }
    String instanceId = UUIDUtil.getUUIDAsString();
    if ( SimpleRuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_CREATE_INSTANCE", instanceId ) ); //$NON-NLS-1$
    }
    SimpleRuntimeElement re = new SimpleRuntimeElement( instanceId, parId, parType );
    return re;
  }

  /**
   * 
   * Creates a new RuntimeElement
   * 
   * @param parId
   *          Parent Id of the runtime element
   * @param parType
   *          Parent type of the runtime element
   * @param solnId
   *          Solution Id of the element
   * @return The created runtime element
   */
  public IRuntimeElement newRuntimeElement( final String parId, final String parType, final String solnId,
      final boolean transientOnly ) {
    if ( SimpleRuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_NEW_ELEMENT_PARENT_SOLN", parId, parType, solnId ) ); //$NON-NLS-1$
    }
    String instanceId = UUIDUtil.getUUIDAsString();
    if ( SimpleRuntimeRepository.debug ) {
      debug( Messages.getInstance().getString( "RTREPO.DEBUG_CREATE_INSTANCE", instanceId ) ); //$NON-NLS-1$
    }
    SimpleRuntimeElement re = new SimpleRuntimeElement( instanceId, parId, parType, solnId );
    return re;
  }

  /* ILogger Needs */
  @Override
  public Log getLogger() {
    return SimpleRuntimeRepository.log;
  }

  public boolean usesHibernate() {
    return false;
  }

  public void exitPoint() {

  }

}
