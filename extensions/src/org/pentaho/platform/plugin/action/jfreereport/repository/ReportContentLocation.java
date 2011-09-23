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

import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.io.IOUtils;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.util.IntegerCache;
import org.pentaho.reporting.libraries.repository.ContentCreationException;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentItem;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.LibRepositoryBoot;
import org.pentaho.reporting.libraries.repository.Repository;
import org.pentaho.reporting.libraries.repository.dummy.DummyContentItem;

/**
 * Creation-Date: 05.07.2007, 14:45:06
 *
 * @author Thomas Morgner
 */
public class ReportContentLocation implements ContentLocation {
  private IContentLocation location;

  private ReportContentRepository repository;

  private String actionName;

  public ReportContentLocation(final IContentLocation location, final ReportContentRepository repository,
      final String actionName) {
    this.location = location;
    this.repository = repository;
    this.actionName = actionName;
  }

  public ContentEntity[] listContents() throws ContentIOException {
    final ArrayList itemCollection = new ArrayList();
    final Iterator iterator = this.location.getContentItemIterator();
    while (iterator.hasNext()) {
      final IContentItem item = (IContentItem) iterator.next();
      itemCollection.add(new ReportContentItem(item, this));
    }
    return (ContentEntity[]) itemCollection.toArray(new ContentEntity[itemCollection.size()]);
  }

  public ContentEntity getEntry(final String string) throws ContentIOException {
    final IContentItem rawItem = this.location.getContentItemByName(string);
    if (rawItem == null) {
      throw new ContentIOException(Messages.getInstance().getErrorString("ReportContentLocation.ERROR_0001_NO_ITEM", string)); //$NON-NLS-1$
    }
    return new ReportContentItem(rawItem, this);
  }

  public ContentItem createItem(final String name) throws ContentCreationException {
    final String extension = IOUtils.getInstance().getFileExtension(name);
    final String mimeType = repository.getMimeRegistry().getMimeType(new DummyContentItem(this, name));
    final IContentItem iContentItem = this.location.newContentItem(name, Messages.getInstance()
        .getString("ReportContentLocation.GENERATED_REPORT_CONTENT"), //$NON-NLS-1$
        extension, mimeType, null, IContentItem.WRITEMODE_OVERWRITE);
    return new ReportContentItem(iContentItem, this);
  }

  public ContentLocation createLocation(final String string) throws ContentCreationException {
    throw new ContentCreationException(Messages.getInstance().getErrorString(
        "ReportContentLocation.ERROR_0002_CANT_CREATE_CONTENT_LOCATION", string)); //$NON-NLS-1$
  }

  public boolean exists(final String string) {
    return (this.location.getContentItemByName(string) != null);
  }

  public String getActionName() {
    return actionName;
  }

  public String getName() {
    return this.location.getName();
  }

  public Object getContentId() {
    return this.location.getId();
  }

  public Object getAttribute(final String domain, final String key) {
    if (LibRepositoryBoot.REPOSITORY_DOMAIN.equals(domain)) {
      if (LibRepositoryBoot.VERSION_ATTRIBUTE.equals(key)) {
        return IntegerCache.getInteger(location.getRevision());
      }
    }
    return null;
  }

  public boolean setAttribute(final String domain, final String key, final Object object) {
    return false;
  }

  public ContentLocation getParent() {
    // We have no parent ...
    return null;
  }

  public Repository getRepository() {
    return repository;
  }

  public boolean delete() {
    // cannot be deleted ..
    return false;
  }
}
