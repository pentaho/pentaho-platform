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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.actionsequence.dom.actions.JFreeReportGenAction;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.jfreereport.castormodel.reportspec.Field;
import org.pentaho.jfreereport.castormodel.reportspec.ReportSpec;
import org.pentaho.jfreereport.castormodel.reportspec.ReportSpecChoice;
import org.pentaho.jfreereport.wizard.utility.report.ReportGenerationUtility;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Bill Seyler, Michael D'Amour
 * 
 * 
 */
public class JFreeReportGeneratorComponent extends ComponentBase {
  /**
   * 
   */
  private static final long serialVersionUID = 9050456842938084174L;

  private static final String GROUP_LABELS_PROP = "group-labels"; //$NON-NLS-1$

  private static final String GROUP_LABEL_PROP = "group-label"; //$NON-NLS-1$

  private static final String GROUPED_COLUMNS_PROP = "grouped-columns"; //$NON-NLS-1$

  private static final String GROUPED_COLUMN_INDICES_PROP = "group-index"; //$NON-NLS-1$

  private static final String COLUMN_NAMES_PROP = "column-names"; //$NON-NLS-1$

  private static final String COLUMN_NAME_PROP = "column-name"; //$NON-NLS-1$

  private static final String COLUMN_WIDTHS_PROP = "column-widths"; //$NON-NLS-1$

  private static final String WIDTH_PROP = "width"; //$NON-NLS-1$

  private static final String COLUMN_ALIGNMENTS_PROP = "column-alignments"; //$NON-NLS-1$

  private static final String COLUMN_ALIGNMENT_PROP = "column-alignment"; //$NON-NLS-1$

  private static final String ITEM_HIDES_PROP = "item-hide-columns"; //$NON-NLS-1$

  private static final String ITEM_HIDE_PROP = "use-item-hide"; //$NON-NLS-1$

  private static final String COLUMN_FORMATS_PROP = "column-formats"; //$NON-NLS-1$

  private static final String FORMAT_PROP = "column-format"; //$NON-NLS-1$

  IPentahoResultSet resultSet = null;

  String[] displayNames = null;

  String compPath = null;

  String path = null;

  String orientation = "landscape"; //$NON-NLS-1$

  String[] groupLabels = null;

  int[] groupIndices = null;

  int[] widths = null;

  boolean[] itemHides = null;

  String[] formats = null;

  String[] columnAlignments;

  String reportName = ""; //$NON-NLS-1$

  boolean createSubTotals = false;

  boolean createGrandTotals = false;

  boolean createRowBanding = false;

  boolean createTotalColumn = false;

  String totalColumnName = "All"; //$NON-NLS-1$

  int totalColumnWidth = 120;

  String totalColumnFormat = "#,##0"; //$NON-NLS-1$

  String rowBandingColor = "#A0A0A0"; //$NON-NLS-1$

  int spacerWidth = 10;

  String columnHeaderBackgroundColor = "#C0C0C0"; //$NON-NLS-1$

  String columnHeaderForegroundColor = "#202020"; //$NON-NLS-1$

  String columnHeaderFontFace = "SansSerif"; //$NON-NLS-1$

  String columnHeaderFontSize = "12"; //$NON-NLS-1$

  int columnHeaderGap = 2;

  String nullString = ""; //$NON-NLS-1$

  int horizontalOffset = 0;

  @Override
  public Log getLogger() {
    return LogFactory.getLog( JFreeReportGeneratorComponent.class );
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    /*
     * String templatePath = getComponentSetting(TEMPLATE_PATH_PROP); if (templatePath == null) {
     * error("Undefined Template Path"); return false; }
     */
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() {
    /*
     * The on-the-fly JFreeReport generator will expect the following: =============================================
     * 
     * INPUT: Path (String) to Existing JFreeReport to use as "template" OutputStream to write generated report
     * IPentahoResultSet List of Columns to "Group" List of column widths
     * 
     * OUTPUT: JFreeReport XML to provided Path or OutputStream
     * 
     * ASSUMPTIONS: List of column widths - last provided item is to be repeated for all remaining columns Perform
     * ItemSumFunction on all numeric columns per group and grand total Perform ItemSumFunction on calculated column per
     * group and grand total Groups and Items will be removed from template (we will retain font/color data)
     * =============================================
     * 
     * public OnTheFlyJFreeReportGenerator(String path, IPentahoResultSet set, List groupLabels, List widths,
     * OutputStream stream) ------ public void process()
     */

    JFreeReportGenAction genAction = null;

    if ( !( getActionDefinition() instanceof JFreeReportGenAction ) ) {
      error( Messages.getInstance().getErrorString(
          "JFreeReportGeneratorComponent.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML() ) ); //$NON-NLS-1$
      return false;
    } else {
      genAction = (JFreeReportGenAction) getActionDefinition();

      resultSet = (IPentahoResultSet) genAction.getResultSet().getValue();
      Node componentNode = null;
      String settingsFromActionSequence = null;
      try {
        settingsFromActionSequence = genAction.getComponentSettings().getStringValue();
      } catch ( Exception ex ) {
        //ignore
      }
      if ( settingsFromActionSequence != null ) {
        try {
          Document settingsDoc =
              XmlDom4JHelper.getDocFromString( settingsFromActionSequence, new PentahoEntityResolver() );
          componentNode = settingsDoc.getRootElement();
        } catch ( Exception e ) {
          error( "Could not get settings from action sequence document", e ); //$NON-NLS-1$
          return false;
        }
      } else {
        componentNode = getComponentDefinition();
      }
      try {
        compPath = genAction.getTemplatePath().getStringValue();
        path = PentahoSystem.getApplicationContext().getSolutionPath( compPath );
        orientation = genAction.getOrientation().getStringValue();
        nullString = genAction.getNullString().getStringValue();
        horizontalOffset = genAction.getHorizontalOffset().getIntValue( horizontalOffset );
        reportName = genAction.getReportName().getStringValue();
        createSubTotals = genAction.getCreateSubTotals().getBooleanValue( false );
        createGrandTotals = genAction.getCreateGrandTotals().getBooleanValue( false );
        createRowBanding = genAction.getCreateRowBanding().getBooleanValue( false );
        createTotalColumn = genAction.getCreateTotalColumn().getBooleanValue( false );
        totalColumnName = genAction.getTotalColumnName().getStringValue();
        totalColumnWidth = genAction.getTotalColumnWidth().getIntValue( totalColumnWidth );
        totalColumnFormat = genAction.getTotalColumnFormat().getStringValue();
        rowBandingColor = genAction.getRowBandingColor().getStringValue();
        spacerWidth = genAction.getSpacerWidth().getIntValue( spacerWidth );
        columnHeaderBackgroundColor = genAction.getColumnHeaderBackgroundColor().getStringValue();
        columnHeaderForegroundColor = genAction.getColumnHeaderForegroundColor().getStringValue();
        columnHeaderFontFace = genAction.getColumnHeaderFontFace().getStringValue();
        columnHeaderFontSize = genAction.getColumnHeaderFontSize().getStringValue();
        columnHeaderGap = genAction.getColumnHeaderGap().getIntValue( columnHeaderGap );
      } catch ( Exception e ) {
        e.printStackTrace();
      }

      // Get the group display labels
      List groupLabelNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.GROUP_LABELS_PROP
              + "/" + JFreeReportGeneratorComponent.GROUP_LABEL_PROP ); //$NON-NLS-1$
      if ( groupLabelNodes != null ) {
        groupLabels = new String[groupLabelNodes.size()];
        for ( int i = 0; i < groupLabels.length; i++ ) {
          groupLabels[i] = ( (Node) groupLabelNodes.get( i ) ).getText();
        }
      }
      // Get the grouped columns indices
      List groupedColumnsIndicesNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.GROUPED_COLUMNS_PROP
              + "/" + JFreeReportGeneratorComponent.GROUPED_COLUMN_INDICES_PROP ); //$NON-NLS-1$
      if ( groupedColumnsIndicesNodes != null ) {
        groupIndices = new int[groupedColumnsIndicesNodes.size()];
        for ( int i = 0; i < groupIndices.length; i++ ) {
          groupIndices[i] = Integer.parseInt( ( (Node) groupedColumnsIndicesNodes.get( i ) ).getText() ) - 1;
          // I am zero based, this is not
        }
      }
      // get display names
      List displayNameNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.COLUMN_NAMES_PROP
              + "/" + JFreeReportGeneratorComponent.COLUMN_NAME_PROP ); //$NON-NLS-1$
      if ( displayNameNodes != null ) {
        displayNames = new String[displayNameNodes.size()];
        for ( int i = 0; i < displayNames.length; i++ ) {
          displayNames[i] = ( (Node) displayNameNodes.get( i ) ).getText();
        }
      }
      // get column alignments
      List columnAlignmentNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.COLUMN_ALIGNMENTS_PROP
              + "/" + JFreeReportGeneratorComponent.COLUMN_ALIGNMENT_PROP ); //$NON-NLS-1$
      if ( columnAlignmentNodes != null ) {
        columnAlignments = new String[columnAlignmentNodes.size()];
        for ( int i = 0; i < columnAlignments.length; i++ ) {
          columnAlignments[i] = ( (Node) columnAlignmentNodes.get( i ) ).getText();
        }
      }
      // Get the column widths
      List widthNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.COLUMN_WIDTHS_PROP
              + "/" + JFreeReportGeneratorComponent.WIDTH_PROP ); //$NON-NLS-1$
      if ( widthNodes != null ) {
        widths = new int[widthNodes.size()];
        for ( int i = 0; i < widths.length; i++ ) {
          widths[i] = Integer.valueOf( ( (Node) widthNodes.get( i ) ).getText() ).intValue();
        }
      }
      // Get the column item hides
      List itemHideNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.ITEM_HIDES_PROP
              + "/" + JFreeReportGeneratorComponent.ITEM_HIDE_PROP ); //$NON-NLS-1$
      if ( itemHideNodes != null ) {
        itemHides = new boolean[itemHideNodes.size()];
        for ( int i = 0; i < itemHides.length; i++ ) {
          itemHides[i] = Boolean.valueOf( ( (Node) itemHideNodes.get( i ) ).getText() ).booleanValue();
        }
      }
      // Get the column formats
      List formatNodes =
          componentNode.selectNodes( JFreeReportGeneratorComponent.COLUMN_FORMATS_PROP
              + "/" + JFreeReportGeneratorComponent.FORMAT_PROP ); //$NON-NLS-1$
      if ( formatNodes != null ) {
        formats = new String[formatNodes.size()];
        for ( int i = 0; i < formats.length; i++ ) {
          formats[i] = ( (Node) formatNodes.get( i ) ).getText();
        }
      }
    }
    String reportDefinition = process();
    if ( reportDefinition != null ) {
      if ( genAction.getOutputReportDefinition() != null ) {
        genAction.getOutputReportDefinition().setValue( reportDefinition );
      } else {
        // This is to support the old way where
        // we did not check if report-definition existed in the output section
        setOutputValue( JFreeReportGenAction.REPORT_DEFINITION, reportDefinition );
      }
    }

    return true;
  }

  @SuppressWarnings( "deprecation" )
  public String process() {
    // CREATE report-spec.xml
    // USE passed in jfreeReportTemplate as "include" -- stuff in
    // report-spec
    // GENERATE JFreeReport from report-spec using code already written in
    // DesignerUtility
    //
    ByteArrayOutputStream outputStream = null;
    try {
      outputStream = new ByteArrayOutputStream();
    } catch ( Exception e ) {
      getLogger().error( e );
    }
    ReportSpec reportSpec = new ReportSpec();
    reportSpec.setReportName( reportName );
    reportSpec.setHorizontalOffset( horizontalOffset );
    reportSpec.setIncludeSrc( getPath() );
    reportSpec.setQuery( "no query" ); //$NON-NLS-1$
    reportSpec.setReportSpecChoice( new ReportSpecChoice() );
    reportSpec.getReportSpecChoice().setJndiSource( "SampleData" ); //$NON-NLS-1$
    reportSpec.setCalculateGrandTotals( createGrandTotals );
    reportSpec.setTopMargin( 10 );
    reportSpec.setBottomMargin( 10 );
    reportSpec.setLeftMargin( 10 );
    reportSpec.setRightMargin( 10 );
    reportSpec.setUseRowBanding( createRowBanding );
    reportSpec.setColumnHeaderGap( columnHeaderGap );
    if ( rowBandingColor != null ) {
      reportSpec.setRowBandingColor( rowBandingColor );
    }
    if ( columnHeaderBackgroundColor != null ) {
      reportSpec.setColumnHeaderBackgroundColor( columnHeaderBackgroundColor );
    }
    if ( columnHeaderForegroundColor != null ) {
      reportSpec.setColumnHeaderFontColor( columnHeaderForegroundColor );
    }
    if ( columnHeaderFontFace != null ) {
      reportSpec.setColumnHeaderFontName( columnHeaderFontFace );
    }
    if ( columnHeaderFontSize != null ) {
      reportSpec.setColumnHeaderFontSize( Integer.parseInt( columnHeaderFontSize ) );
    }
    reportSpec.setOrientation( orientation );
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    int totalWidth = reportSpec.getLeftMargin() + reportSpec.getRightMargin();
    List groupsList = new LinkedList();
    List details = new LinkedList();
    // leading spacer
    if ( spacerWidth > 0 ) {
      Field spacer = new Field();
      spacer.setName( "" ); //$NON-NLS-1$
      spacer.setDisplayName( "" ); //$NON-NLS-1$
      spacer.setType( Types.VARCHAR );
      spacer.setFormat( "" ); //$NON-NLS-1$
      spacer.setHorizontalAlignment( "right" ); //$NON-NLS-1$
      spacer.setVerticalAlignment( "middle" ); //$NON-NLS-1$
      spacer.setWidth( new BigDecimal( spacerWidth ) );
      spacer.setWidthLocked( true );
      totalWidth += spacerWidth;
      spacer.setExpression( "none" ); //$NON-NLS-1$
      spacer.setIsWidthPercent( false );
      spacer.setIsDetail( true );
      reportSpec.addField( spacer );
    }
    for ( int i = 0; i < colHeaders.length; i++ ) {
      // System.out.println("header [" + i + "] = " + colHeaders[i]);
      Class typeClass = null;
      for ( int j = 0; j < resultSet.getRowCount(); j++ ) {
        Object value = resultSet.getValueAt( j, i );
        if ( ( value != null ) && !value.toString().equals( "" ) ) { //$NON-NLS-1$
          typeClass = value.getClass();
        }
      }
      String columnName = colHeaders[i].toString();
      Field f = new Field();
      f.setName( columnName );
      f.setNullString( getNullString() );
      if ( isGroup( columnName ) ) {
        f.setDisplayName( getGroupLabel( columnName, i ) );
      } else if ( i < displayNames.length ) {
        f.setDisplayName( displayNames[i] );
      } else {
        f.setDisplayName( columnName );
      }
      f.setIsWidthPercent( false );
      f.setWidth( new BigDecimal( getWidth( columnName ) ) );
      f.setWidthLocked( true );
      f.setIsDetail( !isGroup( columnName ) );
      if ( f.getIsDetail() ) {
        details.add( f );
      } else {
        groupsList.add( f );
      }
      f.setBackgroundColor( "#FFFFFF" ); //$NON-NLS-1$
      f.setType( getType( typeClass ) );
      if ( ( itemHides == null ) || ( itemHides.length == 0 ) ) {
        f.setUseItemHide( getType( typeClass ) == Types.NUMERIC ? false : true );
      } else {
        f.setUseItemHide( useItemHide( columnName ) );
      }
      f.setVerticalAlignment( "middle" ); //$NON-NLS-1$
      f.setFormat( getColumnFormat( columnName ) );
      String alignment = getColumnAlignment( columnName );
      if ( alignment != null ) {
        f.setHorizontalAlignment( alignment );
      } else {
        if ( f.getIsDetail() && ( f.getType() == Types.NUMERIC ) ) {
          f.setHorizontalAlignment( "right" ); //$NON-NLS-1$
        }
      }
      if ( f.getIsDetail() && ( f.getType() == Types.NUMERIC ) ) {
        f.setExpression( "sum" ); //$NON-NLS-1$
      } else {
        f.setExpression( "none" ); //$NON-NLS-1$
      }
      f.setCalculateGroupTotals( createSubTotals );
      reportSpec.addField( f );
      if ( ( spacerWidth > 0 ) && f.getIsDetail() ) {
        // spacer
        Field spacer = new Field();
        spacer.setName( "" ); //$NON-NLS-1$
        spacer.setDisplayName( "" ); //$NON-NLS-1$
        spacer.setType( Types.VARCHAR );
        spacer.setFormat( "" ); //$NON-NLS-1$
        spacer.setHorizontalAlignment( "right" ); //$NON-NLS-1$
        spacer.setVerticalAlignment( "middle" ); //$NON-NLS-1$
        spacer.setWidth( new BigDecimal( spacerWidth ) );
        spacer.setWidthLocked( true );
        totalWidth += spacerWidth;
        spacer.setExpression( "none" ); //$NON-NLS-1$
        spacer.setIsWidthPercent( false );
        spacer.setIsDetail( true );
        reportSpec.addField( spacer );
      }
    }
    for ( int i = 0; i < details.size(); i++ ) {
      Field f = (Field) details.get( i );
      totalWidth += f.getWidth().intValue();
    }
    if ( createTotalColumn ) {
      Field f = new Field();
      f.setName( "TOTAL_COLUMN" ); //$NON-NLS-1$
      f.setDisplayName( totalColumnName );
      f.setType( Types.NUMERIC );
      f.setFormat( totalColumnFormat );
      f.setHorizontalAlignment( "right" ); //$NON-NLS-1$
      f.setVerticalAlignment( "middle" ); //$NON-NLS-1$
      f.setWidth( new BigDecimal( totalColumnWidth ) );
      f.setWidthLocked( true );
      f.setExpression( "sum" ); //$NON-NLS-1$
      f.setIsWidthPercent( false );
      f.setIsDetail( true );
      reportSpec.addField( f );
      totalWidth += totalColumnWidth;
      if ( spacerWidth > 0 ) {
        // spacer
        Field spacer = new Field();
        spacer.setName( "" ); //$NON-NLS-1$
        spacer.setDisplayName( "" ); //$NON-NLS-1$
        spacer.setType( Types.VARCHAR );
        spacer.setFormat( "" ); //$NON-NLS-1$
        spacer.setHorizontalAlignment( "right" ); //$NON-NLS-1$
        spacer.setVerticalAlignment( "middle" ); //$NON-NLS-1$
        spacer.setWidth( new BigDecimal( spacerWidth ) );
        spacer.setWidthLocked( true );
        totalWidth += spacerWidth;
        spacer.setExpression( "none" ); //$NON-NLS-1$
        spacer.setIsWidthPercent( false );
        spacer.setIsDetail( true );
        reportSpec.addField( spacer );
      }
    }
    try {
      reportSpec.setUseCustomPageFormat( true );
      int width = 612;
      int height = 792;
      if ( orientation.equalsIgnoreCase( "landscape" ) ) { //$NON-NLS-1$
        width = height;
        height = 612;
      }
      // w totalWidth
      // - = ------------
      // h scaledHeight
      int scaledHeight = ( height * totalWidth ) / width;
      if ( orientation.equalsIgnoreCase( "landscape" ) ) { //$NON-NLS-1$
        reportSpec.setCustomPageFormatHeight( totalWidth );
        reportSpec.setCustomPageFormatWidth( scaledHeight );
        ReportGenerationUtility.createJFreeReportXML( reportSpec, outputStream, scaledHeight, totalWidth,
            createTotalColumn, totalColumnName, totalColumnWidth, spacerWidth );
      } else {
        reportSpec.setCustomPageFormatHeight( scaledHeight );
        reportSpec.setCustomPageFormatWidth( totalWidth );
        ReportGenerationUtility.createJFreeReportXML( reportSpec, outputStream, totalWidth, scaledHeight,
            createTotalColumn, totalColumnName, totalColumnWidth, spacerWidth );
      }
    } catch ( Exception e ) {
      //ignore
    }
    return new String( outputStream.toByteArray() );
  }

  public int getType( final Class typeClass ) {
    if ( typeClass != null ) {
      if ( typeClass.getName().equals( String.class.getName() ) ) {
        return Types.VARCHAR;
      } else if ( typeClass.getName().equals( BigDecimal.class.getName() )
          || typeClass.getName().equals( Integer.class.getName() ) ) {
        return Types.NUMERIC;
      } else if ( typeClass.getName().equals( Date.class.getName() ) ) {
        return Types.DATE;
      }
    }
    return Types.VARCHAR;
  }

  public String getGroupLabel( final String columnName, final int index ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        // at this point we verified the column is in the metadata, but is the index a group?
        for ( int j = 0; j < getGroupIndices().length; j++ ) {
          if ( i == getGroupIndices()[j] ) {
            return groupLabels[j];
          }
        }
      }
    }
    return displayNames[index];
  }

  public boolean isGroup( final String columnName ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        // at this point we verified the column is in the metadata, but is the index a group?
        for ( int j = 0; j < getGroupIndices().length; j++ ) {
          if ( i == getGroupIndices()[j] ) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public String getColumnAlignment( final String columnName ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        try {
          String alignment = getColumnAlignments()[getColumnAlignments().length - 1];
          alignment = getColumnAlignments()[i];
          return alignment;
        } catch ( Exception e ) {
          //ignore
        }
      }
    }
    return null;
  }

  public boolean useItemHide( final String columnName ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        boolean itemHide = getItemHides()[getItemHides().length - 1];
        try {
          itemHide = getItemHides()[i];
        } catch ( Exception e ) {
          //ignore
        }
        return itemHide;
      }
    }
    return false;
  }

  public int getWidth( final String columnName ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        int width = getWidths()[getWidths().length - 1];
        try {
          width = getWidths()[i];
        } catch ( Exception e ) {
          //ignore
        }
        return width;
      }
    }
    return 0;
  }

  public String getColumnFormat( final String columnName ) {
    Object[] colHeaders = resultSet.getMetaData().getColumnHeaders()[0];
    for ( int i = 0; i < colHeaders.length; i++ ) {
      if ( colHeaders[i].equals( columnName ) ) {
        String format = ""; //$NON-NLS-1$
        try {
          format = getFormats()[getFormats().length - 1];
          format = getFormats()[i];
        } catch ( Exception e ) {
          //ignore
        }
        return format;
      }
    }
    return ""; //$NON-NLS-1$
  }

  public String getResultOutputName() {
    Set outputs = getOutputNames();
    if ( ( outputs == null ) || ( outputs.size() == 0 ) ) {
      error( "NO OUPUT DEFINED" ); //$NON-NLS-1$
      return null;
    }
    String outputName = (String) outputs.iterator().next();
    return outputName;
  }

  @Override
  public boolean init() {
    // nothing to do here really
    return true;
  }

  /**
   * @return Returns the formats.
   */
  public String[] getFormats() {
    return formats;
  }

  /**
   * @param formats
   *          The formats to set.
   */
  public void setFormats( final String[] formats ) {
    this.formats = formats;
  }

  /**
   * @return Returns the groupLabels.
   */
  public String[] getGroupLabels() {
    return groupLabels;
  }

  /**
   * @param groupLabels
   *          The groupLabels to set.
   */
  public void setGroups( final String[] groupLabels ) {
    this.groupLabels = groupLabels;
  }

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path
   *          The path to set.
   */
  public void setPath( final String path ) {
    this.path = path;
  }

  /**
   * @return Returns the widths.
   */
  public int[] getWidths() {
    return widths;
  }

  /**
   * @param widths
   *          The widths to set.
   */
  public void setWidths( final int[] widths ) {
    this.widths = widths;
  }

  /**
   * @return Returns the item hides
   */
  public boolean[] getItemHides() {
    return itemHides;
  }

  /**
   * @param widths
   *          The item hides to set.
   */
  public void setItemHides( final boolean[] itemHides ) {
    this.itemHides = itemHides;
  }

  /**
   * @return Returns the widths.
   */
  public String[] getColumnAlignments() {
    return columnAlignments;
  }

  /**
   * @param widths
   *          The widths to set.
   */
  public void setColumnAlignments( final String[] columnAlignments ) {
    this.columnAlignments = columnAlignments;
  }

  public int[] getGroupIndices() {
    return groupIndices;
  }

  public void setGroupIndices( final int[] groupIndices ) {
    this.groupIndices = groupIndices;
  }

  public String getNullString() {
    return nullString;
  }

  public void setNullString( final String nullString ) {
    this.nullString = nullString;
  }
}
