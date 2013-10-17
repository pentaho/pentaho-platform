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
