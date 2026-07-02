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


package org.pentaho.platform.plugin.action.jfreereport.outputs;

import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;

import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:50:22
 * 
 * @author Thomas Morgner
 */
public abstract class AbstractGenerateStreamContentComponent extends AbstractGenerateContentComponent {

  private static final long serialVersionUID = -4562767913928444314L;
  private IContentItem contentItem = null;

  protected AbstractGenerateStreamContentComponent() {
  }

  @Override
  protected boolean validateAction() {
    if ( !( super.validateAction() ) ) {
      return false;
    }

    if ( isDefinedOutput( AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT ) ) {
      return true;
    }

    if ( getOutputNames().size() == 1 ) {
      return true;
    }

    if ( getOutputNames().size() == 0 ) {
      warn( Messages.getInstance().getString( "Base.WARN_NO_OUTPUT_STREAM" ) ); //$NON-NLS-1$
      return true;
    }

    warn( Messages.getInstance().getString(
      "AbstractGenerateStreamContentComponent.JFreeReport.ERROR_0038_NO_OUTPUT_DEFINED" ) ); //$NON-NLS-1$
    return false;
  }

  protected abstract String getMimeType();

  protected abstract String getExtension();

  @Override
  protected final boolean performExport( final MasterReport report ) {
    OutputStream outputStream = createOutputStream();
    if ( outputStream == null ) {
      // We could not get an output stream for the content
      error( Messages.getInstance().getErrorString( "JFreeReport.ERROR_0008_INVALID_OUTPUT_STREAM" ) ); //$NON-NLS-1$
      return false;
    }

    return performExport( report, outputStream );
  }

  protected final void close() {
    if ( contentItem != null ) {
      contentItem.closeOutputStream();
    }
  }

  protected abstract boolean performExport( final MasterReport report, final OutputStream outputStream );

  @SuppressWarnings( "deprecation" )
  protected OutputStream createOutputStream() {
    // Try to get the output from the action-sequence document.
    final String mimeType = getMimeType();

    if ( isDefinedOutput( AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT ) ) {
      contentItem =
          getOutputItem( AbstractJFreeReportComponent.REPORTGENERATESTREAM_REPORT_OUTPUT, mimeType, getExtension() );
      try {
        contentItem.setMimeType( mimeType );
        return contentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        return null;
      }
    } else if ( getOutputNames().size() == 1 ) {
      String outputName = (String) getOutputNames().iterator().next();
      contentItem = getOutputContentItem( outputName, mimeType );
      try {
        contentItem.setMimeType( mimeType );
        return contentItem.getOutputStream( getActionName() );
      } catch ( Exception e ) {
        return null;
      }
    }
    if ( getOutputNames().size() == 0 ) {
      // There was no output in the action-sequence document, so make a
      // default
      // outputStream.
      final OutputStream outputStream = getDefaultOutputStream( mimeType );
      return outputStream;
    }

    return null;
  }

  protected IContentItem getContentItem() {
    return contentItem;
  }

}
