package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.EnumSet;

public class RepositoryFileAce implements Serializable {
  private RepositoryFileSid recipient;

  private EnumSet<RepositoryFilePermission> permissions;

  public RepositoryFileAce(final RepositoryFileSid recipient, final RepositoryFilePermission first,
      final RepositoryFilePermission... rest) {
    this(recipient, EnumSet.of(first, rest));
  }

  public RepositoryFileAce(final RepositoryFileSid recipient, final EnumSet<RepositoryFilePermission> permissions) {
    super();
    this.recipient = recipient;
    this.permissions = permissions;
  }

  public RepositoryFileSid getSid() {
    return recipient;
  }

  public EnumSet<RepositoryFilePermission> getPermissions() {
    return permissions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
    result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
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
    RepositoryFileAce other = (RepositoryFileAce) obj;
    if (permissions == null) {
      if (other.permissions != null)
        return false;
    } else if (!permissions.equals(other.permissions))
      return false;
    if (recipient == null) {
      if (other.recipient != null)
        return false;
    } else if (!recipient.equals(other.recipient))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileAce[recipient=" + recipient + ", permissions=" + permissions + "]";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
  }

}
