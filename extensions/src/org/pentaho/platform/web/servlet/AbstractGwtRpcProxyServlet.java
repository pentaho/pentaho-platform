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

package org.pentaho.platform.web.servlet;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( "serial" )
/**
 * Base class for GWT RPC proxying servlets, which allows developers to write GWT Services as Pojos and have 
 * the GWT client POST requests to subclasses of this class.
 */
public abstract class AbstractGwtRpcProxyServlet extends RemoteServiceServlet {

  private static final Log logger = LogFactory.getLog( AbstractGwtRpcProxyServlet.class );

  public AbstractGwtRpcProxyServlet() {
    super();
  }

  /**
   * Resolve the target impl that ultimately handles the GWT RPC request, provided a key.
   * 
   * @param servletContextPath
   *          the portion of the http request path beyond the servlet context, used to uniquely identify the target impl
   * @return the object that handles the request
   * @throws GwtRpcProxyException
   *           if no target can be found
   */
  protected abstract Object resolveDispatchTarget( String servletContextPath );

  protected void doUnexpectedFailure( Throwable e ) {
    super.doUnexpectedFailure( e );
    logger.error( e );
  }

  /**
   * Returns the dispatch key for this request. This name is the part of the request path beyond the servlet base path.
   * I.e. if the GwtRpcPluginProxyServlet is mapped to the "/gwtrpc" context in web.xml, then this method will return
   * "testservice" upon a request to "http://localhost:8080/pentaho/gwtrpc/testservice".
   * 
   * @return the part of the request url used to dispatch the request
   */
  protected String getDispatchKey() {
    HttpServletRequest request = getThreadLocalRequest();
    // path info will give us what we want with
    String requestPathInfo = request.getPathInfo();
    if ( requestPathInfo.startsWith( "/" ) ) { //$NON-NLS-1$
      requestPathInfo = requestPathInfo.substring( 1 );
    }
    if ( requestPathInfo.contains( "/" ) ) { //$NON-NLS-1$
      // if the request path happens to be multiple levels deep, return the last element in the path
      String[] elements = requestPathInfo.split( "/" ); //$NON-NLS-1$
      return elements[elements.length - 1];
    }
    return requestPathInfo;
  }

  @SuppressWarnings( "nls" )
  protected String getServletContextPath() {
    HttpServletRequest request = getThreadLocalRequest();
    String path = request.getServletPath() + "/" + request.getPathInfo();
    if ( path.contains( "//" ) ) {
      path = path.replaceAll( "//", "/" );
    }
    return path;
  }

  @Override
  public String processCall( String payload ) throws SerializationException {
    Map<Class<?>, Boolean> whitelist = new HashMap<Class<?>, Boolean>();
    whitelist.put( GwtRpcProxyException.class, Boolean.TRUE );
    Map<Class<?>, String> obfuscatedTypeIds = new HashMap<Class<?>, String>();
    StandardSerializationPolicy policy = new StandardSerializationPolicy( whitelist, whitelist, obfuscatedTypeIds );

    String servletContextPath = getServletContextPath();

    Object target = null;
    try {
      target = resolveDispatchTarget( servletContextPath );
    } catch ( GwtRpcProxyException ex ) {
      logger.error( Messages.getInstance().getErrorString(
          "AbstractGwtRpcProxyServlet.ERROR_0001_FAILED_TO_RESOLVE_DISPATCH_TARGET", servletContextPath ), ex ); //$NON-NLS-1$
      return RPC.encodeResponseForFailure( null, ex, policy );
    }

    final ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
    final ClassLoader altLoader = target.getClass().getClassLoader();

    try {
      // temporarily swap out the context classloader to an alternate classloader if
      // the targetBean has been loaded by one other than the context classloader.
      // This is necessary, so the RPC class can do a Class.forName and find the service
      // class specified in the request
      if ( altLoader != origLoader ) {
        Thread.currentThread().setContextClassLoader( altLoader );
      }

      RPCRequest rpcRequest = RPC.decodeRequest( payload, null, this );
      onAfterRequestDeserialized( rpcRequest );
      // don't require the server side to implement the service interface
      Method method = rpcRequest.getMethod();
      try {
        Method m = target.getClass().getMethod( method.getName(), method.getParameterTypes() );
        if ( m != null ) {
          method = m;
        }
      } catch ( Exception e ) {
        e.printStackTrace();
      }
      return RPC.invokeAndEncodeResponse( target, method, rpcRequest.getParameters(), rpcRequest
          .getSerializationPolicy() );
    } catch ( IncompatibleRemoteServiceException ex ) {
      logger.error( Messages.getInstance().getErrorString(
          "AbstractGwtRpcProxyServlet.ERROR_0003_RPC_INVOCATION_FAILED", target.getClass().getName() ), ex ); //$NON-NLS-1$
      return RPC.encodeResponseForFailure( null, ex );
    } finally {
      // reset the context classloader if necessary
      if ( ( altLoader != origLoader ) && origLoader != null ) {
        Thread.currentThread().setContextClassLoader( origLoader );
      }
    }
  }
}
