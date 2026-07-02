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

import org.jfree.ui.RefineryUtilities;
import org.pentaho.platform.plugin.action.jfreereport.AbstractJFreeReportComponent;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.ReportController;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;

import java.awt.Dialog;
import java.awt.Frame;

/**
 * Creation-Date: 07.07.2006, 14:06:43
 * 
 * @author Thomas Morgner
 */
public class JFreeReportPreviewSwingComponent extends AbstractGenerateContentComponent {
  private static final long serialVersionUID = -8158403113060631980L;

  private static final String PROGRESS_DIALOG_ENABLED_KEY = "org.jfree.report.modules.gui.base.ProgressDialogEnabled"; //$NON-NLS-1$

  private static final String PROGRESS_BAR_ENABLED_KEY = "org.jfree.report.modules.gui.base.ProgressBarEnabled"; //$NON-NLS-1$

  public JFreeReportPreviewSwingComponent() {
  }

  @Override
  protected boolean performExport( final MasterReport report ) {
    final ModifiableConfiguration reportConfiguration = report.getReportConfiguration();

    final boolean progressBar =
        getInputBooleanValue(
            AbstractJFreeReportComponent.REPORTSWING_PROGRESSBAR,
            "true".equals( reportConfiguration.getConfigProperty(
              JFreeReportPreviewSwingComponent.PROGRESS_BAR_ENABLED_KEY ) ) ); //$NON-NLS-1$
    final boolean progressDialog =
        getInputBooleanValue(
            AbstractJFreeReportComponent.REPORTSWING_PROGRESSDIALOG,
            "true".equals( reportConfiguration.getConfigProperty(
              JFreeReportPreviewSwingComponent.PROGRESS_DIALOG_ENABLED_KEY ) ) ); //$NON-NLS-1$
    reportConfiguration.setConfigProperty( JFreeReportPreviewSwingComponent.PROGRESS_DIALOG_ENABLED_KEY, String
        .valueOf( progressDialog ) );
    reportConfiguration.setConfigProperty( JFreeReportPreviewSwingComponent.PROGRESS_BAR_ENABLED_KEY, String
        .valueOf( progressBar ) );

    final PreviewDialog dialog = createDialog( report );
    final ReportController reportController = getReportController();
    if ( reportController != null ) {
      dialog.setReportController( reportController );
    }
    dialog.pack();
    if ( dialog.getParent() != null ) {
      RefineryUtilities.centerDialogInParent( dialog );
    } else {
      RefineryUtilities.centerFrameOnScreen( dialog );
    }

    dialog.setVisible( true );
    return true;
  }

  private ReportController getReportController() {
    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTSWING_REPORTCONTROLLER ) ) {
      final Object controller = getInputValue( AbstractJFreeReportComponent.REPORTSWING_REPORTCONTROLLER );
      if ( controller instanceof ReportController ) {
        return (ReportController) controller;
      }
    }
    return null;
  }

  private PreviewDialog createDialog( final MasterReport report ) {
    final boolean modal = getInputBooleanValue( AbstractJFreeReportComponent.REPORTSWING_MODAL, true );

    if ( isDefinedInput( AbstractJFreeReportComponent.REPORTSWING_PARENTDIALOG ) ) {
      final Object parent = getInputValue( AbstractJFreeReportComponent.REPORTSWING_PARENTDIALOG );
      if ( parent instanceof Dialog ) {
        return new PreviewDialog( report, (Dialog) parent, modal );
      } else if ( parent instanceof Frame ) {
        return new PreviewDialog( report, (Frame) parent, modal );
      }
    }

    final PreviewDialog previewDialog = new PreviewDialog( report );
    previewDialog.setModal( modal );
    return previewDialog;
  }
}
