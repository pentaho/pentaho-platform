/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.services.outputhandler;

import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentItem;

public abstract class BaseOutputHandler implements IContentOutputHandler {

  private String instanceId;

  private String mimeType;

  private IPentahoSession session;

  private String handlerId;

  private String solutionPath;

  private String contentRef;

  public abstract IContentItem getFileOutputContentItem();

  /**
   * @return Returns the handlerId.
   */
  public String getHandlerId() {
    return handlerId;
  }

  /**
   * @param handlerId
   *          The handlerId to set.
   */
  public void setHandlerId( final String handlerId ) {
    this.handlerId = handlerId;
  }

  /**
   * @return Returns the instanceId.
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId
   *          The instanceId to set.
   */
  public void setInstanceId( final String instanceId ) {
    this.instanceId = instanceId;
  }

  /**
   * @return Returns the mimeType.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @param mimeType
   *          The mimeType to set.
   */
  public void setMimeType( final String mimeType ) {
    this.mimeType = mimeType;
  }

  /**
   * @return Returns the session.
   */
  public IPentahoSession getSession() {
    return session;
  }

  /**
   * @param session
   *          The session to set.
   */
  public void setSession( final IPentahoSession session ) {
    this.session = session;
  }

  /**
   * @return Returns the solutionPath.
   */
  public String getSolutionPath() {
    return solutionPath;
  }

  /**
   * @param solutionPath
   *          The solutionPath to set.
   */
  public void setSolutionPath( final String solutionPath ) {
    this.solutionPath = solutionPath;
  }

  /**
   * @return Returns the contentRef.
   */
  public String getContentRef() {
    return contentRef;
  }

  /**
   * @param contentRef
   *          The contentRef to set.
   */
  public void setContentRef( final String contentRef ) {
    this.contentRef = contentRef;
  }

  public void setName( String name ) {

  }

}
