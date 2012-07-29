package org.pentaho.platform.repository2.unified.fs;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

public class FileSystemFileAclDao implements IRepositoryFileAclDao{

  public void addAce(Serializable fileId, RepositoryFileSid recipient, EnumSet<RepositoryFilePermission> permission) {
    // TODO Auto-generated method stub
    
  }

  public RepositoryFileAcl createAcl(Serializable fileId, RepositoryFileAcl acl) {
    // TODO Auto-generated method stub
    return null;
  }

  public RepositoryFileAcl getAcl(Serializable fileId) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId, boolean forceEntriesInheriting) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasAccess(String relPath, EnumSet<RepositoryFilePermission> permissions) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setFullControl(Serializable fileId, RepositoryFileSid sid, RepositoryFilePermission permission) {
    // TODO Auto-generated method stub
    
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl acl) {
    // TODO Auto-generated method stub
    return null;
  }

}
