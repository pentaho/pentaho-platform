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
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.StreamRTFOutputProcessor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportRTFComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -4095237855917616138L;

  public JFreeReportRTFComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/rtf"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".rtf"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {
      final StreamRTFOutputProcessor target =
          new StreamRTFOutputProcessor( report.getConfiguration(), outputStream, report.getResourceManager() );
      final StreamReportProcessor proc = new StreamReportProcessor( report, target );
      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        proc.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      proc.processReport();
      proc.close();
      outputStream.close();
      close();
      return true;
    } catch ( ReportProcessingException e ) {
      return false;
    } catch ( IOException e ) {
      return false;
    }
  }
}
