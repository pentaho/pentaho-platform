package org.pentaho.platform.plugin.action.mondrian.catalog;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IAclNodeHelper;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

import java.io.InputStream;

/**
 * This interface is a temporary solution created to keep backwards compatibility prior to 6.0<br><b>Note: This
 * interface will be removed in 6.0</b>
 *
 * @author Andrey Khayrutdinov
 */
public interface IAclAwareMondrianCatalogService extends IMondrianCatalogService {

  /**
   * Pass the input stream directly from data access PUC and schema workbench
   *
   * @param inputStream           stream
   * @param catalog               catalog
   * @param overwriteInRepository flag, defining to overwrite existing or not
   * @param acl                   catalog ACL, <tt>null</tt> means no ACL
   * @param session               user session
   */
  void addCatalog( InputStream inputStream, MondrianCatalog catalog, boolean overwriteInRepository,
                   RepositoryFileAcl acl, IPentahoSession session );

  void setAclFor( String catalogName, RepositoryFileAcl acl );

  RepositoryFileAcl getAclFor( String catalogName );
}
