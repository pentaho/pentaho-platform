package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;

/**
 * Repository file security identifier (SID) used as file owners and permission recipients in access control entries.
 * 
 * <p>
 * Same abstraction as {@code org.springframework.security.acls.sid.Sid}.
 * </p>
 */
public class RepositoryFileSid implements Serializable {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = 6081475460363558001L;

  // ~ Instance fields =================================================================================================

  private final String name;

  private final RepositoryFileSid.Type type;

  // ~ Constructors ====================================================================================================

  /**
   * Creates a new Sid with given name and type of {@link Type#USER}.
   */
  public RepositoryFileSid(final String name) {
    this(name, Type.USER);
  }

  public RepositoryFileSid(final String name, final RepositoryFileSid.Type type) {
    super();
    notNull(name);
    notNull(type);
    this.name = name;
    this.type = type;
  }

  // ~ Methods =========================================================================================================

  private void notNull(final Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException();
    }
  }
  
  public String getName() {
    return name;
  }
  
  public RepositoryFileSid.Type getType() {
    return type;
  }

  public static enum Type {
    USER, ROLE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + type.hashCode();
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
    RepositoryFileSid other = (RepositoryFileSid) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileSid[name=" + name + ", type=" + type + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}