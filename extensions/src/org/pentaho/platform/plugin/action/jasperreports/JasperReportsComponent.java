/*
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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.plugin.action.jasperreports;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInput;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.JasperReportAction;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * @author James Dixon and Barry Klawans
 * 
 * JasperReports runner for Pentaho.
 * 
 * This class implements a Pentaho Component that runs JasperReports. It
 * includes full support for parameterization and output in PDF and HTML.
 * 
 * This is a Beta version, with a number of open issues. 1) Data sources must be
 * specified in the config file for EACH report. There should be a default
 * source in the jasper reports configuration. 2) Support for JNDI defined data
 * source has not been added yet. 3) Images are written to the Pentaho temp
 * directory and never cleaned up. They should probably be moved into Session
 * related storage. 4) Support should be added so a Filled report can be
 * persisited into the Pentaho repository and exported repeatedly.
 * 
 * @author Radek Maciaszek <radek@m3.net>
 * 
 * Added handling multiple reports in batch mode which allow to create excel reports with
 * multiple worksheets
 */
public class JasperReportsComponent extends ComponentBase {

  private static final String REMOVE_EMPTY_ROWS = "remove-empty-rows"; //$NON-NLS-1$

  private static final String IMAGE_URL = "image-url"; //$NON-NLS-1$

  private static final String IMAGE_DIR = "image-dir"; //$NON-NLS-1$

  private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$  

  private static final String CONFIG_XML = "jasperreports/jasperreports-conf.xml"; //$NON-NLS-1$   

  private static final String HTML = "html"; //$NON-NLS-1$

  private static final String PDF = "pdf"; //$NON-NLS-1$

  private static final String XML = "xml"; //$NON-NLS-1$

  private static final String XLS = "xls"; //$NON-NLS-1$

  private static final String CSV = "csv"; //$NON-NLS-1$

  private static final String TXT = "txt"; //$NON-NLS-1$

  private static final String RTF = "rtf"; //$NON-NLS-1$

  private static final String NEED_TO_PROMPT = "NeedToPrompt"; //$NON-NLS-1$

  private static final String RETURN_IMMEDIATELY = "ReturnImmediately"; //$NON-NLS-1$

  private static final long serialVersionUID = -4422766007912720969L;

  /**
   * The extension of the JasperReports reports to run. Currently only designs
   * saved as an XML file are supported, not searilized JasperDesign objects.
   */
  public static final String JASPER_REPORTS_DESIGN_EXTENSION = ".jrxml"; //$NON-NLS-1$

  private static Log logger = LogFactory.getLog(JasperReportsComponent.class);

  /**
   * The extension of a compiled JasperReports file.
   */
  public static final String COMPILED_JASPER_REPORTS_EXTENSION = ".jasper"; //$NON-NLS-1$

  /**
   * The parametername of the report directory (to support subreports)
   */
  public static final String REPORT_FOLDER_PARAMETER = "REPORT_FOLDER"; //$NON-NLS-1$

  public Log getLogger() {
    return logger;
  }

  /**
   * Validates the settings in the jasper reports configuration file.
   * <p>
   * Currently the mandatory settings are imageHandling/imageUrl and
   * imageHandling/imageDir, which specify where to store images generated by
   * the report (ie charts) and how to access the images inside an HTML page.
   * @return true if the validation was successful
   */
  protected boolean validateSystemSettings() {
    // make sure that the system settings are valid.
    // In production this will only be called when they have changed, for
    // development purposes we are calling it every time

    // get and validate system settings
    String imageUrl = PentahoSystem.getSystemSetting(CONFIG_XML, "jasperreports/imageHandling/imageUrl", null); //$NON-NLS-1$ 
    String imageDir = PentahoSystem.getSystemSetting(CONFIG_XML, "jasperreports/imageHandling/imageDir", null); //$NON-NLS-1$ 
    String removeEmptyRows = PentahoSystem.getSystemSetting(CONFIG_XML,
        "jasperreports/htmlExportOptions/removeEmptySpaceBetweenRows", "false"); //$NON-NLS-1$ //$NON-NLS-2$ 

    if (debug) {
      debug(Messages.getInstance().getString("JasperReport.DEBUG_IMAGE_URL") + imageUrl); //$NON-NLS-1$
      debug(Messages.getInstance().getString("JasperReport.DEBUG_IMAGE_DIRECTORY") + imageDir); //$NON-NLS-1$
      debug(Messages.getInstance().getString("JasperReport.DEBUG_REMOVE_EMPTRY_ROWS") + removeEmptyRows); //$NON-NLS-1$
    }

    if (imageUrl == null) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0001_IMAGE_URL_NOT_DEFINED")); //$NON-NLS-1$
      return false;
    }
    if (imageDir == null) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0002_IMAGE_DIRECTORY_INVALID")); //$NON-NLS-1$
      return false;
    }

    saveSetting(IMAGE_URL, imageUrl);
    saveSetting(IMAGE_DIR, imageDir);
    saveSetting(REMOVE_EMPTY_ROWS, removeEmptyRows);

    // set a property for these, they will be cached and be available at
    // execution time
    return true;
  }

  /**
   * NOTE: The comments from Pentaho state that in production this will not be
   * called during execution. If so, this info needs to be moved into the
   * execute path as well.
   * 
   * @return true if the validation was successful
   */
  public boolean validateAction() {
    // In production this will only be called during validation and publish,
    // it will not be called before report executions
    // for now it is called before every execution
    boolean actionValidated = true;
    JasperReportAction reportAction = (JasperReportAction) getActionDefinition();

    // get report connection setting
    if (getActionDefinition() instanceof JasperReportAction) {

      // Validate settings are passed. Treat the password as optional.
      if (reportAction.getJndi() == ActionInputConstant.NULL_INPUT) {
        if (reportAction.getDriver() != ActionInputConstant.NULL_INPUT) {
          error(Messages.getInstance().getErrorString("JasperReport.ERROR_0003_JDBC_DRIVER_NOT_SPECIFIED")); //$NON-NLS-1$
          actionValidated = false;
        }
        if (actionValidated && reportAction.getConnection() == ActionInputConstant.NULL_INPUT) {
          error(Messages.getInstance().getErrorString("JasperReport.ERROR_0004_JDBC_CONNECTION_NOT_SPECIFIED")); //$NON-NLS-1$
          actionValidated = false;
        }
        if (actionValidated && reportAction.getUserId() == ActionInputConstant.NULL_INPUT) {
          error(Messages.getInstance().getErrorString("JasperReport.ERROR_0005_JDBC_USER_NOT_SPECIFIED")); //$NON-NLS-1$
          actionValidated = false;
        }
      }
      // check the inputs, we cannot reply on input values during validation
      if (actionValidated && reportAction.getOutputType() == ActionInputConstant.NULL_INPUT) { 
        error(Messages.getInstance().getErrorString("JasperReport.ERROR_0006_OUTPUT_TYPE_NOT_SPECIFIED")); //$NON-NLS-1$
        actionValidated = false;
      }

      // check the resources
      if (actionValidated && reportAction.getReportDefinition() == null) {
        error(Messages.getInstance().getErrorString("JasperReport.ERROR_0007_REPORT_DEFINITION_NOT_SPECIFIED")); //$NON-NLS-1$
        actionValidated = false;
      }
    } else {
      actionValidated = false;
      error(Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$      
    }
    return actionValidated;
  }

  /**
   * Creates and initializes the appropriate exporter for producing output.
   * All channel specific export options will be set for the exporter.
   * 
   * @param outputType
   *            the channel (pdf or html) to use when exporting
   * @param reportName
   *            used to create a unique name for the directory to store images
   *            in. Should be something unique to the invocation, such as the
   *            session id, but I don't know how to get that from the Pentaho
   *            API yet.
   * 
   * @return the exporter to use, or <code>null</code> if the output type is
   *         not valid.
   * 
   * TODO: replace reportName with something unique, like session id.
   */
  private JRExporter getExporter(String outputType, String reportName) {
    JRExporter exporter = null;
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();

    if (HTML.equals(outputType)) { 
      String removeEmptyRows = getStringSetting("removeEmptyRows"); //$NON-NLS-1$
      exporter = new JRHtmlExporter();
      exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
      if (removeEmptyRows != null && "true".equalsIgnoreCase(removeEmptyRows)) { //$NON-NLS-1$
        exporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
      }
      String imageUrl = requestContext.getContextPath() + getStringSetting(IMAGE_URL); //$NON-NLS-1$ //$NON-NLS-2$
      StringBuffer tempImgPath = new StringBuffer().append(PentahoSystem.getApplicationContext().getSolutionPath(getStringSetting(IMAGE_DIR)));

      if (! (tempImgPath.charAt(tempImgPath.length() - 1) == File.separatorChar)) {
        tempImgPath.append(File.separator);
      }
      
      tempImgPath.append(reportName).append(File.separator);

      String imagePath = tempImgPath.toString();
      
      File imageDir = new File(imagePath);
      imageDir.mkdirs();
      exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR, imageDir);
      // exporter.setParameter(JRHtmlExporter.IMAGES_URI, imageUrl +
      // reportName ); //$NON-NLS-1$
      exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, imageUrl + reportName + "/"); //$NON-NLS-1$
      exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
      if (debug) {
        debug(Messages.getInstance().getString("JasperReport.DEBUG_IMAGE_DIRECTORY", imagePath)); //$NON-NLS-1$
      }
    } else if (PDF.equals(outputType)) { 
      exporter = new JRPdfExporter();
    } else if (XLS.equals(outputType)) { 
      exporter = new JRXlsExporter();
      // Some cleaning in order to make excel reports look better
      exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
      exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
      exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);
      exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
    } else if (CSV.equals(outputType)) { 
      exporter = new JRCsvExporter();
    } else if (XML.equals(outputType)) { 
      exporter = new JRXmlExporter();
    } else if (TXT.equals(outputType)) { 
      exporter = new JRTextExporter();
      // Add required parameters
      exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, new Integer("120")); //$NON-NLS-1$
      exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, new Integer("120")); //$NON-NLS-1$
    } else if (RTF.equals(outputType)) { 
      exporter = new JRRtfExporter();
    }
    return exporter;
  }

  /**
   * Runs a report.
   * 
   * @return true if action was successful
   */
  public boolean executeAction() {
    JasperReportAction reportAction = (JasperReportAction) getActionDefinition();

    // perform runtime validation of the output type
    String reportOutputType = reportAction.getOutputType().getStringValue();
    if (debug) {
      debug(Messages.getInstance().getString("JasperReport.DEBUG_OUTPUT_TYPE", reportOutputType)); //$NON-NLS-1$
    }

    String mimeType = getMimeType(reportOutputType);
    if (mimeType == null) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0011_OUTPUT_TYPE_INVALID")); //$NON-NLS-1$
      return false;
    }

    String extension = "." + reportOutputType; //$NON-NLS-1$

    HashSet<String> compiledReportPaths = new HashSet<String>();
    JasperReport jrreport = null;
    // Store parameters for all reports
    HashMap allReportParameters = new HashMap();

    IActionResource reportDefinition = reportAction.getReportDefinition();
    if (reportDefinition != null) {
      IActionSequenceResource resource = getResource(reportDefinition.getName());
      HashMap returnImmediately = new HashMap();

      processCurrentReport(resource, compiledReportPaths, jrreport, allReportParameters, returnImmediately,
          reportOutputType);

      if (!returnImmediately.isEmpty() && returnImmediately.containsKey(RETURN_IMMEDIATELY)) {
        return true;
      }

    } else {
      // This else statement is here to support old action sequence functionality.
      org.pentaho.actionsequence.dom.IActionSequenceResource[] actionSequenceResources = reportAction.getDocument()
          .getResources();
      for (org.pentaho.actionsequence.dom.IActionSequenceResource element : actionSequenceResources) {
        IActionSequenceResource resource = getResource(element.getName());
        HashMap returnImmediately = new HashMap();
        // Compile every report
        processCurrentReport(resource, compiledReportPaths, jrreport, allReportParameters, returnImmediately,
            reportOutputType);

        if (!returnImmediately.isEmpty() && returnImmediately.containsKey(RETURN_IMMEDIATELY)) {
          return true;
        }
      }
    }

    // Try to get the output from the action-sequence document.
    // execute the report here...
    IContentItem contentItem = null;
    OutputStream outputStream = getOutputStream(mimeType, extension, contentItem);
    if (outputStream == null) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0013_OUTPUT_STREAM_INVALID")); //$NON-NLS-1$
      return false;
    }

    return exportReport(compiledReportPaths, allReportParameters, reportOutputType, outputStream, contentItem);
  }

  /*
   * This function does the actual meat work. This method was created to reduce redundancy 
   * in code, that was a part of supporting the new way and old way.
   * NOTE: The input params compiledReportPaths, jrreport and allReportParameters are being updated in the given method.
   * They are in essence being used as inout variables / referene vars. 
   * 
   * @return boolean if processing should continue
   */
  private boolean processCurrentReport(IActionSequenceResource resource, HashSet<String> compiledReportPaths,
      JasperReport jrreport, HashMap allReportParameters, HashMap returnImmediately, String reportOutputType) {
    
    boolean continueProcessing = true;
    String reportDefinitionPath = getReportDefinitionPath(resource);
    String compiledReportPath = ""; //$NON-NLS-1$

    if (reportDefinitionPath == null) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0008_REPORT_DEFINITION_UNREADABLE")); //$NON-NLS-1$
      continueProcessing = false;
    }

    if (continueProcessing) {
      compiledReportPath = getCompiledReportPath(reportDefinitionPath);
      if (null == compiledReportPath) {
        continueProcessing = false;
      }
    }

    if (continueProcessing) {
      compiledReportPaths.add(compiledReportPath);
      continueProcessing = didReportCompile(reportDefinitionPath, compiledReportPath);
    }

    if (continueProcessing) {
      jrreport = loadJasperReport(compiledReportPath);
      if (null == jrreport) {
        continueProcessing = false;
      }
    }

    if (continueProcessing && (jrreport != null) ) {
      // We have a compiled reports, ready to run.          
      JRParameter[] jrparams = jrreport.getParameters();

      if (debug) {
        debug(Messages.getInstance().getString("JasperReport.DEBUG_LOADED_DESIGN", Integer.toString(jrparams.length))); //$NON-NLS-1$
      }

      HashMap needToPrompt = new HashMap();
      Map reportParameters = getReportParameters(jrparams, needToPrompt);

      // We're adding a parameter with the report's location. Useful for
      // subreports
      reportParameters.put(REPORT_FOLDER_PARAMETER, (new java.io.File(reportDefinitionPath)).getParent());

      // Add a ignore pagination parameter for xls, html and csv
      if (XLS.equals(reportOutputType) || HTML.equals(reportOutputType) || CSV.equals(reportOutputType)) {
        reportParameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
      }

      allReportParameters.put(compiledReportPath, reportParameters);

      // If we have parameters to prompt for, have Pentaho create the
      // parameter
      // page for us.
      if (!needToPrompt.isEmpty() && needToPrompt.containsKey(NEED_TO_PROMPT)) {
        // make sure the type parameter comes back to us...
        // context.createFeedbackParameter( "type", "type",
        // reportOutputType, false ); //$NON-NLS-1$ //$NON-NLS-1$
        // //$NON-NLS-2$
        // Isn't the parameter page always HTML?
        setFeedbackMimeType("text/html"); //$NON-NLS-1$
        /*
         *  Creation of the key RETURN IMMEDIATELY is important here.
         *  We would like to return because we need to prompt the 
         *  user for input at this point.
         */
        returnImmediately.put(RETURN_IMMEDIATELY, null);
      } else if (getRuntimeContext().isPromptPending()) {
        returnImmediately.put(RETURN_IMMEDIATELY, null);
      }
    }

    return continueProcessing;
  }

  /*
   * Simply exports the report once we have all the report parameters, the location 
   * of the compiled report.
   */
  private boolean exportReport(HashSet<String> compiledReportPaths, Map allReportParameters, String reportOutputType,
      OutputStream outputStream, IContentItem contentItem) {
    Connection conn = getConnection();
    if (conn == null) {
      return false;
    }

    try {
      String reportBaseName = ""; //$NON-NLS-1$
      List jasperPrintList = new ArrayList();

      for (String compiledReportPath : compiledReportPaths) {
        // Fill reports
        // TODO: Read reportParameters from HashMap
        Map reportParameters = (Map) allReportParameters.get(compiledReportPath);
        JasperPrint jrprint = JasperFillManager.fillReport(compiledReportPath, reportParameters, conn);
        //jasperPrintList.add(JRLoader.loadObject(compiledReportPath));
        jasperPrintList.add(jrprint);

        // Get a configure exporter for the desired output format.
        File compiledReportFile = new File(compiledReportPath);
        String lastReportBaseName = compiledReportFile.getName();

        int extensionIdx = lastReportBaseName.lastIndexOf("."); //$NON-NLS-1$
        if (extensionIdx > 0) {
          lastReportBaseName = lastReportBaseName.substring(0, extensionIdx);
        }

        reportBaseName = reportBaseName.concat(lastReportBaseName);
      }

      JRExporter exporter = getExporter(reportOutputType, reportBaseName);

      // Reverse jasperPrintList order so first resource will appear as first report
      List jasperPrintListReversed = new ArrayList();
      for (int i = jasperPrintList.size() - 1; i >= 0; i--) {
        jasperPrintListReversed.add(jasperPrintList.get(i));
      }

      // Set the filled report and output stream.
      exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintListReversed);
      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

      // Go!
      exporter.exportReport();
    } catch (JRException jre) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0014_REPORT_EXECUTION_FAILED"), jre); //$NON-NLS-1$
      return false;
    } finally {
      try {
        conn.close();
        if (contentItem != null) {
          contentItem.closeOutputStream();
        }
      } catch (SQLException ignored) {
      }
    }
    return true;
  }

  /*
   * Gets all the report parameters required for the report. 
   */
  private Map getReportParameters(JRParameter[] jrparams, HashMap needToPrompt) {

    Map reportParameters = new HashMap();
    for (JRParameter param : jrparams) {
      if (param.isSystemDefined()) {
        continue;
      }

      String parameterName = param.getName();
      // We use Object type so that we can handle String and non-String type params
      Object parameterValue = null;
      IActionInput actionInput = getActionDefinition().getInput(parameterName);

      if (actionInput != null) {
        if ((actionInput instanceof ActionInput) && ((ActionInput) actionInput).getType().equals("string-list")) { //$NON-NLS-1$
          Object parameterObject = getInputValue(parameterName);
          parameterValue = sqlQuote(parameterObject);
        } else {
          parameterValue = getInputStringValue(parameterName);
        }
      }

      if (parameterValue != null && parameterValue.toString().length() != 0) {
        // give the parameter value to the report engine...
        if (debug) {
          debug(Messages.getInstance().getString("JasperReport.DEBUG_ADDING_PARAMETER", parameterName, parameterValue.toString())); //$NON-NLS-1$ 
        }

        reportParameters.put(parameterName, parameterValue);
      }
      // Check for unspecified parameters that need to be specified
      else if (param.isForPrompting()) {
        if (debug) {
          debug(Messages.getInstance().getString("JasperReport.DEBUG_PARAMETER_NEEDED", parameterName)); //$NON-NLS-1$
        }
        // see if we can prompt for this...
        if (feedbackAllowed()) {

          IActionParameter paramParameter = getInputParameter(parameterName);
          if (paramParameter.getPromptStatus() != IActionParameter.PROMPT_PENDING) {
            String displayName = param.getDescription();
            if (displayName == null || displayName.trim().length() == 0) {
              displayName = parameterName;
            }
            String defaultValue = ""; //$NON-NLS-1$
            createFeedbackParameter(parameterName, displayName, "", defaultValue, true); //$NON-NLS-1$
          }
          needToPrompt.put(NEED_TO_PROMPT, Boolean.TRUE);
        }
      }
    }

    return reportParameters;
  }

  /*
   *  Load the current jasper report defined by the compiled report path. 
   */
  private JasperReport loadJasperReport(String compiledReportPath) {
    if (debug) {
      debug(Messages.getInstance().getString("JasperReport.DEBUG_LOADING_REPORT_DESIGN")); //$NON-NLS-1$
    }

    JasperReport jrreport = null;
    try {
      jrreport = (JasperReport) JRLoader.loadObject(compiledReportPath);
    } catch (JRException jre) {
      jrreport = null;
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0012_REPORT_DESIGN_NO_LOADABLE", compiledReportPath), jre); //$NON-NLS-1$      
    }

    return jrreport;
  }

  /*
   * Get the location of the compiled report.
   */
  private String getCompiledReportPath(String reportDefinitionPath) {
    String compiledReportPath = null;

    if (debug) {
      debug(Messages.getInstance().getString("JasperReport.GETTING_REPORT_PATH", reportDefinitionPath)); //$NON-NLS-1$
    }

    // See if we have a compiled report or a report definition. If its a
    // definition, see if a compiled version exists, and if not, compile it.
    // get the base name of the report.
    // Parse the path and get the name of the report.
    File sourceFile = new File(reportDefinitionPath);
    String reportBaseName = sourceFile.getName();
    int extensionIdx = reportBaseName.lastIndexOf("."); //$NON-NLS-1$
    if (extensionIdx > 0)
      reportBaseName = reportBaseName.substring(0, extensionIdx);

    // If we are handed a .jasper file, just use it as the compiled report 
    // definition.
    if (reportDefinitionPath.endsWith(COMPILED_JASPER_REPORTS_EXTENSION)) {
      compiledReportPath = reportDefinitionPath;
    } else {
      // Assume its a .jrxml
      if (!sourceFile.exists()) {
        compiledReportPath = null;
        error(Messages.getInstance().getErrorString("JasperReport.ERROR_0009_REPORT_DEFINITION_MISSING", reportDefinitionPath)); //$NON-NLS-1$
      } else {
        if (debug) {
          debug(Messages.getInstance().getString("JasperReport.DEBUG_REPORT_FILE_FOUND")); //$NON-NLS-1$
        }
        StringBuffer sb = new StringBuffer();
        sb.append(sourceFile.getParent());
        if (!sourceFile.getParent().endsWith(File.separator)) {
          sb.append(File.separator);
        }
        // Get the directory where the report source is, and compile to
        // same directory
        sb.append(reportBaseName).append(COMPILED_JASPER_REPORTS_EXTENSION);
        compiledReportPath = sb.toString();
      }
    }
    return compiledReportPath;
  }

  /*
   * Compile the report and see if the report compiles successfully.
   */
  private boolean didReportCompile(String reportDefinitionPath, String compiledReportPath) {
    boolean reportCompiled = true;

    if (debug) {
      debug(Messages.getInstance().getString("JasperReport.DEBUG_RUNNING_REPORT", reportDefinitionPath)); //$NON-NLS-1$
      debug(Messages.getInstance().getString("JasperReport.DEBUG_COMPILED_REPORT_LOCATION", compiledReportPath)); //$NON-NLS-1$
    }

    // make sure the report is compiled
    File compiledReportFile = new File(compiledReportPath);
    File sourceFile = new File(reportDefinitionPath);

    // We compile if the compiled file doesn't exist, or the source is
    // newer
    if (!compiledReportFile.exists() || sourceFile.lastModified() > compiledReportFile.lastModified()) {
      if (debug) {
        debug(Messages.getInstance().getString("JasperReport.DEBUG_COMPILING_REPORT")); //$NON-NLS-1$
      }
      // We are currently ignoring any error conditions with compiled
      // files
      // that exist but aren't readable.

      // Use the jdt compiler
      System.setProperty("jasper.reports.compiler.class", "net.sf.jasperreports.engine.design.JRJdtCompiler"); //$NON-NLS-1$ //$NON-NLS-2$

      // Compile the report design
      try {
        JasperCompileManager.compileReportToFile(reportDefinitionPath, compiledReportPath);
      } catch (JRException jre) {
        error(Messages.getInstance().getErrorString(
            "JasperReport.ERROR_0010_UNABLE_TO_COMPILE", reportDefinitionPath, compiledReportPath), jre); //$NON-NLS-1$
        reportCompiled = false;
      }

      if (debug && reportCompiled) {
        debug(Messages.getInstance().getString("JasperReport.DEBUG_COMPILED_OK")); //$NON-NLS-1$
      }
    }

    return reportCompiled;
  }

  /*
   * 
   */
  private static String getReportDefinitionPath(IActionSequenceResource resource) {
    String reportDefinitionPath = null;

    if (resource.getSourceType() == IActionSequenceResource.SOLUTION_FILE_RESOURCE) {
      reportDefinitionPath = PentahoSystem.getApplicationContext().getSolutionPath(resource.getAddress());
    } else {
      reportDefinitionPath = resource.getAddress();
    }
    return reportDefinitionPath;
  }

  private static String getMimeType(String reportOutputType) {
    String mimeType = null;

    if (HTML.equals(reportOutputType)) { 
      mimeType = TEXT_HTML; 
    } else if (PDF.equals(reportOutputType)) { 
      mimeType = "application/pdf"; //$NON-NLS-1$
    } else if (XLS.equals(reportOutputType)) { 
      mimeType = "application/vnd.ms-excel"; //$NON-NLS-1$
    } else if (CSV.equals(reportOutputType)) { 
      mimeType = "text/text"; //$NON-NLS-1$
    } else if (XML.equals(reportOutputType)) { 
      mimeType = "text/xml"; //$NON-NLS-1$
    } else if (TXT.equals(reportOutputType)) { 
      mimeType = "text/plain"; //$NON-NLS-1$
    } else if (RTF.equals(reportOutputType)) { 
      mimeType = "application/rtf"; //$NON-NLS-1$
    } else {
      mimeType = "application/octet-stream"; //$NON-NLS-1$
    }
    return mimeType;
  }

  @SuppressWarnings("deprecation")
  private OutputStream getOutputStream(String mimeType, String extension, IContentItem contentItem) {
    OutputStream outputStream = null;
    JasperReportAction reportAction = (JasperReportAction) getActionDefinition();
    if (reportAction.getOutputReport() != null) {
      //contentItem = getOutputItem(JasperReportAction.REPORT_OUTPUT_ELEMENT, mimeType, extension);
      contentItem = getOutputItem(reportAction.getOutputReport().getName(), mimeType, extension);
      try {
        outputStream = contentItem.getOutputStream(getActionName());
      } catch (IOException e) {
        outputStream = null;
      }
    } else if (getOutputNames().size() == 1) {
      String outputName = (String) getOutputNames().iterator().next();
      contentItem = getOutputContentItem(outputName, mimeType);
      try {
        outputStream = contentItem.getOutputStream(getActionName());
      } catch (IOException e) {
        outputStream = null;
      }
    } else {
      // There was no output in the action-sequence document, so make a
      // default
      // outputStream.
      warn(Messages.getInstance().getString("Base.WARN_NO_OUTPUT_STREAM")); //$NON-NLS-1$
      outputStream = getDefaultOutputStream(mimeType);
      if (outputStream != null) {
        setOutputMimeType(mimeType);
      }
    }
    return outputStream;
  }

  public boolean init() {
    // TODO any initialization you need to do before execution

    return true;
  }

  public void done() {
    // perform any cleanup necessary
  }

  private Connection getConnection() {
    try {
      // get the report settings
      JasperReportAction reportAction = (JasperReportAction) getActionDefinition();
      String jndiUrl = reportAction.getJndi().getStringValue();
      Connection conn = null;
      if (jndiUrl != null) {
      	IDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDatasourceService.class ,null);    	  
        DataSource ds = datasourceService.getDataSource(jndiUrl);
        return ds.getConnection();
      } else {
        String driver = reportAction.getDriver().getStringValue();
        String connectString = reportAction.getConnection().getStringValue();
        String user = reportAction.getUserId().getStringValue();
        String password = reportAction.getPassword().getStringValue();
        Class.forName(driver);
        conn = DriverManager.getConnection(connectString, user, password);
      }
      return conn;
    } catch (ObjectFactoryException objface) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0017_UNABLE_TO_FACTORY_OBJECT")); //$NON-NLS-1$      
    } catch (ClassNotFoundException cnfe) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0015_JDBC_DRIVER_LOAD_FAILED")); //$NON-NLS-1$
    } catch (SQLException se) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0016_DATABASE_CONNECTION_FAILED"), se); //$NON-NLS-1$
    } catch (DatasourceServiceException dse) {
      error(Messages.getInstance().getErrorString("JasperReport.ERROR_0016_DATABASE_CONNECTION_FAILED"), dse); //$NON-NLS-1$
    }
    return null;
  }

  private String sqlQuote(Object obj) {
    if (obj == null) {
      return (null);
    }

    String out = null;

    if (obj instanceof String[]) {
      // quote everything, and join with ''
      List c = Arrays.asList((String[]) obj);
      out = quoteMe(c);
    } else if (obj instanceof List) {
      out = quoteMe((Collection) obj);
    } else {
      if (!(obj instanceof String)) {
        warn("Unknown type " + obj.getClass() + " in object " + obj); //$NON-NLS-1$ //$NON-NLS-2$
      }
      out = "'" + obj.toString().replaceAll("'", "'") + "'";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    return out;
  }

  private static String quoteMe(Collection c) {
    if (c.size() == 0) {
      return ""; //$NON-NLS-1$
    }

    StringBuffer sb = new StringBuffer();
    for (Object value : c) {
      sb.append(",'"); //$NON-NLS-1$
      sb.append(value.toString().replaceAll("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append("'"); //$NON-NLS-1$
    }
    return sb.substring(1);
  }

}
