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
 *
 * 
 * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Dec 21, 2006 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.repository.content;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.outputhandler.BaseOutputHandler;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class ContentRepositoryOutputHandler extends BaseOutputHandler {

  private static final byte[] lock = new byte[0];

  @Override
  public IContentItem getFileOutputContentItem() {

    String contentRef = getContentRef();
    // get an output stream to hand to the caller
    IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, getSession());
    if (contentRepository == null) {
      Logger.error(this.getClass().getName(), Messages.getInstance()
          .getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY")); //$NON-NLS-1$
      return null;
    }
    String extension = ""; //$NON-NLS-1$
    int idx1 = contentRef.lastIndexOf("."); //$NON-NLS-1$
    if (idx1 != -1) {
      extension = contentRef.substring(idx1);
    } else {
      idx1 = contentRef.length();
    }
    String extensionFolder = extension;
    if (extensionFolder.startsWith(".")) { //$NON-NLS-1$
      extensionFolder = extensionFolder.substring(1);
    }
    int idx2 = contentRef.lastIndexOf("/"); //$NON-NLS-1$
    String outputFolder = ""; //$NON-NLS-1$
    String itemName = contentRef;
    if (idx2 != -1) {
      outputFolder = contentRef.substring(0, idx2);
    }
    itemName = contentRef.substring(idx2 + 1, idx1);
    String contentPath = outputFolder + "/" + itemName + "/" + extensionFolder; //$NON-NLS-1$ //$NON-NLS-2$ 
    IContentItem contentItem = null;
    //
    // Synchronizing solves a nasty race condition when
    // multiple simultaneous threads ask Hibernate if a
    // specific Location/Item exists. In all cases, the
    // answer will be no, so they all create the corresponding
    // object and tell Hibernate to save it. The end-result is
    // exceptions thrown from the database for key-constraint violations.
    // Synchronizing down to the create of the item will make sure
    // that all pending saves get persistent.
    //
    synchronized (ContentRepositoryOutputHandler.lock) {
      // Find the location if it's already there.     
      IContentLocation contentLocation = null;
      try {
        contentLocation = contentRepository.getContentLocationByPath(contentPath);
      } catch (Exception ex) {
        Logger.debug(this.getClass().getName(), contentPath, ex);
      }
      if (contentLocation == null) {
        //  Logger.debug(this.getClass().getName(),"******** New Location: " + contentPath + " - Thread: " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        contentLocation = contentRepository.newContentLocation(contentPath, contentRef, contentRef, "",
            true);
      }
      if (contentLocation == null) {
        Logger.error(this.getClass().getName(), Messages.getInstance()
            .getErrorString("RuntimeContext.ERROR_0025_INVALID_CONTENT_LOCATION")); //$NON-NLS-1$
        return null;
      }
      // TODO support content expiration

      // TODO make the write mode based on the output definition

      // Get the content item from the location - if it's there.
      try {
        contentItem = contentLocation.getContentItemByName(getInstanceId());
      } catch (Exception ex) {
        Logger.debug(this.getClass().getName(), getInstanceId(), ex);
      }
      if (contentItem == null) { // DM - Need to keep versions so each report
        // in a burst gets saved
        contentItem = contentLocation.newContentItem(getInstanceId(), contentRef, extension, getMimeType(), null,
            IContentItem.WRITEMODE_KEEPVERSIONS);
      }
    }
    return contentItem;
  }

}
