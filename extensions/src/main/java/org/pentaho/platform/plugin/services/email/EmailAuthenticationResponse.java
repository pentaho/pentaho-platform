package org.pentaho.platform.plugin.services.email;

import org.pentaho.platform.api.email.IEmailAuthenticationResponse;

public class EmailAuthenticationResponse implements IEmailAuthenticationResponse {
  private String access_token;
  private String token_type;
  private Integer expires_in;
  private Integer ext_expires_in;
  private String refresh_token;
  private String scope;
  private String id_token;

  public String getAccess_token() {
    return access_token == null ? "" : access_token;
  }

  public void setAccess_token( final String access_token ) {
    this.access_token = access_token;
  }

  public String getToken_type() {
    return token_type == null ? "" : token_type;
  }

  public void setToken_type( final String token_type ) {
    this.token_type = token_type;
  }

  public Integer getExpires_in() {
    return expires_in;
  }

  public void setExpires_in( final Integer expires_in ) {
    this.expires_in = expires_in;
  }

  public Integer getExt_expires_in() {
    return ext_expires_in;
  }

  public void setExt_expires_in( final Integer ext_expires_in ) {
    this.ext_expires_in = ext_expires_in;
  }

  public String getRefresh_token() {
    return refresh_token;
  }

  public void setRefresh_token( final String refresh_token ) {
    this.refresh_token = refresh_token;
  }

  public String getScope() {
    return scope;
  }

  public void setScope( String scope ) {
    this.scope = scope;
  }

  public String getId_token() {
    return id_token;
  }

  public void setId_token( String id_token ) {
    this.id_token = id_token;
  }
}
