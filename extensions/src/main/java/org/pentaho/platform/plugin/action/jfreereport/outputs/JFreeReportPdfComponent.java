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

import org.jfree.util.Log;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfOutputProcessor;

import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportPdfComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 3209507821690330555L;

  public JFreeReportPdfComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/pdf"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".pdf"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    PageableReportProcessor proc = null;
    try {

      final PdfOutputProcessor outputProcessor = new PdfOutputProcessor( report.getConfiguration(), outputStream );
      proc = new PageableReportProcessor( report, outputProcessor );
      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        proc.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      proc.processReport();
      proc.close();
      proc = null;
      close();
      return true;
    } catch ( Exception e ) {
      Log.error( Messages.getInstance().getErrorString( "JFreeReportPdfComponent.ERROR_0001_WRITING_PDF_FAILED", //$NON-NLS-1$
          e.getLocalizedMessage() ), e );
      return false;
    } finally {
      if ( proc != null ) {
        proc.close();
      }
    }
  }
}
