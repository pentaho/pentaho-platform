package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public class DefaultAccessVoter implements IRepositoryAccessVoter{

  @Override
  public boolean hasAccess(RepositoryFile file, RepositoryFilePermission operation, RepositoryFileAcl acl, IPentahoSession session) {
    return true;
  }

}
