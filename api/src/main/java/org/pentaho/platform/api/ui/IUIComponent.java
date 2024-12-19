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


package org.pentaho.platform.api.ui;

import org.pentaho.platform.api.engine.IActionRequestHandler;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public interface IUIComponent {

  @SuppressWarnings( "rawtypes" )
  public void handleRequest( OutputStream outputStream, IActionRequestHandler actionRequestHandler, String contentType,
      HashMap requestParameterProviders ) throws IOException;

  public boolean validate();

  /**
   * Set the userSession member, generate a Log Id, set the requestHandler, and validate the component's
   * configuration. NOTE: this method has several side effects not related to validation. could probably use some
   * refactoring
   * 
   * @param session
   * @param actionRequestHandler
   * @return boolean true if component configuration is valid, else false
   */
  public boolean validate( IPentahoSession session, IActionRequestHandler actionRequestHandler );

}
