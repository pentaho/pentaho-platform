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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.jfreereport.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentItem;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.LibRepositoryBoot;
import org.pentaho.reporting.libraries.repository.Repository;

/**
 * Creation-Date: 05.07.2007, 14:54:08
 *
 * @author Thomas Morgner
 */
public class ReportContentItem implements ContentItem {
  private IContentItem backend;

  private ReportContentLocation parent;

  public ReportContentItem(final IContentItem backend, final ReportContentLocation parent) {
    this.backend = backend;
    this.parent = parent;
  }

  public String getMimeType() throws ContentIOException {
    return backend.getMimeType();
  }

  public OutputStream getOutputStream() throws ContentIOException, IOException {
    return backend.getOutputStream(parent.getActionName());
  }

  public InputStream getInputStream() throws ContentIOException, IOException {
    return backend.getInputStream();
  }

  public boolean isReadable() {
    return false;
  }

  public boolean isWriteable() {
    return true;
  }

  public String getName() {
    return backend.getName();
  }

  public Object getContentId() {
    return backend.getId();
  }

  public Object getAttribute(final String domain, final String key) {
    if (LibRepositoryBoot.REPOSITORY_DOMAIN.equals(domain)) {
      if (LibRepositoryBoot.SIZE_ATTRIBUTE.equals(key)) {
        return new Long(backend.getFileSize());
      } else if (LibRepositoryBoot.VERSION_ATTRIBUTE.equals(key)) {
        return backend.getFileDateTime();
      }
    }
    return null;
  }

  public boolean setAttribute(final String domain, final String key, final Object object) {
    return false;
  }

  public ContentLocation getParent() {
    return parent;
  }

  public Repository getRepository() {
    return parent.getRepository();
  }

  public boolean delete() {
    backend.removeVersion(backend.getId());
    return true;
  }
}
