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

import java.io.OutputStream;

/**
 * The interface for POJO components that want to stream content to the caller.
 * 
 * @author jamesdixon
 * @deprecated Pojo components are deprecated, use {@link org.pentaho.platform.api.action.IAction}
 * 
 */
@Deprecated
public interface IStreamingPojo {

  /**
   * Sets the outputstream that the component can stream content to
   * 
   * @param outputStream
   */
  public void setOutputStream( OutputStream outputStream );

  /**
   * Gets the mimetype of the content that this object will write to the output stream
   * 
   * @return
   */
  public String getMimeType();

}
