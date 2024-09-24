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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.gwt.rpc;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith( MockitoJUnitRunner.class )
public class SystemGwtRpcTest {

  private HttpServletRequest setupHttpRequest( String servletPath, String pathInfo ) {
    HttpServletRequest httpRequestMock = mock( HttpServletRequest.class );
    when( httpRequestMock.getServletPath() ).thenReturn( servletPath );
    when( httpRequestMock.getPathInfo() ).thenReturn( pathInfo );

    return httpRequestMock;
  }

  // region HttpRequest
  @Test
  public void testGetServletRequest() {
    String servletPath = "/ws";
    String pathInfo = "/gwt/serviceName";

    HttpServletRequest httpRequestMock = setupHttpRequest( servletPath, pathInfo );

    SystemGwtRpc gwtRpc = new SystemGwtRpc( httpRequestMock );

    HttpServletRequest result = gwtRpc.getServletRequest();

    assertEquals( httpRequestMock, result );
  }
  // endregion

  // region Target
  @Test( expected = GwtRpcProxyException.class )
  public void testResolveTargetThrowsWhenServiceIsNotDefined() {
    String servletPath = "/ws";
    String pathInfo = "/gwt/serviceName";
    String serviceKey = "ws-gwt-serviceName";

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( servletPath, pathInfo );

    // ---

    ApplicationContext appContext = mock( ApplicationContext.class );
    when( appContext.containsBean( serviceKey ) ).thenReturn( false );

    // ---

    SystemGwtRpc gwtRpc = spy( new SystemGwtRpc( httpRequestMock ) );
    doReturn( appContext ).when( gwtRpc ).createAppContext();

    // ---

    gwtRpc.resolveTarget();
  }

  @Test( expected = GwtRpcProxyException.class )
  public void testResolveTargetThrowsIfBeanResolveThrowsBeansException() {
    String servletPath = "/ws";
    String pathInfo = "/gwt/serviceName";
    String serviceKey = "ws-gwt-serviceName";

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( servletPath, pathInfo );

    // ---

    BeansException error = mock( BeansException.class );

    ApplicationContext appContext = mock( ApplicationContext.class );
    when( appContext.containsBean( serviceKey ) ).thenReturn( true );
    when( appContext.getBean( serviceKey ) ).thenThrow( error );

    // ---

    SystemGwtRpc gwtRpc = spy( new SystemGwtRpc( httpRequestMock ) );
    doReturn( appContext ).when( gwtRpc ).createAppContext();

    // ---

    gwtRpc.resolveTarget();
  }

  @Test
  public void testResolveTargetWithServiceDefined() {
    String servletPath = "/ws";
    String pathInfo = "/gwt/serviceName";
    String serviceKey = "ws-gwt-serviceName";
    Object serviceTarget = new Object();

    // ---

    HttpServletRequest httpRequestMock = setupHttpRequest( servletPath, pathInfo );

    // ---

    ApplicationContext appContext = mock( ApplicationContext.class );
    when( appContext.containsBean( serviceKey ) ).thenReturn( true );
    when( appContext.getBean( serviceKey ) ).thenReturn( serviceTarget );

    // ---

    SystemGwtRpc gwtRpc = spy( new SystemGwtRpc( httpRequestMock ) );
    doReturn( appContext ).when( gwtRpc ).createAppContext();

    // ---

    Object result = gwtRpc.resolveTarget();

    assertEquals( serviceTarget, result );
  }
  // endregion

  // region Serialization Policy
  @Test
  public void testLoadSerializationPolicy() {
    String servletPath = "/ws";
    String pathInfo = "/gwt/serviceName";

    String moduleContextPath = "/mantle/";
    String strongName = "ABC";
    String policyFilePath = moduleContextPath + strongName + ".gwt.rpc";

    // ---

    InputStream systemPolicyInputStreamMock = mock( InputStream.class );
    ServletContext servletContextMock = mock( ServletContext.class );

    when( servletContextMock.getResourceAsStream( policyFilePath ) )
      .thenReturn( systemPolicyInputStreamMock );

    // ---

    SerializationPolicy systemPolicyMock = mock( SerializationPolicy.class );

    try ( MockedStatic<AbstractGwtRpc> rpc = Mockito.mockStatic( AbstractGwtRpc.class ) ) {
      rpc.when( () -> AbstractGwtRpc.loadSerializationPolicyFromInputStream( any(), eq( policyFilePath ) ) ).thenAnswer( (Answer<SerializationPolicy>) invocationOnMock -> {
        @SuppressWarnings( "unchecked" )
        ThrowingSupplier<InputStream, IOException> inputStreamSupplier =
          (ThrowingSupplier<InputStream, IOException>) invocationOnMock.getArguments()[0];

        InputStream inputStream = inputStreamSupplier.get();
        assertEquals( systemPolicyInputStreamMock, inputStream );

        return systemPolicyMock;
      } );
      // ---

      HttpServletRequest httpRequestMock = setupHttpRequest( servletPath, pathInfo );
      SystemGwtRpc gwtRpcSpy = spy( new SystemGwtRpc( httpRequestMock ) );

      doReturn( servletContextMock ).when( gwtRpcSpy ).getServletContext();

      // ---

      SerializationPolicy result = gwtRpcSpy.loadSerializationPolicy( moduleContextPath, strongName );

      // ---

      assertEquals( systemPolicyMock, result );
      rpc.verify( () -> AbstractGwtRpc.loadSerializationPolicyFromInputStream( any(), eq( policyFilePath ) ), times( 1 ) );
    }
  }
  // endregion
}
