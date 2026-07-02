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

import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.xml.XMLProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportXmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 8323789322309175815L;

  public JFreeReportXmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/xml"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".xml"; //$NON-NLS-1$
  }

  @SuppressWarnings( "deprecation" )
  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {
      final XMLProcessor processor = new XMLProcessor( report );
      final OutputStreamWriter writer = new OutputStreamWriter( outputStream );
      processor.setWriter( writer );
      processor.processReport();

      writer.close();
      close();
      return true;
    } catch ( ReportProcessingException e ) {
      error( Messages.getInstance().getString( "JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( IOException e ) {
      error( Messages.getInstance().getString( "JFreeReportXmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    }
  }
}
