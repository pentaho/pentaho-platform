/**
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 5, 2005 
 * @author William Seyler
 **/

package org.pentaho.platform.plugin.action.builtin;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.print.PrintService;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.awt.AWTRenderer;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.PrinterAction;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.util.messages.LocaleHelper;

/**
 * 
 * Implements a PrintComponent class that will send a attached print file to a
 * specified printer.
 */
public class PrintComponent extends ComponentBase {
  /**
   * 
   */
  private static final long serialVersionUID = 7377566797214172734L;

  private static final String DEFAULT_PRINTER = "PENTAHO_DEFAULT_PRINTER"; // This should never be a real printer //$NON-NLS-1$

  @Override
  public Log getLogger() {
    return LogFactory.getLog(PrintComponent.class);
  }

  @Override
  public boolean init() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // This component does not have any system settings to validate
    return true;
  }

  @Override
  protected boolean validateAction() {
    PrinterAction printerAction = null;
    boolean actionValidated = true;

    if (getActionDefinition() instanceof PrinterAction) {
      printerAction = (PrinterAction) getActionDefinition();

      if ((printerAction.getPrintfile() == ActionInputConstant.NULL_INPUT)
          && (printerAction.getResourcesPrintFile() == null)
          && (printerAction.getReportOutput() == ActionInputConstant.NULL_INPUT)
          && (printerAction.getOutputPrinterName() == null)) {
        actionValidated = false;
        error(Messages.getInstance().getErrorString("PrintComponent.ERROR_0001_NO_PRINT_FILE_DEFINED") + getActionName()); //$NON-NLS-1$
      }
    } else {
      actionValidated = false;
      error(Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$      
    }

    return actionValidated;
  }

  @Override
  protected boolean executeAction() {
    String printFileName = null;
    IActionSequenceResource printFileResource = null;
    PrinterAction printAction = (PrinterAction) getActionDefinition();

    if (printAction.getPrintfile() != ActionInputConstant.NULL_INPUT) {
      printFileName = printAction.getPrintfile().getStringValue();
    } else if (printAction.getResourcesPrintFile() != null) {
      org.pentaho.actionsequence.dom.IActionResource tempResource = printAction.getResourcesPrintFile();
      printFileResource = getResource(tempResource.getName());

    }

    InputStream inStream = null;
    String printerName = printAction.getPrinterName().getStringValue(PrintComponent.DEFAULT_PRINTER);
    String lastPrinter = printAction.getDefaultPrinter().getStringValue();

    if ((printAction.getOutputPrinterName() != null) && !printerName.equals("")) { //$NON-NLS-1$
      IActionOutput output = printAction.getOutputPrinterName();
      output.setValue(printerName);
      if (printAction.getOutputDefaultPrinter() != null) {
        IActionOutput defaultPrinterOutput = printAction.getOutputDefaultPrinter();
        defaultPrinterOutput.setValue(printerName);
      }
      return true;
    }

    PrintService printer = getPrinterInternal(printerName, lastPrinter);
    if (printer == null) {
      if (!feedbackAllowed()) {
        error(Messages.getInstance().getErrorString("PrintComponent.ERROR_0002_NO_SUITABLE_PRINTER")); //$NON-NLS-1$
        return false;
      }
      // we created the printer feedback entry already
      return true;
    }

    if (printAction.getOutputDefaultPrinter() != null) {
      IActionOutput defaultPrinterOutput = printAction.getOutputDefaultPrinter();
      defaultPrinterOutput.setValue(printerName);
    }

    // Get the number of copies
    int copies = printAction.getCopies().getIntValue(1);

    // Check for a valid printFileName or printFile Resource
    if (printFileName != null) {
      inStream = ActionSequenceResource.getInputStream(printFileName, LocaleHelper.getLocale());
    } else if (printFileResource != null) {
      try {
        inStream = getResourceInputStream(printFileResource);
      } catch (FileNotFoundException e) {
        return false;
      }
    } else if (printAction.getReportOutput() != ActionInputConstant.NULL_INPUT) {
      inStream = getInputStream(PrinterAction.REPORT_OUTPUT);
    } else { // This should never happen if we validated ok.
      return false;
    }
    try {


      // Set the input source for sending to the driver.
//      InputSource source = new InputSource(inStream);
      try {

        FopFactory fopFactory = FopFactory.newInstance();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        //Set up our own PrintRenderer instance so we can supply a special PrinterJob instance.
        PrintRenderer renderer = new PrintRenderer(printerJob,copies);
        renderer.setUserAgent(userAgent);
        userAgent.setRendererOverride(renderer);

        // Construct fop with desired output format (here, it is set through the user agent)
        Fop fop = fopFactory.newFop(userAgent);

        // Setup JAXP using identity transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(); // identity transformer

        // Setup input stream
        Source src = new StreamSource(inStream);

        // Resulting SAX events (the generated FO) must be piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        // Start XSLT transformation and FOP processing
        transformer.transform(src, res);

      } catch (Exception ex) {
        return false;
      }
    } finally {
      try {
        inStream.close();
      } catch (IOException ex) {
        // TODO: Provide message here...
        ex.printStackTrace();
      }
    }
    return true;
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub

  }

  /**
   * Takes a printer name and find the associated PrintService. If no match
   * can be made it randomly picks the first printer listed from the call to
   * lookupPrintServices.
   * 
   * @param printerName
   * @return PrintService referenced by the printerName
   */
  public PrintService getPrinterInternal(final String printerName, final String lastPrinterName) {
    // The parameter value was not provided, and we are allowed to create
    // user interface forms

    PrintService[] services = PrinterJob.lookupPrintServices();
    for (PrintService element : services) {
      if (element.getName().equals(printerName)) {
        return element;
      }
    }
    if (feedbackAllowed()) {
      // If it's not valid then lets find one and end this current run.
      ArrayList values = new ArrayList();
      for (PrintService element : services) {
        String value = element.getName();
        values.add(value);
      }
      createFeedbackParameter(StandardSettings.PRINTER_NAME,
          Messages.getInstance().getString("PrintComponent.USER_PRINTER_NAME"), "", lastPrinterName, values, null, "select"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      promptNeeded();
      return null;
    }
    return services[0];
  }

  /**
   * Takes a printer name and find the associated PrintService. If no match
   * can be made it randomly picks the first printer listed from the call to
   * lookupPrintServices.
   * 
   * @param printerName
   * @return PrintService referenced by the printerName
   */
  public static PrintService getPrinter(final String printerName) {
    // The parameter value was not provided, and we are allowed to create
    // user interface forms

    PrintService[] services = PrinterJob.lookupPrintServices();
    for (PrintService element : services) {
      if (element.getName().equals(printerName)) {
        return element;
      }
    }
    return services[0];
  }

  public static PrintService getDefaultPrinter() {
    // The parameter value was not provided, and we are allowed to create
    // user interface forms

    PrintService[] services = PrinterJob.lookupPrintServices();
    if ((services == null) || (services.length == 0)) {
      return null;
    }
    return services[0];
  }

  /**
   * 
   * Extends AWTRenderer to create a class that will print to a specified
   * printerJob
   * 
   */
  class PrintRenderer extends AWTRenderer {

    private static final int EVEN_AND_ALL = 0;

    private static final int EVEN = 1;

    private static final int ODD = 2;

    private int startNumber;

    private int endNumber;

    private int mode = PrintRenderer.EVEN_AND_ALL;

    private int copies = 1;

    private PrinterJob printerJob;

    PrintRenderer(final PrinterJob printerJob, final int copies) {
      super();

      this.printerJob = printerJob;
      this.copies = copies;
      startNumber = 0;
      endNumber = -1;

      printerJob.setPageable(this);
      printerJob.setCopies(this.copies);
      mode = PrintRenderer.EVEN_AND_ALL;
      String str = null;
      if (str != null) {
        try {
          mode = Boolean.valueOf(str).booleanValue() ? PrintRenderer.EVEN : PrintRenderer.ODD;
        } catch (Exception e) {
        }

      }

    }

    @Override
    public void stopRenderer() throws IOException {
      super.stopRenderer();

      try {
        printerJob.print();
      } catch (PrinterException e) {
        e.printStackTrace();
        throw new IOException(Messages.getInstance().getString(
            "PrintComponent.ERROR_0003_UNABLE_TO_PRINT", e.getClass().getName(), e.getMessage())); //$NON-NLS-1$
      }
    }

    //        public void renderPage(Page page) {
    //            pageWidth = (int) (page.getWidth() / 1000f);
    //            pageHeight = (int) (page.getHeight() / 1000f);
    //            super.renderPage(page);
    //        }

    private ArrayList getInvalidPageNumbers() {
      ArrayList vec = new ArrayList();
      int max = this.getNumberOfPages();
      boolean isValid;
      for (int i = 0; i < max; i++) {
        isValid = true;
        if ((i < startNumber) || (i > endNumber)) {
          isValid = false;
        } else if (mode != PrintRenderer.EVEN_AND_ALL) {
          if ((mode == PrintRenderer.EVEN) && ((i + 1) % 2 != 0)) {
            isValid = false;
          } else if ((mode == PrintRenderer.ODD) && ((i + 1) % 2 != 1)) {
            isValid = false;
          }
        }
        if (!isValid) {
          vec.add(i + ""); //$NON-NLS-1$
        }
      }
      return vec;
    }
  } // class PrintRenderer
}