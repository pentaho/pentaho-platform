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
