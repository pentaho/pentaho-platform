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


package org.pentaho.platform.plugin.action.builtin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.actions.TemplateMsgAction;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.OutputStream;
import java.util.Set;

public class TemplateComponent extends ComponentBase {

  private static final String TEMPLATE = "template"; //$NON-NLS-1$

  /**
   * 
   */
  private static final long serialVersionUID = 4383466190328580251L;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( TemplateComponent.class );
  }

  @Override
  protected boolean validateAction() {

    // see if we have a template defined
    TemplateMsgAction actionDefinition = (TemplateMsgAction) getActionDefinition();

    boolean templateOk = false;
    if ( null != actionDefinition.getTemplate() ) {
      templateOk = true;
    } else if ( isDefinedResource( TemplateComponent.TEMPLATE ) ) {
      templateOk = true;
    }

    if ( !templateOk ) {
      error( Messages.getInstance().getString( "Template.ERROR_0001_TEMPLATE_NOT_DEFINED" ) ); //$NON-NLS-1$
      return false;
    }
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) || ( outputs.size() > 1 ) ) {
      error( Messages.getInstance().getString( "Template.ERROR_0002_OUTPUT_COUNT_WRONG" ) ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // nothing to do here
    return true;
  }

  @Override
  public void done() {
    // nothing to do here
  }

  @Override
  protected boolean executeAction() {

    try {

      TemplateMsgAction actionDefinition = (TemplateMsgAction) getActionDefinition();
      String template = null;

      template = actionDefinition.getTemplate().getStringValue();
      if ( ( null == template ) && isDefinedResource( TemplateComponent.TEMPLATE ) ) {
        IActionSequenceResource resource = getResource( "template" ); //$NON-NLS-1$
        template = getResourceAsString( resource );
      }

      String outputName = (String) getOutputNames().iterator().next();
      IActionParameter outputParam = getOutputItem( outputName );

      if ( outputParam.getType().equals( IActionParameter.TYPE_CONTENT ) ) {

        String mimeType = actionDefinition.getMimeType().getStringValue();
        String extension = actionDefinition.getExtension().getStringValue();

        // This would prevent null values being passed as parameters to getOutputItem
        if ( mimeType == null ) {
          mimeType = ""; //$NON-NLS-1$
        }

        if ( extension == null ) {
          extension = ""; //$NON-NLS-1$
        }

        // Removing the null check here because if we avoid null exception it gives misleading hibernate
        // stale data exception which has nothing to do with a report that simply reads data.
        IContentItem outputItem = getOutputContentItem( outputName, mimeType );
        // IContentItem outputItem = getOutputItem(outputName, mimeType, extension);
        OutputStream outputStream = outputItem.getOutputStream( getActionName() );

        outputStream.write( applyInputsToFormat( template ).getBytes( LocaleHelper.getSystemEncoding() ) );
        outputItem.closeOutputStream();
        return true;
      } else {
        setOutputValue( outputName, applyInputsToFormat( template ) );
      }

      return true;
    } catch ( Exception e ) {
      error( Messages.getInstance().getString( "Template.ERROR_0004_COULD_NOT_FORMAT_TEMPLATE" ), e ); //$NON-NLS-1$
      return false;
    }

  }

  @Override
  public boolean init() {
    // nothing to do here
    return true;
  }

}
