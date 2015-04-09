/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A struct-like object for bundling related objects together for import into the platform. Bundles contain all
 * information necessary for import into the system. While this interface includes a hash-map optional parameters
 * function, it should be subclassed if many properties are accessed this way.
 * 
 * @author mlowery, nbaker, tband
 */
public interface IPlatformImportBundle {

  /**
   * This allows for arbitrary parent-child trees to be imported. Note this does not support the folder/file paradigm
   * and is instead a logical relationship between import bundles.
   * 
   * @return a list of "child" bundles.
   */
  List<IPlatformImportBundle> getChildBundles();

  /**
   * Optional content name. Repository content this will be stored based on this name
   * 
   * @return optional name
   */
  String getName();

  /**
   * Path which may be used to indicate where a bundle belongs in the repository.
   * 
   * @param path
   */
  void setPath( String path );

  /**
   * Path which may be used to indicate where a bundle belongs in the repository.
   * 
   * @return path
   */
  String getPath();
  /**
   * Optional InputStream for content with a binary component.
   * 
   * @return optional InputStream
   */
  InputStream getInputStream() throws IOException;

  /**
   * Optional character set for the binary InputStream. UTF-8 will be used by default for in the case of binary text
   * content
   * 
   * @return Optional character set for the associated InputStream
   */
  String getCharset();

  /**
   * mime-type to be used to resolve an IPlatformImportHandler. If not set the IPlatformImporter will attempt to resolve
   * a mime-type based on the configured IPlatformMimeResolver
   * 
   * @return mime-type
   */
  String getMimeType();

  /**
   * Convenience method for extra properties. A subclass would be preferred if there are a great number of properties
   * accessed from this method.
   * 
   * @param prop
   * @return property Object
   */
  Object getProperty( String prop );

  /**
   * pass in flag to allow overwrite in repository (if exists)
   * 
   * @return boolean
   */
  boolean overwriteInRepository();

  /**
   * Ability to use the export manifest during import to apply ACL and File settings
   * 
   * @return
   */
  RepositoryFileAcl getAcl();

  void setAcl( RepositoryFileAcl acl );

  boolean isOverwriteAclSettings();

  /**
   * use the import manifest ACL settings and overwrite existing settings
   * 
   * @param overwriteAclSettings
   */
  void setOverwriteAclSettings( boolean overwriteAclSettings );

  boolean isRetainOwnership();

  /**
   * retain the file metadata ownership
   * 
   * @param retainOwnership
   */
  public abstract void setRetainOwnership( boolean retainOwnership );

  boolean isApplyAclSettings();

  /**
   * use the import manfiest file to apply ACL settings to files and folders
   * 
   * @param applyAclSettings
   */
  void setApplyAclSettings( boolean applyAclSettings );

  /**
   * Preserve DSW OLAP model data
   * @return
   */
  boolean isPreserveDsw();
  /**
   * Preserve DSW OLAP model data
   * @param preserveDsw
   */
  void setPreserveDsw( boolean preserveDsw );
}
