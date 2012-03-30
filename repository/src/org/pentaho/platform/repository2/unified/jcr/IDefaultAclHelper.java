package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

/**
 * Creates an ACL to use when ACL is not supplied during file/folder creation.
 * 
 * @author mlowery
 */
public interface IDefaultAclHelper {
  RepositoryFileAcl createDefaultAcl();
}
