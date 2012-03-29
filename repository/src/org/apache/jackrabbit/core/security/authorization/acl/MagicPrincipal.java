package org.apache.jackrabbit.core.security.authorization.acl;

import org.pentaho.platform.repository2.unified.jcr.PentahoInternalPrincipal;

/**
 * A principal that goes into "magic ACEs."
 * <p>This could be accomplished with an anonymous class but this is more readable for debugging.</p>
 * 
 * @author mlowery
 */
public class MagicPrincipal implements PentahoInternalPrincipal {

  private String name;

  public MagicPrincipal(final String name) {
    super();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    MagicPrincipal other = (MagicPrincipal) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "MagicPrincipal [name=" + name + "]";
  }

}
