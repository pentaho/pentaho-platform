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

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.repository.IContentItem;

/**
 * Interface for a class used to stream contents from a Pentaho action sequence to a destination of some kind, e.g.
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
