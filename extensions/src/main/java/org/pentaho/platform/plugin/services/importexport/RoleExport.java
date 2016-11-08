package org.pentaho.platform.plugin.services.importexport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "RoleExport", propOrder = { "rolename", "permissions" } )
public class RoleExport {
  @XmlElement( name="rolename" )
  private String rolename;

  @XmlElement( name="permissions" )
  List<String> permissions = new ArrayList<String>();

  public String getRolename() {
    return rolename;
  }

  public void setRolename( String rolename ) {
    this.rolename = rolename;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  public void setPermission( List<String> permissions ) {
    this.permissions= permissions;
  }
}
