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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jun 29, 2007 
 * @author wseyler
 */

package org.pentaho.platform.plugin.action.mondrian;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * Utility class used to save an analysis action sequence from a JPivot view.
 */
public class AnalysisSaver extends PentahoMessenger {
  private static final long serialVersionUID = 6290291421129174060L;

  private static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$

  private static final String ATTRIBUTE_STRING = "string"; //$NON-NLS-1$

  private static final String TITLE_NODE_NAME = "title"; //$NON-NLS-1$

  public static final String SUFFIX = ".xaction"; //$NON-NLS-1$

  public static final String PROPERTIES_SUFFIX = ".properties"; //$NON-NLS-1$

  private static Log logger = null;
  
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.system.PentahoBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return AnalysisSaver.logger;
  }

  public static int saveAnalysis(final IPentahoSession session, final HashMap props, final String path, String fileName, final boolean overwrite) {

    if ("true".equals(PentahoSystem.getSystemSetting("kiosk-mode", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      throw new RuntimeException(Messages.getInstance().getErrorString("ANALYSISSAVER.ERROR_0006_SAVE_IS_DISABLED")); //$NON-NLS-1$
    }

    int result = 0;
    try {
      AnalysisSaver.logger = LogFactory.getLog(AnalysisSaver.class);
      String baseUrl = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, session);

      // We will (at this point in time) always have an original action sequence to start from...
      String originalActionReference = (String) props.get("actionreference"); //$NON-NLS-1$

      if (originalActionReference == null) {
        throw new MissingParameterException(Messages.getInstance().getErrorString("ANALYSISSAVER.ERROR_0001_MISSING_ACTION_REFERENCE")); //$NON-NLS-1$
      }

      Document document = null;
      try {
        org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
        reader.setEntityResolver(new SolutionURIResolver());
        document =  reader.read(ActionSequenceResource.getInputStream(originalActionReference, LocaleHelper.getLocale()));
      } catch (Throwable t) {
        // XML document can't be read. We'll just return a null document.
      }

      // Update the document with the stuff we passed in on the props
      document = AnalysisSaver.updateDocument(document, props);
      fileName = fileName.endsWith(AnalysisSaver.SUFFIX) ? fileName : fileName + AnalysisSaver.SUFFIX;
      result = solutionRepository.publish(baseUrl, path, fileName, document.asXML().getBytes(document.getXMLEncoding()), overwrite);

      // Now save the resource files
      ActionInfo actionInfo = ActionInfo.parseActionString(originalActionReference);
      String originalPath = actionInfo.getSolutionName() + "/" + actionInfo.getPath(); //$NON-NLS-1$
      String originalFileName = actionInfo.getActionName();
      originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf(AnalysisSaver.SUFFIX));
      ISolutionFile[] parentFiles = solutionRepository.getSolutionFile(originalPath, ISolutionRepository.ACTION_EXECUTE).listFiles();
      String baseFileName = fileName.substring(0, fileName.lastIndexOf(AnalysisSaver.SUFFIX));
      for (ISolutionFile aSolutionFile : parentFiles) {
        if (!aSolutionFile.isDirectory() && aSolutionFile.getFileName().startsWith(originalFileName)
            && aSolutionFile.getFileName().toLowerCase().endsWith(AnalysisSaver.PROPERTIES_SUFFIX)) {
          String newFileName = aSolutionFile.getFileName().replaceFirst(originalFileName, baseFileName);
          result = result & solutionRepository.publish(baseUrl, path, newFileName, aSolutionFile.getData(), overwrite);
        }
      }

      solutionRepository.resetRepository();
    } catch (Exception e) {
      AnalysisSaver.logger.error(Messages.getInstance().getErrorString("ANALYSISSAVER.ERROR_0000_UNKNOWN"), e); //$NON-NLS-1$
      result = ISolutionRepository.FILE_ADD_FAILED;
    }

    return result;
  }

  /**
   * @param document
   * @param props
   * @return
   */
  private static Document updateDocument(final Document document, final HashMap props) {
    try {
      Element componentDefinition = null;
      Element actionOutput = null;
      Element actionSequenceOutput = null;

      Node actionSequence = document.selectSingleNode("/action-sequence"); //$NON-NLS-1$
      if (actionSequence == null) {
        throw new InvalidDocumentException(Messages.getInstance().getErrorString("ANALYSISSAVER.ERROR_0004_INVALID_ORIGIN_DOCUMENT")); //$NON-NLS-1$
      }
      Element asElement = ((Element)actionSequence);
      Node title = null;
      String propertyTitle = (String) props.get(AnalysisSaver.TITLE_NODE_NAME);
      title = asElement.selectSingleNode(AnalysisSaver.TITLE_NODE_NAME);
      if ( (title == null) && (propertyTitle != null) ) {
        title = asElement.addElement(AnalysisSaver.TITLE_NODE_NAME);
      }
      
      if ( (title != null) && (propertyTitle != null)  ) {
        // remove existing text if it's there
        title.setText(""); //$NON-NLS-1$ 
        ((Element)title).addCDATA( propertyTitle ); // adds CDATA
      }

      // Next, we need to retrieve the PivotViewComponent action and
      // process/update it.. there could possibly be more than one
      // PivotViewComponent in an action sequence, however, we have no idea
      // how to figure out which one to process, so we default to picking the last one we found.

      componentDefinition = (Element) document.selectSingleNode("//action-definition[component-name='PivotViewComponent']/component-definition"); //$NON-NLS-1$
      if (componentDefinition == null) {
        throw new InvalidDocumentException(Messages.getInstance().getErrorString("ANALYSISSAVER.ERROR_0005_INVALID_NO_PIVOT_ACTION")); //$NON-NLS-1$
      }

      AnalysisSaver.updateComponent(componentDefinition, props);

      // Get the action's root action-output node, in case we need to add the
      // appropriate outputs for the pivot view...
      actionOutput = (Element) document.selectSingleNode("//action-definition[component-name='PivotViewComponent']/action-outputs"); //$NON-NLS-1$
      AnalysisSaver.updateOutput(actionOutput, props);

      // Get the action's root action sequence output node, in case we need to add the
      // appropriate outputs for the pivot view...
      actionSequenceOutput = (Element) document.selectSingleNode("//action-sequence/outputs"); //$NON-NLS-1$
      AnalysisSaver.updateOutput(actionSequenceOutput, props);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return document;
  }

  /**
   * @param componentDefinition
   * @param props
   */
  private static void updateComponent(final Element componentDefinition, final HashMap props) {
    Iterator iter = props.keySet().iterator();

    while (iter.hasNext()) {
      Object key = iter.next();
      Node node = componentDefinition.selectSingleNode(key.toString());
      if (node == null) {
        node = componentDefinition.addElement(key.toString());
      }
      if (PivotViewComponent.OPTIONS.equals(node.getName())) {
        List optionsList = (List) props.get(key);
        Iterator optsIter = optionsList.iterator();
        while (optsIter.hasNext()) {
          String anOption = optsIter.next().toString();
          Node anOptionNode = node.selectSingleNode(anOption);
          if (anOptionNode == null) {
            ((Element) node).addElement(anOption);
          }
        }
      } else {
        Object value = props.get(key);
        if (value != null) {
          // remove existing text
          node.setText(""); //$NON-NLS-1$
          ((Element)node).addCDATA( value.toString() );
        }
      }
    }
		// the property "mdx" is no longer being put in the hashmap. So,
		// query will be passed properly now.
  }

  /**
   * @param outputNode
   * @param props
   */
  private static void updateOutput(final Element outputNode, final HashMap props) {
    Iterator iter = props.keySet().iterator();

    while (iter.hasNext()) {
      Object key = iter.next();
      Node node = outputNode.selectSingleNode(key.toString());
      if (node == null) {
        outputNode.addElement(key.toString()).addAttribute(AnalysisSaver.ATTRIBUTE_TYPE,
            "options".equals(key.toString()) ? "list" : AnalysisSaver.ATTRIBUTE_STRING);//$NON-NLS-1$//$NON-NLS-2$
      }
    }
  }
}
