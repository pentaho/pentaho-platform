/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action.jfreereport.outputs;

import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.table.base.FlowReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FileSystemURLRewriter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.FlowHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.file.FileRepository;

import java.io.File;

/**
 * Creation-Date: 07.07.2006, 20:42:17
 * 
 * @author Thomas Morgner
 */
public class JFreeReportDirectoryHtmlComponent extends AbstractGenerateContentComponent {
  private static final long serialVersionUID = -7511578647689368225L;

  public JFreeReportDirectoryHtmlComponent() {
  }

  private File getInputFileValue( final String inputName ) {
    final Object input = getInputValue( inputName );
    if ( input == null ) {
      return null;
    }
    if ( input instanceof File ) {
      return (File) input;
    }
    if ( input instanceof String ) {
      return new File( (String) input );
    }
    return null;
  }

  @Override
  protected boolean performExport( final MasterReport report ) {
    try {
      final File targetFile = getInputFileValue( AbstractJFreeReportComponent.REPORTDIRECTORYHTML_TARGETFILE );
      if ( targetFile == null ) {
        return false;
      }

      File dataDirectory = getInputFileValue( AbstractJFreeReportComponent.REPORTDIRECTORYHTML_DATADIR );
      if ( dataDirectory == null ) {
        dataDirectory = new File( targetFile, "data/" ); //$NON-NLS-1$
      }

      final File targetDirectory = targetFile.getParentFile();
      if ( dataDirectory.exists() && ( dataDirectory.isDirectory() == false ) ) {
        dataDirectory = dataDirectory.getParentFile();
        if ( dataDirectory.isDirectory() == false ) {
          String msg = Messages.getInstance().getErrorString( "JFreeReportDirectoryComponent.ERROR_0001_INVALID_DIR", //$NON-NLS-1$
              dataDirectory.getPath() );
          throw new ReportProcessingException( msg );
        }
      } else if ( dataDirectory.exists() == false ) {
        dataDirectory.mkdirs();
      }

      final FileRepository targetRepository = new FileRepository( targetDirectory );
      final ContentLocation targetRoot = targetRepository.getRoot();

      final FileRepository dataRepository = new FileRepository( dataDirectory );
      final ContentLocation dataRoot = dataRepository.getRoot();

      final FlowHtmlOutputProcessor outputProcessor = new FlowHtmlOutputProcessor();

      final HtmlPrinter printer = new AllItemsHtmlPrinter( report.getResourceManager() );
      printer.setContentWriter( targetRoot, new DefaultNameGenerator( targetRoot, targetFile.getName() ) );
      printer.setDataWriter( dataRoot, new DefaultNameGenerator( targetRoot, "content" ) ); //$NON-NLS-1$
      printer.setUrlRewriter( new FileSystemURLRewriter() );
      outputProcessor.setPrinter( printer );

      final FlowReportProcessor sp = new FlowReportProcessor( report, outputProcessor );
      final int yieldRate = getYieldRate();
      if ( yieldRate > 0 ) {
        sp.addReportProgressListener( new YieldReportListener( yieldRate ) );
      }
      sp.processReport();
      sp.close();
      return true;
    } catch ( ReportProcessingException e ) {
      return false;
    } catch ( ContentIOException e ) {
      return false;
    }
  }
}
