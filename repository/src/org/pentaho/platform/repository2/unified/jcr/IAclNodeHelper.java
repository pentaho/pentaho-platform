package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

/**
 * The interface for operations over ACL nodes.
 *
 * @author Andrey Khayrutdinov
 */
public interface IAclNodeHelper {

  /**
   * Returns <tt>true</tt> if the current user has access to <tt>dataSourceName</tt>.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   * @return <tt>true</tt> if the user can access the data source
   */
  boolean hasAccess( String dataSourceName, DatasourceType type );

  /**
   * Returns an ACL rules for <tt>dataSourceName</tt>. If none exists, <tt>null</tt> is returned. <b>Note:</b> this
   * method should be invoked with 'repository admin' privileges.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   * @return ACL rules if exist or <tt>null</tt> otherwise
   */
  RepositoryFileAcl getAclFor( String dataSourceName, DatasourceType type );

  /**
   * Sets <tt>acl</tt> for <tt>dataSourceName</tt>. If a ACL node does not exist, it is created. If <tt>acl</tt> is
   * <tt>null</tt>, the ACL node is removed. <b>Note:</b> this method should be invoked with 'repository admin'
   * privileges.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   * @param acl            an ACL rules for the data source
   */
  void setAclFor( String dataSourceName, DatasourceType type, RepositoryFileAcl acl );

  /**
   * Makes the <tt>dataSourceName</tt> public by removing corresponding ACL node. <b>Note:</b> this method should be
   * invoked with 'repository admin' privileges.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   */
  void publishDatasource( String dataSourceName, DatasourceType type );

  /**
   * Removes, if it exists, a ACL node related to the <tt>dataSourceName</tt>. Internally it simply calls
   * <code>setAcl(null)</code>. <b>Note:</b> this method should be invoked with 'repository admin' privileges.
   *
   * @param dataSourceName data source
   * @param type           data source's type
   */
  void removeAclNodeFor( String dataSourceName, DatasourceType type );

  /**
   * Returns a path where ACL nodes are created.
   *
   * @return ACL nodes folder's path
   */
  String getAclNodeFolder();

  enum DatasourceType {
    MONDRIAN {
      @Override String resolveName( String dataSourceName ) {
        return String.format( "%s.mondrian.acl", dataSourceName );
      }
    },

    METADATA {
      @Override String resolveName( String dataSourceName ) {
        return String.format( "%s.metadata.acl", dataSourceName );
      }
    };


    abstract String resolveName( String dataSourceName );
  }

}
