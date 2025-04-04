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


package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.repository.IContentItem;

/**
 * Interface for a class used to stream contents from a Hitachi Vantara action sequence to a destination of some kind, e.g.
 * file, database, content management system (CMS)
 * 
 * @author jdixon
 * 
 */
public interface IContentOutputHandler extends IMimeTypeListener {

  /**
   * Returns a content item that can be used to stream content to a destination of some kind
   */
  public IContentItem getFileOutputContentItem();

  /**
   * @return Returns the handlerId.
   */
  public String getHandlerId();

  /**
   * @param handlerId
   *          The handlerId to set.
   */
  public void setHandlerId( String handlerId );

  /**
   * @return Returns the instanceId.
   */
  public String getInstanceId();

  /**
   * @param instanceId
   *          The instanceId to set.
   */
  public void setInstanceId( String instanceId );

  /**
   * @return Returns the mimeType.
   */
  public String getMimeType();

  /**
   * @param mimeType
   *          The mimeType to set.
   */
  public void setMimeType( String mimeType );

  /**
   * @return Returns the session.
   */
  public IPentahoSession getSession();

  /**
   * @param session
   *          The session to set.
   */
  public void setSession( IPentahoSession session );

  /**
   * @return Returns the solutionPath.
   */
  public String getSolutionPath();

  /**
   * @param solutionPath
   *          The solutionPath to set.
   */
  public void setSolutionPath( String solutionPath );

  /**
   * @return Returns the contentRef.
   */
  public String getContentRef();

  /**
   * @param contentRef
   *          The path and name of the content to be stored. This comes from the <file> definition in the outputs
   *          section of the Action Sequence
   */
  public void setContentRef( String contentRef );

}
