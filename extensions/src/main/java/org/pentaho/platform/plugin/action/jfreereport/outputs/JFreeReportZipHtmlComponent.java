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
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FlowHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.SingleRepositoryURLRewriter;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.RepositoryUtilities;
import org.pentaho.reporting.libraries.repository.zip.ZipRepository;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportZipHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -3904516365257691828L;

  public JFreeReportZipHtmlComponent() {
  }

  @Override
  protected String getMimeType() {
    return "application/zip"; //$NON-NLS-1$
  }

  @Override
  protected String getExtension() {
    return ".zip"; //$NON-NLS-1$
  }

  @Override
  protected boolean performExport( final MasterReport report, final OutputStream outputStream ) {
    try {
      String dataDirectory = getInputStringValue( AbstractJFreeReportComponent.REPORTDIRECTORYHTML_DATADIR );
      if ( dataDirectory == null ) {
        dataDirectory = "data"; //$NON-NLS-1$
      }

      final ZipRepository zipRepository = new ZipRepository();
      final ContentLocation root = zipRepository.getRoot();
      final ContentLocation data =
          RepositoryUtilities.createLocation( zipRepository, RepositoryUtilities.split( dataDirectory, "/" ) ); //$NON-NLS-1$

      final FlowHtmlOutputProcessor outputProcessor = new FlowHtmlOutputProcessor();

      final HtmlPrinter printer = new AllItemsHtmlPrinter( report.getResourceManager() );
      printer.setContentWriter( root, new DefaultNameGenerator( root, "report.html" ) ); //$NON-NLS-1$
      printer.setDataWriter( data, new DefaultNameGenerator( data, "content" ) ); //$NON-NLS-1$
      printer.setUrlRewriter( new SingleRepositoryURLRewriter() );
      outputProcessor.setPrinter( printer );

      final FlowReportProcessor sp = new FlowReportProcessor( report, outputProcessor );
      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        sp.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      sp.processReport();
      zipRepository.write( outputStream );
      close();
      return true;
    } catch ( ReportProcessingException e ) {
      error( Messages.getInstance().getString( "JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( IOException e ) {
      error( Messages.getInstance().getString( "JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( ContentIOException e ) {
      error( Messages.getInstance().getString( "JFreeReportZipHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    }
  }
}
