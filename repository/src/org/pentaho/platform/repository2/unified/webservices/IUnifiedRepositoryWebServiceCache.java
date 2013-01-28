package org.pentaho.platform.repository2.unified.webservices;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class IUnifiedRepositoryWebServiceCache {
  public static IUnifiedRepositoryWebServiceAsync service = null;
  /**
   * Get an instance of this async interface for IUnifiedRepositoryWebService.
   *
   * @param url The url where the remote endpoint is mounted.
   * @return The async instance.
   */

  public static IUnifiedRepositoryWebServiceAsync getService(String url) {
    if (service == null) {
      service = (IUnifiedRepositoryWebServiceAsync) GWT.create(IUnifiedRepositoryWebService.class);
      ServiceDefTarget endpoint = (ServiceDefTarget) service;
      endpoint.setServiceEntryPoint(url);
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
      return getService("http://localhost:8080/pentaho/ws/gwt/unifiedRepository");
    }

    /**
     * Get an instance of this async interface for IUnifiedRepositoryWebService. This method assumes the endpoint is
     * mounted at url + "ws/gwt/unifiedRepository".
     *
     * @return The async instance.
     */
    public static IUnifiedRepositoryWebServiceAsync getServiceRelativeToUrl(String url) {
      return getService(url + "ws/gwt/unifiedRepository");
    }
}
