/*
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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 1, 2005 
 * @author Marc Batchelor
 * 
 */

package org.pentaho.platform.api.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;

/**
 * There is no access to this class outside of this package.
 */
public interface IContentItemFile {

  /**
   * @return The file name on disk.
   */
  public String getOsFileName();

  /**
   * @return The path to the file on disk
   */
  public String getOsPath();

  /**
   * @return The action name that created this file
   */
  public String getActionName();

  /**
   * @return The Id of this file
   */
  public String getId();

  /**
   * @return The file size
   */
  public long getFileSize();

  /**
   * @return The file date/time
   */
  public Date getFileDateTime();

  /**
   * @return An input stream from the file on disk
   * @throws ContentException
   */
  public InputStream getInputStream() throws ContentException;

  /**
   * @return A Reader from the file on disk.
   * @throws ContentException
   */
  public Reader getReader() throws ContentException;

  /**
   * @param overWriteOk
   *            Indicates whether overwriting the file on disk is ok.
   *            (determined by the write mode of the containing ContentItem).
   * @return An OutputStream to write to
   * @throws ContentException
   */
  public OutputStream getOutputStream(boolean overWriteOk) throws ContentException;

  /**
   * @param overWriteOk
   *            Indicates whether overwriting the file on disk is ok.
   *            (determined by the write mode of the containing ContentItem).
   * @param append
   *            Indicates whether to append to the file.
   * @return An OutputStream to write to
   * @throws ContentException
   */
  public OutputStream getOutputStream(boolean overWriteOk, boolean append) throws ContentException;

  /**
   * Copies the latest file to another location
   * 
   * @param newFileName
   *            File name to copy to. Must be fully qualified.
   * @return Number of bytes copied
   * @throws ContentException
   */
  public long copyToFile(String newFileName) throws ContentException;

  /**
   * Deletes the file in the Operating System
   * 
   * @return Success indicator
   */
  public boolean deleteOsFile();

}
