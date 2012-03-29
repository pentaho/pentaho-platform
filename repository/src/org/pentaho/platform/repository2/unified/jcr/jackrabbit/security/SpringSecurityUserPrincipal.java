package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import java.security.Principal;

public class SpringSecurityUserPrincipal implements Principal {
  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private String name;

  // ~ Constructors ====================================================================================================

  public SpringSecurityUserPrincipal(final String name) {
    super();
    this.name = name;
  }

  // ~ Methods =========================================================================================================

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
    SpringSecurityUserPrincipal other = (SpringSecurityUserPrincipal) obj;
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
    return "SpringSecurityUserPrincipal[name=" + name + "]";
  }

}
