/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.repository2.unified.importexport;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;

/**
 * A source of files for import operations.
 * 
 * @author mlowery
 */
public interface ImportSource {
	 
  void initialize(IUnifiedRepository repository);

  IRepositoryFileBundle getFile(final String path);

  Iterable<IRepositoryFileBundle> getFiles() throws IOException;
  
  void addFile(IRepositoryFileBundle file);
  
  List<ImportSource> getDependentImportSources();

  /**
   * ImportSource instances are expected to present bytes in InputStreams as encoded by the given charset.
   */
  void setRequiredCharset(final String charset);
  
  String getRequiredCharset();
  
  /**
   * Assists ImportSource instances in the creation of ACLs.
   */
  void setOwnerName(final String ownerName);

  /**
   * A struct-like object for bundling related objects together.
   * 
   * @author mlowery
   */
  interface IRepositoryFileBundle {

    RepositoryFile getFile();

    RepositoryFileAcl getAcl();

    /**
     * Gets the stream (if !getFile().isFolder()).
     */
    InputStream getInputStream() throws IOException;

    /**
     * Path to file. Will be appended to destination folder path to create absolute path.
     */
    String getPath();
    
    void setPath(String path);
    
    /**
     * Null for folders or binary types.
     */
    String getCharset();

    String getMimeType();

  }
}
