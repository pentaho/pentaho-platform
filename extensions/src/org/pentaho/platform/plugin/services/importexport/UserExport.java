package org.pentaho.platform.plugin.services.importexport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "UserExport", propOrder = { "username", "roles" } )
public class UserExport {
  @XmlElement( name="username" )
  String username;
  @XmlElement( name="roles" )
  List<String> roles = new ArrayList<String>();

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRole( String role ) {
    this.roles.add( role );
  }
}
