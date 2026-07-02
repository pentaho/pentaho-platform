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


package org.pentaho.platform.engine.services.solution;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;

import java.io.OutputStream;
import java.security.InvalidParameterException;

public abstract class SimpleContentGenerator extends BaseContentGenerator {

  private static final long serialVersionUID = -8882315618256741737L;

  @Override
  public void createContent() throws Exception {
    OutputStream out = null;
    if ( outputHandler == null ) {
      error( Messages.getInstance().getErrorString( "SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER" ) ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString(
          "SimpleContentGenerator.ERROR_0001_NO_OUTPUT_HANDLER" ) ); //$NON-NLS-1$
    }

    if ( instanceId == null ) {
      setInstanceId( UUIDUtil.getUUIDAsString() );
    }
    IContentItem contentItem = outputHandler.getOutputContentItem( "response", "content", instanceId, getMimeType() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if ( contentItem == null ) {
      error( Messages.getInstance().getErrorString( "SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM" ) ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString(
          "SimpleContentGenerator.ERROR_0002_NO_CONTENT_ITEM" ) ); //$NON-NLS-1$
    }

    contentItem.setMimeType( getMimeType() );

    out = contentItem.getOutputStream( itemName );
    if ( out == null ) {
      error( Messages.getInstance().getErrorString( "SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM" ) ); //$NON-NLS-1$
      throw new InvalidParameterException( Messages.getInstance().getString(
          "SimpleContentGenerator.ERROR_0003_NO_OUTPUT_STREAM" ) ); //$NON-NLS-1$
    }

    createContent( out );

    try {
      // we created the output stream, let's be sure it's closed
      // do not leave it up to the implementations of SimpleContentGenerator
      // do do this or not
      out.flush();
      out.close();
    } catch ( Exception ignored ) {
      // this is cleanup code anyway, the output stream was probably
      // closed by the impl
    }
  }

  public abstract void createContent( OutputStream out ) throws Exception;

  public abstract String getMimeType();
}
