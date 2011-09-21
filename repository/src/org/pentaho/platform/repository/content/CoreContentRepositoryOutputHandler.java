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
 * Copyright 2006-2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.repository.content;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputDef;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;

public class CoreContentRepositoryOutputHandler implements IOutputHandler {

  private boolean responseExpected;
  
  private static final byte[] lock = new byte[0];

  public static final String DefaultMimeType = "application/octet-stream"; //$NON-NLS-1$

  public static final String BackgroundFolder = "background"; //$NON-NLS-1$

  public static final String DefaultExtension = ".bin"; //$NON-NLS-1$

  public static final String FileObjectName = "file"; //$NON-NLS-1$
  public static final String ContentRepoObjectName = "contentrepo"; //$NON-NLS-1$
  
  private boolean contentGenerated;

  private IContentItem outputContentItem;

  private int outputType = IOutputHandler.OUTPUT_TYPE_DEFAULT;

  private String location;

  private String contentGUID;

  //  private String solution;
  private String mimeType;

  private String extension;

  private int writeMode = IContentItem.WRITEMODE_OVERWRITE;

  private IPentahoSession userSession;

  protected IRuntimeContext runtimeContext;

  public CoreContentRepositoryOutputHandler(final String location, final String contentGUID, final String solution,
      final IPentahoSession session) {
    this(location, contentGUID, solution, CoreContentRepositoryOutputHandler.DefaultMimeType,
        CoreContentRepositoryOutputHandler.DefaultExtension, session);
  }

  public CoreContentRepositoryOutputHandler(final String location, final String contentGUID, final String solution,
      final String mimeType, final String extension, final IPentahoSession session) {

    this.contentGUID = contentGUID;
    this.location = location;
    //this.solution = solution;
    this.mimeType = mimeType;
    this.extension = extension;
    userSession = session;
  }

  public void setSession(final IPentahoSession session) {
    this.userSession = session;
  }

  public IPentahoSession getSession() {
    return userSession;
  }

  public void setMimeType(final String value) {
    mimeType = value;
  }

  public void setExtension(final String value) {
    extension = value;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getExtension() {
    return extension;
  }

  public void setWriteMode(final int value) {
    writeMode = value;
  }

  public int getWriteMode() {
    return writeMode;
  }

  public Log getLogger() {
    return LogFactory.getLog(ContentRepositoryOutputHandler.class);
  }

  public boolean allowFeedback() {
    return false;
  }

  public boolean contentDone() {
    return contentGenerated;
  }

  public IContentItem getFeedbackContentItem() {
    return null;
  }

  public IContentItem getOutputContentItem(final String objectName, final String contentName, final String solution,
      final String instanceId, final String inMimeType) {
    return getOutputContentItem(objectName, contentName, contentGUID, null, solution, instanceId, inMimeType);
  }

  public IContentItem getOutputContentItem(final String objectName, final String contentName, final String title,
      final String url, final String solution, final String instanceId, final String inMimeType) {
    contentGenerated = true;
    
    if (FileObjectName.equalsIgnoreCase(objectName) || ContentRepoObjectName.equalsIgnoreCase(objectName)) {
      IContentOutputHandler output = null;
      // this code allows us to stay backwards compatible
      if ((contentName != null) && (contentName.indexOf(":") == -1)) { //$NON-NLS-1$
        output = PentahoSystem.getOutputDestinationFromContentRef(objectName + ":" + contentName, userSession); //$NON-NLS-1$
      } else {
        output = PentahoSystem.getOutputDestinationFromContentRef(contentName, userSession);
        if (output == null) {
          output = PentahoSystem.getOutputDestinationFromContentRef(objectName + ":" + contentName, userSession); //$NON-NLS-1$
        }
      }
      if (output != null) {
        output.setInstanceId(instanceId);
        output.setMimeType(mimeType);
        return output.getFileOutputContentItem();
      }
    }      
    
    if (outputContentItem == null) {
      if (inMimeType != null) {
        this.setMimeType(inMimeType);
      }
      // We need to create one now because someone is asking for it.
      IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, userSession);
      if (contentRepository == null) {
        getLogger().error(Messages.getInstance().getErrorString("RuntimeContext.ERROR_0024_NO_CONTENT_REPOSITORY")); //$NON-NLS-1$
        return null;
      }
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
      synchronized (CoreContentRepositoryOutputHandler.lock) {
        // Find the location if it's already there.
        IContentLocation contentLocation = null;
        try {
          contentLocation = contentRepository.getContentLocationByPath(location);
        } catch (Exception ex) {
          // ignored
        }
        if (contentLocation == null) {
          contentLocation = contentRepository.newContentLocation(location, contentName, contentName, solution, true);
        }

        if (contentLocation == null) {
          getLogger().error(Messages.getInstance().getErrorString("RuntimeContext.ERROR_0025_INVALID_CONTENT_LOCATION")); //$NON-NLS-1$
          return null;
        }

        // Get the content item from the location - if it's there.
        try {
          contentItem = contentLocation.getContentItemByName(contentGUID);
        } catch (Exception ex) {
          // Ignored
        }
        if (contentItem == null) {
          // Create a contentItem with the ID that came from the contentGUID. 
          // This must be a GUID or conflicts in the repository will happen.

          contentItem = contentLocation.newContentItem(contentGUID, contentGUID, title, extension, mimeType, url,
              writeMode);
        }
        outputContentItem = contentItem;
      }
    }
    if (objectName.equals(IOutputHandler.RESPONSE) && contentName.equals(IOutputHandler.CONTENT)) {
      responseExpected = true;
    }  
    return outputContentItem;
  }

  public IOutputDef getOutputDef(final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map getOutputDefs() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getOutputPreference() {
    // TODO Auto-generated method stub
    return outputType;
  }

  public void setContentItem(final IContentItem content, final String objectName, final String contentName) {
    outputContentItem = content;
    if (objectName.equals(IOutputHandler.RESPONSE) && contentName.equals(IOutputHandler.CONTENT)) {
      responseExpected = true;
    }  
  }

  public void setOutput(final String name, final Object value) {
    if (IOutputHandler.CONTENT.equalsIgnoreCase(name)) {
      if (value instanceof IContentItem) {
        outputContentItem = (IContentItem) value;
        contentGenerated = true;
      }
      responseExpected = true;
    }
  }

  public void setOutputPreference(final int value) {
    this.outputType = value;
  }

  public IMimeTypeListener getMimeTypeListener() {
    return null;
  }

  public void setMimeTypeListener(final IMimeTypeListener mimeTypeListener) {
    // ignored
  }

  public void setRuntimeContext(final IRuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }

  public boolean isResponseExpected() {
    return responseExpected;
  }

}
