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

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.jfreereport.helper.PentahoURLRewriter;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.StreamReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.StreamHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriter;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.NameGenerator;
import org.pentaho.reporting.libraries.repository.file.FileRepository;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportHtmlComponent extends AbstractGenerateStreamContentComponent {
  private static final long serialVersionUID = -4296469329232291213L;

  private static final boolean DO_NOT_USE_THE_CONTENT_REPOSITORY = true;

  public JFreeReportHtmlComponent() {
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

      String contentHandlerPattern = getInputStringValue( AbstractJFreeReportComponent.REPORTHTML_CONTENTHANDLER );
      if ( contentHandlerPattern == null ) {
        final Configuration globalConfig = ClassicEngineBoot.getInstance().getGlobalConfig();
        contentHandlerPattern = globalConfig.getConfigProperty( "org.pentaho.web.ContentHandler" ); //$NON-NLS-1$
      }

      final IApplicationContext ctx = PentahoSystem.getApplicationContext();

      final URLRewriter rewriter;
      final ContentLocation dataLocation;
      final NameGenerator dataNameGenerator;
      if ( ctx != null ) {
        File dataDirectory = new File( ctx.getFileOutputPath( "system/tmp/" ) ); //$NON-NLS-1$
        if ( dataDirectory.exists() && ( dataDirectory.isDirectory() == false ) ) {
          dataDirectory = dataDirectory.getParentFile();
          if ( dataDirectory.isDirectory() == false ) {
            throw new ReportProcessingException( Messages.getInstance().getErrorString(
                "JFreeReportDirectoryComponent.ERROR_0001_INVALID_DIR", dataDirectory.getPath() ) ); //$NON-NLS-1$
          }
        } else if ( dataDirectory.exists() == false ) {
          dataDirectory.mkdirs();
        }

        final FileRepository dataRepository = new FileRepository( dataDirectory );
        dataLocation = dataRepository.getRoot();
        dataNameGenerator = new DefaultNameGenerator( dataLocation );
        rewriter = new PentahoURLRewriter( contentHandlerPattern );
      } else {
        dataLocation = null;
        dataNameGenerator = null;
        rewriter = new PentahoURLRewriter( contentHandlerPattern );
      }

      final StreamRepository targetRepository = new StreamRepository( null, outputStream );
      final ContentLocation targetRoot = targetRepository.getRoot();

      final HtmlOutputProcessor outputProcessor = new StreamHtmlOutputProcessor( report.getConfiguration() );
      final HtmlPrinter printer = new AllItemsHtmlPrinter( report.getResourceManager() );
      printer.setContentWriter( targetRoot, new DefaultNameGenerator( targetRoot, "index", "html" ) ); //$NON-NLS-1$//$NON-NLS-2$
      printer.setDataWriter( dataLocation, dataNameGenerator );
      printer.setUrlRewriter( rewriter );
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
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( IOException e ) {
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    } catch ( ContentIOException e ) {
      error( Messages.getInstance().getString( "JFreeReportHtmlComponent.ERROR_0046_FAILED_TO_PROCESS_REPORT" ), e ); //$NON-NLS-1$
      return false;
    }
  }
}
