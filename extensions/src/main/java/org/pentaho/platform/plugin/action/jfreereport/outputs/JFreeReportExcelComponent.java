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

import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.FlowReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.FlowExcelOutputProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportExcelComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -2130145967763406737L;

  public JFreeReportExcelComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/vnd.ms-excel"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".xls"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {
      final FlowExcelOutputProcessor target =
          new FlowExcelOutputProcessor( report.getConfiguration(), outputStream, report.getResourceManager() );
      final FlowReportProcessor reportProcessor = new FlowReportProcessor( report, target );

      if ( isDefinedInput( AbstractJFreeReportComponent.WORKBOOK_PARAM ) ) {
        try {
          final InputStream inputStream = getInputStream( AbstractJFreeReportComponent.WORKBOOK_PARAM );
          target.setTemplateInputStream( inputStream );
        } catch ( Exception e ) {
          error(
              Messages.getInstance().getString( "JFreeReportExcelComponent.ERROR_0037_ERROR_READING_REPORT_INPUT" ), e ); //$NON-NLS-1$
          return false;
        }
      }

      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        reportProcessor.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      reportProcessor.processReport();
      reportProcessor.close();
      outputStream.flush();
      close();
      return true;
    } catch ( ReportProcessingException e ) {
      return false;
    } catch ( IOException e ) {
      return false;
    }
  }
}
