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
