package org.apache.jackrabbit.core.security.authorization.acl;

import java.util.Arrays;

import javax.jcr.security.Privilege;

/**
 * A configuration entry that defines a "magic ACE" rule. This is the object representation of rules that reside in 
 * {@code repository.xml}.
 * 
 * @author mlowery
 */
public class MagicAceDefinition {

  public String path;

  public String logicalRole;

  public Privilege[] privileges;

  public boolean applyToChildren;

  public boolean applyToAncestors;
  
  public boolean applyToTarget;
  
  public MagicAceDefinition(final String path, final String logicalRole, final Privilege[] privileges, final boolean applyToTarget, final boolean applyToChildren, final boolean applyToAncestors) {
    super();
    this.path = path;
    this.logicalRole = logicalRole;
    this.privileges = privileges;
    this.applyToChildren = applyToChildren;
    this.applyToAncestors = applyToAncestors;
    this.applyToTarget = applyToTarget;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((logicalRole == null) ? 0 : logicalRole.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + Arrays.hashCode(privileges);
    result = prime * result + (applyToChildren ? 1231 : 1237);
    result = prime * result + (applyToAncestors ? 8 : 9);
    result = prime * result + (applyToTarget ? 6 : 7);
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
    if (applyToChildren != other.applyToChildren)
      return false;
    if (applyToAncestors != other.applyToAncestors)
      return false;
    if (applyToTarget != other.applyToTarget)
      return false;
    return true;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "MagicAceDefinition [path=" + path + ", logicalRole=" + logicalRole + ", privileges="
        + Arrays.toString(privileges) + ", applyToTarget=" + applyToTarget + ", applyToChildren=" + applyToChildren + ", applyToAncestors=" + applyToAncestors + "]";
  }
}
