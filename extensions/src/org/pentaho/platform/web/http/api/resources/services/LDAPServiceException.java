package org.pentaho.platform.web.http.api.resources.services;

public class LDAPServiceException extends Exception {
  
  /**
   * 
   */
  private static final long serialVersionUID = 691L;

  public LDAPServiceException(String msg) {
    super(msg);
  }
  
  public LDAPServiceException(Throwable cause) {
    super(cause);
  }
  
  public LDAPServiceException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
  public LDAPServiceException() {
    super();
  }
}