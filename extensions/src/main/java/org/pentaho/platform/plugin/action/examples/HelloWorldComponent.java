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


package org.pentaho.platform.plugin.action.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.HelloWorldAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.OutputStream;

/**
 * @author James Dixon
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class HelloWorldComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = 9050456842938084174L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( HelloWorldComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    boolean result = true;
    if ( !( getActionDefinition() instanceof HelloWorldAction ) ) {
      error( Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$
      result = false;
    }
    return result;
  }

  @Override
  public void done() {
  }

  @SuppressWarnings( "deprecation" )
  @Override
  protected boolean executeAction() {
    HelloWorldAction helloWorldAction = (HelloWorldAction) getActionDefinition();
    boolean result = true;

    // return the quote as the result of this component
    String msg =
        Messages.getInstance().getString(
            "HelloWorld.USER_HELLO_WORLD_TEXT", helloWorldAction.getQuote().getStringValue( "" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    OutputStream outputStream = getDefaultOutputStream( "text/html" ); //$NON-NLS-1$
    if ( outputStream != null ) {
      try {
        outputStream.write( msg.getBytes( LocaleHelper.getSystemEncoding() ) );
      } catch ( Exception e ) {
        error( Messages.getInstance().getErrorString( "HelloWorld.ERROR_0001_COULDNOTWRITE" ), e ); //$NON-NLS-1$
        result = false;
      }
    }

    info( msg );
    return result;
  }

  @Override
  public boolean init() {

    // nothing to do here really
    return true;
  }

}
