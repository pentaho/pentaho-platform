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
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FileSystemURLRewriter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.StreamHtmlOutputProcessor;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportStreamHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = 7103996413736560262L;

  public JFreeReportStreamHtmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "text/html"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".html"; //$NON-NLS-1$
  }

  @SuppressWarnings( "deprecation" )
  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {

      final StreamRepository targetRepository = new StreamRepository( null, outputStream );
      final ContentLocation targetRoot = targetRepository.getRoot();

      final HtmlOutputProcessor outputProcessor = new StreamHtmlOutputProcessor( report.getConfiguration() );
      final HtmlPrinter printer = new AllItemsHtmlPrinter( report.getResourceManager() );
      printer.setContentWriter( targetRoot, new DefaultNameGenerator( targetRoot, "index", "html" ) ); //$NON-NLS-1$//$NON-NLS-2$
      printer.setDataWriter( null, null );
      printer.setUrlRewriter( new FileSystemURLRewriter() );
      outputProcessor.setPrinter( printer );

      final StreamReportProcessor sp = new StreamReportProcessor( report, outputProcessor );
      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        sp.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      sp.processReport();
      sp.close();

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
