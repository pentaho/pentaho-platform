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


package org.pentaho.platform.util.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
    HttpGet callMethod = new HttpGet( serverUri + "/SolutionRepositoryService?component=getSolutionRepositoryDoc" ); //$NON-NLS-1$

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
