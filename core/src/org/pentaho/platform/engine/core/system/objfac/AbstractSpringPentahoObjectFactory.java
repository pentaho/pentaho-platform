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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 15, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Framework for Spring-based object factories.  Subclasses are required only to implement
 * the init method, which is responsible for setting the {@link ApplicationContext}.
 * <p>
 * A note on creation and management of objects:
 * Object creation and scoping is handled by Spring with one exception: in the case of
 * a {@link StandaloneSession}.  Spring's session scope relates a bean to an {@link HttpSession},
 * and as such it does not know about custom sessions.  The correct approach to solve this problem 
 * is to write a custom Spring scope (called something like "pentahosession").  Unfortunately, we 
 * cannot implement a custom scope to handle the {@link StandaloneSession} because the custom scope
 * would not be able to access it.  There is currently no way to statically obtain a reference to a 
 * pentaho session. So we are left with using custom logic in this factory to execute a different non-Spring logic path
 * when the IPentahoSession is of type StandaloneSession.
 * <p>
 * TODO (BISERVER-2380) remove the custom logic in {@link #retreiveObject(String, IPentahoSession)} and use
 * a custom Spring scope to handle any session types that Spring does not handle out-of-the-box,
 * such as {@link StandaloneSession}.  In order to do this, we need a way to access the
 * current {@link IPentahoSession} from a static context (perhaps a ThreadLocal).
 * 
 * @see IPentahoObjectFactory
 * 
 * @author Aaron Phillips
 */
public abstract class AbstractSpringPentahoObjectFactory implements IPentahoObjectFactory {

  protected ApplicationContext beanFactory;
  protected static final Log logger = LogFactory.getLog(AbstractSpringPentahoObjectFactory.class);

  /**
   * @see IPentahoObjectFactory#get(Class, IPentahoSession)
   */
  public <T> T get(Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException {
    return get(interfaceClass, interfaceClass.getSimpleName(), session);
  }

  /**
   * @see IPentahoObjectFactory#get(Class, String, IPentahoSession)
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> interfaceClass, String key, final IPentahoSession session) throws ObjectFactoryException {
    return (T) retreiveObject(key, session);
  }

  protected Object instanceClass(String key) throws ObjectFactoryException {
    Object object = null;
    try {
      object = beanFactory.getType(key).newInstance();
    } catch (Exception e) {
      String msg = Messages.getInstance().getString("AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_CREATE_OBJECT", key); //$NON-NLS-1$
      throw new ObjectFactoryException(msg, e);
    }
    return object;
  }

  protected Object retrieveViaSpring(String beanId) throws ObjectFactoryException {
    Object object = null;
    try {
      object = beanFactory.getBean(beanId);
    } catch (Throwable t) {
      String msg = Messages.getInstance().getString("AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT", beanId); //$NON-NLS-1$
      throw new ObjectFactoryException(msg,t);
    }
    return object;
  }

  protected Object retreiveObject(String key, final IPentahoSession session) throws ObjectFactoryException {
    //cannot access logger here since this object factory provides the logger
    logger.debug("Attempting to get an instance of [" + key + "] while in session [" + session + "]");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

    Object object = null;

    if (session != null && session instanceof StandaloneSession) {
      //first ask Spring for the object, if it is session scoped it will fail
      //since Spring doesn't know about StandaloneSessions
      try {
        object = beanFactory.getBean(key);
      } catch (Throwable t) {
        //Spring could not create the object, perhaps due to session scoping, let's try
        //retrieving it from our internal session map
        logger.debug("Retrieving object from Pentaho session map (not Spring).");   //$NON-NLS-1$

        object = session.getAttribute(key);

        if ((object == null)) {
          //our internal session map doesn't have it, let's create it
          object = instanceClass(key);
          session.setAttribute(key, object);
        }
      }
    } else {
      //Spring can handle the object retrieval since we are not dealing with StandaloneSession
      object = retrieveViaSpring(key);
    }

    //FIXME: what is this doing here??
    if (object instanceof IPentahoInitializer) {
      ((IPentahoInitializer) object).init(session);
    }

    logger.debug(" Got an instance of [" + key + "]: " + object);   //$NON-NLS-1$ //$NON-NLS-2$
    return object;
  }

  /**
   * @see IPentahoObjectFactory#objectDefined(String)
   */
  public boolean objectDefined(String key) {
    return beanFactory.containsBean(key);
  }

  /**
   * @see IPentahoObjectFactory#getImplementingClass(String)
   */
  @SuppressWarnings("unchecked")
  public Class getImplementingClass(String key) {
    return beanFactory.getType(key);
  }
}