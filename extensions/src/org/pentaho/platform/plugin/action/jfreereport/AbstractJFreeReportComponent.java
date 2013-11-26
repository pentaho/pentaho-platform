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

package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.platform.engine.services.solution.ComponentBase;

public abstract class AbstractJFreeReportComponent extends ComponentBase {

  private static final long serialVersionUID = -8064236773763629440L;

  protected AbstractJFreeReportComponent() {
  }

  /*
   * These are the inputs and component settings that are known by the various JFreeReport components.
   */
  // Data Component
  public static final String DATACOMPONENT_SOURCE = "source"; //$NON-NLS-1$

  public static final String DATACOMPONENT_SQLSOURCE = "sql"; //$NON-NLS-1$

  public static final String DATACOMPONENT_SQLCLASS = "org.pentaho.platform.plugin.action.sql.SQLDataComponent"; //$NON-NLS-1$

  public static final String DATACOMPONENT_MDXSOURCE = "mdx"; //$NON-NLS-1$

  public static final String DATACOMPONENT_MDXCLASS = "org.pentaho.platform.plugin.action.mdx.MDXDataComponent"; //$NON-NLS-1$

  public static final String DATACOMPONENT_DATAINPUT = "data"; //$NON-NLS-1$

  /** the default datasource name used in jfreereport, now supported by the platform in addition to data above */
  public static final String DATACOMPONENT_DEFAULTINPUT = "default"; //$NON-NLS-1$

  public static final String DATACOMPONENT_JARINPUT = "report-jar"; //$NON-NLS-1$

  public static final String DATACOMPONENT_CLASSLOCINPUT = "class-location"; //$NON-NLS-1$

  // Report Generate Definition Component
  public static final String REPORTGENERATEDEFN_REPORTSPECINPUT = "report-spec"; //$NON-NLS-1$

  public static final String REPORTGENERATEDEFN_REPORTDEFN = "report-definition"; //$NON-NLS-1$

  // Load Component
  public static final String REPORTLOAD_RESOURCENAME = "resource-name"; //$NON-NLS-1$

  public static final String REPORTLOAD_REPORTLOC = "report-location"; //$NON-NLS-1$

  public static final String REPORTLOAD_RESURL = "res-url"; //$NON-NLS-1$

  public static final String REPORTGENERATE_YIELDRATE = "yield-rate"; //$NON-NLS-1$

  public static final String REPORTGENERATE_PRIORITYINPUT = "report-priority"; //$NON-NLS-1$

  public static final String REPORTGENERATE_PRIORITYNORMAL = "normal"; //$NON-NLS-1$

  public static final String REPORTGENERATE_PRIORITYLOWER = "lower"; //$NON-NLS-1$

  public static final String REPORTGENERATE_PRIORITYLOWEST = "lowest"; //$NON-NLS-1$

  // All Content Component
  public static final String REPORTALLCONTENT_OUTPUTTYPE = "output-type"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_HTML = "html"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_PDF = "pdf"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_CSV = "csv"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_XML = "xml"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_RTF = "rtf"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_XLS = "xls"; //$NON-NLS-1$

  public static final String REPORTALLCONTENT_OUTPUTTYPE_SWING = "swing-preview"; //$NON-NLS-1$

  // Directory Html Component
  public static final String REPORTDIRECTORYHTML_TARGETFILE = "target-file"; //$NON-NLS-1$

  public static final String REPORTDIRECTORYHTML_DATADIR = "data-directory"; //$NON-NLS-1$

  // Report Excel Component
  public static final String WORKBOOK_PARAM = "workbook"; //$NON-NLS-1$

  // Report HTML Component
  public static final String REPORTHTML_CONTENTHANDLER = "content-handler"; //$NON-NLS-1$

  // Report Preview Swing Component
  public static final String REPORTSWING_PROGRESSBAR = "progress-bar"; //$NON-NLS-1$

  public static final String REPORTSWING_PROGRESSDIALOG = "progress-dialog"; //$NON-NLS-1$

  public static final String REPORTSWING_REPORTCONTROLLER = "report-controler"; //$NON-NLS-1$

  public static final String REPORTSWING_MODAL = "modal"; //$NON-NLS-1$

  public static final String REPORTSWING_PARENTDIALOG = "parent-dialog"; //$NON-NLS-1$

  // Report Parameter Component
  public static final String REPORTPARAMCOMPONENT_PRIVATEREPORT_OUTPUT = "create_private_report"; //$NON-NLS-1$

  // Generate Stream Component
  public static final String REPORTGENERATESTREAM_REPORT_OUTPUT = "report-output"; //$NON-NLS-1$

  /*
   * These are created by the component for communication to the other JFreeReport components. These are named such that
   * they can't occur in the action sequence. (sbarkdull: these must be valid XML element names)
   */
  public static final String DATACOMPONENT_REPORTTEMP_OBJINPUT = "_REPORT.OBJECT"; //$NON-NLS-1$

  public static final String DATACOMPONENT_REPORTTEMP_DATAINPUT = "_REPORT.DATA"; //$NON-NLS-1$

  public static final String REPORTGENERATEDEFN_REPORTTEMP_PERFQRY = "_PERFORM.QUERY"; //$NON-NLS-1$

}
