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

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.StreamCSVOutputProcessor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportCSVComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -6277594193744555596L;

  public JFreeReportCSVComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/csv"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".csv"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {
      final StreamCSVOutputProcessor target = new StreamCSVOutputProcessor( outputStream );
      final StreamReportProcessor reportProcessor = new StreamReportProcessor( report, target );
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
