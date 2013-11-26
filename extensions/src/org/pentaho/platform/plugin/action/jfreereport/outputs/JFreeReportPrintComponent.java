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

import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.gui.print.PrintUtil;
import org.pentaho.reporting.engine.classic.extensions.modules.java14print.Java14PrintUtil;

import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

/**
 * Creation-Date: 07.07.2006, 20:06:56
 * 
 * @author Thomas Morgner
 */
public class JFreeReportPrintComponent extends AbstractGenerateContentComponent {
  private static final long serialVersionUID = 3365941892457480119L;

  public JFreeReportPrintComponent() {
  }

  private PrintService findPrintService( final String name ) {
    final PrintService[] services = PrintServiceLookup.lookupPrintServices(
      DocFlavor.SERVICE_FORMATTED.PAGEABLE, null );
    for ( final PrintService service : services ) {
      if ( service.getName().equals( name ) ) {
        return service;
      }
    }

    if ( services.length == 0 ) {
      return null;
    }
    return services[0];
  }

  @Override
  protected boolean performExport( final MasterReport report ) {
    final String printerName = getInputStringValue( StandardSettings.PRINTER_NAME );
    final Object jobName = getActionTitle();

    if ( jobName instanceof String ) {
      report.getReportConfiguration().setConfigProperty( PrintUtil.PRINTER_JOB_NAME_KEY, String.valueOf( jobName ) );
    }

    final PrintService printer = findPrintService( printerName );
    try {
      Java14PrintUtil.printDirectly( report, printer );
    } catch ( PrintException e ) {
      return false;
    } catch ( ReportProcessingException e ) {
      return false;
    }
    return true;
  }
}
