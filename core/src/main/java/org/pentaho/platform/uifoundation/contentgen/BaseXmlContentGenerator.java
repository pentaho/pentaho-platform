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


package org.pentaho.platform.uifoundation.contentgen;

import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.uifoundation.messages.Messages;

import java.io.OutputStream;

public abstract class BaseXmlContentGenerator extends BaseContentGenerator {

  private static final long serialVersionUID = 2272261269875005948L;

  protected IParameterProvider requestParameters;

  protected IParameterProvider sessionParameters;

  protected abstract String getContent() throws Exception;

  @Override
  public void createContent() throws Exception {

    requestParameters = this.parameterProviders.get( IParameterProvider.SCOPE_REQUEST );
    sessionParameters = this.parameterProviders.get( IParameterProvider.SCOPE_SESSION );

    String content = getContent();

    if ( content == null ) {
      StringBuffer buffer = new StringBuffer();
      PentahoSystem.get( IMessageFormatter.class, userSession ).formatErrorMessage(
          "text/html", Messages.getInstance().getErrorString( "UI.ERROR_0001_CONTENT_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
      content = buffer.toString();
    }

    String intro = ""; //$NON-NLS-1$
    String footer = ""; //$NON-NLS-1$
    IUITemplater templater = PentahoSystem.get( IUITemplater.class, userSession );
    if ( templater != null ) {
      String[] sections = templater.breakTemplate( "template.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
      if ( sections != null && sections.length > 0 ) {
        intro = sections[0];
      }
      if ( sections != null && sections.length > 1 ) {
        footer = sections[1];
      }
    } else {
      intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" ); //$NON-NLS-1$
    }
    IContentItem contentItem =
        outputHandler.getOutputContentItem( IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, "text/html" ); //$NON-NLS-1$
    OutputStream outputStream = contentItem.getOutputStream( null );
    outputStream.write( intro.getBytes() );
    outputStream.write( content.getBytes() );
    outputStream.write( footer.getBytes() );
  }

}
