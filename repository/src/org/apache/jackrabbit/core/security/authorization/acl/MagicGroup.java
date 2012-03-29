package org.apache.jackrabbit.core.security.authorization.acl;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import org.pentaho.platform.repository2.unified.jcr.PentahoInternalPrincipal;

public class MagicGroup implements Group, PentahoInternalPrincipal {

  private Group group;
  
  public MagicGroup(final Group group) {
    this.group = group;
  }

  @Override
  public String getName() {
    return group.getName();
  }
  
  @Override
  public boolean addMember(Principal arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMember(Principal arg0) {
    return group.isMember(arg0);
  }

  @Override
  public Enumeration<? extends Principal> members() {
    return group.members();
  }

  @Override
  public boolean removeMember(Principal arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MagicGroup other = (MagicGroup) obj;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;
    return true;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "MagicGroup [group=" + group + "]";
  }

}
