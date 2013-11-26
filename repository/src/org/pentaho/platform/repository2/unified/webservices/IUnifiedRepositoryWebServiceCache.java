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

package org.pentaho.platform.repository2.unified.webservices;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class IUnifiedRepositoryWebServiceCache {
  public static IUnifiedRepositoryWebServiceAsync service = null;

  /**
   * Get an instance of this async interface for IUnifiedRepositoryWebService.
   * 
   * @param url
   *          The url where the remote endpoint is mounted.
   * @return The async instance.
   */

  public static IUnifiedRepositoryWebServiceAsync getService( String url ) {
    if ( service == null ) {
      service = (IUnifiedRepositoryWebServiceAsync) GWT.create( IUnifiedRepositoryWebService.class );
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      endpoint.setServiceEntryPoint( url );
    }
    return service;
  }

  /**
   * Get an instance of this async interface for IUnifiedRepositoryWebService. This method assumes the endpoint is
   * mounted at "http://localhost:8080/pentaho/ws/gwt/unifiedRepository".
   * 
   * @return The async instance.
   */
  public static IUnifiedRepositoryWebServiceAsync getService() {
    return getService( "http://localhost:8080/pentaho/ws/gwt/unifiedRepository" );
  }

  /**
   * Get an instance of this async interface for IUnifiedRepositoryWebService. This method assumes the endpoint is
   * mounted at url + "ws/gwt/unifiedRepository".
   * 
   * @return The async instance.
   */
  public static IUnifiedRepositoryWebServiceAsync getServiceRelativeToUrl( String url ) {
    return getService( url + "ws/gwt/unifiedRepository" );
  }
}
