package org.apache.jackrabbit.core.security.authorization.acl;

import java.util.Arrays;

import javax.jcr.security.Privilege;

/**
 * A configuration entry that defines a "magic ACE" rule.
 * 
 * @author mlowery
 */
public class MagicAceDefinition {

  public String path;

  public String logicalRole;

  public Privilege[] privileges;

  public boolean recursive;

  public MagicAceDefinition(final String path, final String logicalRole, final Privilege[] privileges,
      final boolean recursive) {
    super();
    this.path = path;
    this.logicalRole = logicalRole;
    this.privileges = privileges;
    this.recursive = recursive;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((logicalRole == null) ? 0 : logicalRole.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + Arrays.hashCode(privileges);
    result = prime * result + (recursive ? 1231 : 1237);
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
    MagicAceDefinition other = (MagicAceDefinition) obj;
    if (logicalRole == null) {
      if (other.logicalRole != null)
        return false;
    } else if (!logicalRole.equals(other.logicalRole))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (!Arrays.equals(privileges, other.privileges))
      return false;
    if (recursive != other.recursive)
      return false;
    return true;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "MagicAceDefinition [path=" + path + ", logicalRole=" + logicalRole + ", privileges="
        + Arrays.toString(privileges) + ", recursive=" + recursive + "]";
  }
}
