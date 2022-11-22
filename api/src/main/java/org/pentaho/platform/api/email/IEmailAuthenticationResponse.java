package org.pentaho.platform.api.email;

public interface IEmailAuthenticationResponse {

  public String getAccess_token();

  public void setAccess_token( final String access_token );

  public String getToken_type();

  public void setToken_type( final String token_type );

  public Integer getExpires_in();

  public void setExpires_in( final Integer expires_in );

  public Integer getExt_expires_in();

  public void setExt_expires_in( final Integer ext_expires_in );

  public String getRefresh_token();

  public void setRefresh_token( final String refresh_token );

  public String getScope();

  public void setScope( String scope );

  public String getId_token();

  public void setId_token( String id_token );
}
