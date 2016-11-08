package org.pentaho.platform.web.http.api.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ChangePasswordUser" )
public class ChangePasswordUser {

  private String userName;
  private String newPassword;
  private String oldPassword;

  public ChangePasswordUser() {
  }

  public ChangePasswordUser( String pUserName, String pNewPassword, String pOldPassword ) {
    userName = pUserName;
    newPassword = pNewPassword;
    oldPassword = pOldPassword;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName( String userName ) {
    this.userName = userName;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword( String newPassword ) {
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword( String oldPassword ) {
    this.oldPassword = oldPassword;
  }
}
