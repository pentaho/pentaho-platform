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

package org.pentaho.platform.util.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.dom4j.Document;

public class BiPlatformRepositoryClient {

  // key for the original BI platform repository
  public static final String PLATFORMORIG = "PLATFORMORIG"; //$NON-NLS-1$

  private BiPlatformRepositoryClientObjectService objectService;

  private BiPlatformRepositoryClientNavigationService navigationService;

  private String serverUri;

  private String userId;

  private String password;

  public BiPlatformRepositoryClient() {
    objectService = new BiPlatformRepositoryClientObjectService();
    navigationService = new BiPlatformRepositoryClientNavigationService();
  }

  /**
   * Connects to a BI server and gets the repository index document. This can be called again to refresh the
   * document
   */
  public void connect() throws ServiceException {
    GetMethod callMethod = new GetMethod( serverUri + "/SolutionRepositoryService?component=getSolutionRepositoryDoc" ); //$NON-NLS-1$

    HttpClient client = ClientUtil.getClient( userId, password );
    Document doc = ClientUtil.getResultDom4jDocument( client, callMethod );
    objectService.setDoc( doc );
    navigationService.setDoc( doc );

  }

  public String getServerUri() {
    return serverUri;
  }

  public void setServerUri( String serverUri ) {
    this.serverUri = serverUri;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId( String userId ) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public BiPlatformRepositoryClientObjectService getObjectService() {
    return objectService;
  }

  public BiPlatformRepositoryClientNavigationService getNavigationService() {
    return navigationService;
  }

}
