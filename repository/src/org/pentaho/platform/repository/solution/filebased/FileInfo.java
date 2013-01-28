/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created on Apr 24, 2005
 *
 */
package org.pentaho.platform.repository.solution.filebased;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.repository.messages.Messages;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FileInfo {

  public static final String FILE_TYPE_ACTIVITY = "FILE.ACTIVITY"; //$NON-NLS-1$

  public static final String FILE_TYPE_FOLDER = "FILE.FOLDER"; //$NON-NLS-1$

  public static final String FILE_TYPE_RULES = "FILE_RULES"; //$NON-NLS-1$

  public static final String FILE_TYPE_REPORT = "FILE_REPORT"; //$NON-NLS-1$

  public static final String FILE_TYPE_WORKFLOW = "FILE_WORKFLOW"; //$NON-NLS-1$

  public static final String FILE_TYPE_XPDL = "FILE_XPDL"; //$NON-NLS-1$

  public static final String FILE_TYPE_BIRT = "FILE_BIRT"; //$NON-NLS-1$

  public static final String FILE_TYPE_MODEL = "FILE_MODEL"; //$NON-NLS-1$

  public static final String FILE_TYPE_VIEW = "FILE_VIEW"; //$NON-NLS-1$

  public static final String FILE_TYPE_CONTENT = "FILE_CONTENT"; //$NON-NLS-1$

  public static final String FILE_TYPE_XML = "FILE_XML"; //$NON-NLS-1$

  public static final String FILE_TYPE_INDEX = "FILE_INDEX"; //$NON-NLS-1$

  public static final String FILE_TYPE_URL = "FILE.URL"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_SOLUTION = "solution"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_FOLDER = "folder"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_REPORT = "report"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_PROCESS = "process"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_RULE = "rule"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_VIEW = "view"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_URL = "url"; //$NON-NLS-1$

  public static final String FILE_DISPLAY_TYPE_UNKNOWN = "unknown"; //$NON-NLS-1$

  private String author;

  private String fileName;

  private String solutionId;

  private String path;

  private String name;

  private String description;

  private boolean hasParameters;

  private Date lastUpdated;

  private long size;

  private List parameterNames;

  private String type;

  private String mimeType;

  private String iconPath;

  private String url;

  private String displayType;

  private boolean visible;

  public FileInfo() {
  }

  public FileInfo(final String fileName, final Date lastModified, final String path, final String solutionId,
      final FileBasedSolutionRepository repository) {

    this.path = path;
    this.solutionId = solutionId;
    String fileNameCaseless = fileName.toLowerCase();
    hasParameters = false;
    iconPath = null;
    url = null;
    displayType = FileInfo.FILE_DISPLAY_TYPE_UNKNOWN;

    if (fileNameCaseless.endsWith(".xaction")) { //$NON-NLS-1$
      // this is dynamic content - open the document to get the
      // descriptions
      Document doc = repository.getSolutionDocument(solutionId, path, fileName, ISolutionRepository.ACTION_EXECUTE);
      if (doc == null) {
        if (doc != null) {
          type = FileInfo.FILE_TYPE_ACTIVITY;
          mimeType = "text/xml"; //$NON-NLS-1$
          name = doc.selectSingleNode("/pentaho-activity/activity-info/name").getText(); //$NON-NLS-1$
          description = doc.selectSingleNode("/pentaho-activity/activity-info/description").getText(); //$NON-NLS-1$
          author = doc.selectSingleNode("/pentaho-activity/activity-info/author").getText(); //$NON-NLS-1$
          Node node = doc.selectSingleNode("/pentaho-activity/activity-info/display-type"); //$NON-NLS-1$
          if (node != null) {
            displayType = node.getText();
            if (!displayType.equals(FileInfo.FILE_DISPLAY_TYPE_PROCESS)
                && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_REPORT)
                && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_RULE)
                && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_VIEW)) {
              displayType = FileInfo.FILE_DISPLAY_TYPE_UNKNOWN;
            }
          }
          node = doc.selectSingleNode("/pentaho-activity/activity-info/visible"); //$NON-NLS-1$
          if (node != null) {
            visible = "true".equalsIgnoreCase(node.getText()); //$NON-NLS-1$
          } else {
            visible = false;
          }
          node = doc.selectSingleNode("/pentaho-activity/activity-info/result-mime-type"); //$NON-NLS-1$
          if (node != null) {
            mimeType = node.getText();

          }
          lastUpdated = null;
          size = -1;
          // TODO: read parameters from first activity in file
          List params = doc.selectNodes("/pentaho-activity/activity-definition[1]/parameters/parameter/name"); //$NON-NLS-1$
          if (params != null) {
            Iterator it = params.iterator();
            parameterNames = new ArrayList();
            while (it.hasNext()) {
              parameterNames.add(((Element) it.next()).getText());
              hasParameters = true;
            }
          }
        }
      }
    } else if (fileNameCaseless.endsWith(".xml")) { //$NON-NLS-1$
      visible = false;
      // see if this is a pentaho document
      if (fileNameCaseless.endsWith("rules.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_RULES;
      } else if (fileNameCaseless.endsWith("birt.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_BIRT;
        visible = true;
      } else if (fileNameCaseless.endsWith("report.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_REPORT;
      } else if (fileNameCaseless.endsWith("workflow.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_WORKFLOW;
      } else if (fileNameCaseless.endsWith("view.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_VIEW;
      } else if (fileNameCaseless.endsWith("model.xml")) { //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_MODEL;
      } else if (fileNameCaseless.endsWith(ISolutionRepository.INDEX_FILENAME)) {
        type = FileInfo.FILE_TYPE_INDEX;
      } else {
        type = FileInfo.FILE_TYPE_XML;
      }

      mimeType = "text/xml"; //$NON-NLS-1$
      Document doc = repository.getSolutionDocument(solutionId, path, fileName, ISolutionRepository.ACTION_EXECUTE);
      if (doc != null) {
        Node node = doc.selectSingleNode("//file-info/name"); //$NON-NLS-1$
        if (node == null) {
          name = fileName.replace('_', ' ');
        } else {
          name = node.getText();
        }
        node = doc.selectSingleNode("//file-info/description"); //$NON-NLS-1$
        if (node == null) {
          description = ""; //$NON-NLS-1$
        } else {
          description = node.getText();
        }
        node = doc.selectSingleNode("//file-info/author"); //$NON-NLS-1$
        if (node == null) {
          author = ""; //$NON-NLS-1$
        } else {
          author = node.getText();
        }
        node = doc.selectSingleNode("//file-info/icon"); //$NON-NLS-1$
        if (node == null) {
          iconPath = ""; //$NON-NLS-1$
        } else {
          iconPath = node.getText();
        }
        node = doc.selectSingleNode("//file-info/url"); //$NON-NLS-1$
        if (node == null) {
          url = ""; //$NON-NLS-1$
        } else {
          url = node.getText();
          type = FileInfo.FILE_TYPE_URL;
        }
        node = doc.selectSingleNode("//file-info/visible"); //$NON-NLS-1$
        if (node != null) {
          visible = "true".equalsIgnoreCase(node.getText()); //$NON-NLS-1$
        }
        node = doc.selectSingleNode("//file-info/result-mime-type"); //$NON-NLS-1$
        if (node != null) {
          mimeType = node.getText();
        }
        node = doc.selectSingleNode("//file-info/display-type"); //$NON-NLS-1$
        if (node != null) {
          displayType = node.getText();
          if (!displayType.equals(FileInfo.FILE_DISPLAY_TYPE_PROCESS)
              && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_REPORT)
              && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_RULE)
              && !displayType.equals(FileInfo.FILE_DISPLAY_TYPE_VIEW)) {
            displayType = FileInfo.FILE_DISPLAY_TYPE_UNKNOWN;
          }
        }

        lastUpdated = null;
        size = -1;
        // TODO: read parameters from first activity in file
        List params = doc.selectNodes("//parameters/parameter/name"); //$NON-NLS-1$
        if (params != null) {
          Iterator it = params.iterator();
          parameterNames = new ArrayList();
          while (it.hasNext()) {
            parameterNames.add(((Element) it.next()).getText());
            hasParameters = true;
          }
        }

      }
    } else {
      // this is static content
      if (fileNameCaseless.endsWith(".xpdl")) { //$NON-NLS-1$
        mimeType = "text/xml"; //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_XPDL;
        visible = false;
      } else if (fileNameCaseless.endsWith(".pdf")) { //$NON-NLS-1$
        mimeType = "application/pdf"; //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_CONTENT;
        visible = true;
      } else if (fileNameCaseless.endsWith(".html")) { //$NON-NLS-1$
        mimeType = "text/html"; //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_CONTENT;
        visible = true;
      } else if (fileNameCaseless.endsWith(".htm")) { //$NON-NLS-1$
        mimeType = "text/html"; //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_CONTENT;
        visible = true;
      } else if (fileNameCaseless.endsWith(".xhtml")) { //$NON-NLS-1$
        mimeType = "text/html"; // until browser support for xhtml improves should be "text/xhtml+xml" //$NON-NLS-1$
        type = FileInfo.FILE_TYPE_CONTENT;
        visible = true;
      }
      name = fileName.replace('_', ' ');
      author = ""; //$NON-NLS-1$
      description = ""; //$NON-NLS-1$
      hasParameters = false;
      lastUpdated = lastModified;
      size = -1;
      parameterNames = null;
    }

  }

  public FileInfo(final Element node, final ILogger logger) {
    String fileType = node.attributeValue("type"); //$NON-NLS-1$
    if (fileType == null) {
      // we don't know what to do with this
      logger.error(Messages.getInstance().getErrorString("FileInfo.ERROR_0001_DOCUMENT_HAS_NO_TYPE")); //$NON-NLS-1$
    } else if (node.attributeValue("type").equals(FileInfo.FILE_TYPE_FOLDER)) { //$NON-NLS-1$
      initFolderFromNode(node, logger);
    } else {
      initFileInfoFromNode(node, logger);
    }
  }

  private void initFileInfoFromNode(final Element node, final ILogger logger) {

    try {
      type = node.attributeValue("type"); //$NON-NLS-1$
      mimeType = node.attributeValue("mimetype"); //$NON-NLS-1$
      displayType = node.attributeValue("displaytype"); //$NON-NLS-1$
      visible = "true".equalsIgnoreCase(node.attributeValue("visible")); //$NON-NLS-1$ //$NON-NLS-2$
      Node tmpNode = node.selectSingleNode("filename"); //$NON-NLS-1$
      if (tmpNode != null) {
        fileName = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("solution"); //$NON-NLS-1$
      if (tmpNode != null) {
        solutionId = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("path"); //$NON-NLS-1$
      if (tmpNode != null) {
        path = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("name"); //$NON-NLS-1$
      if (tmpNode != null) {
        name = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("description"); //$NON-NLS-1$
      if (tmpNode != null) {
        description = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("has-parameters"); //$NON-NLS-1$
      if (tmpNode != null) {
        hasParameters = "true".equals(tmpNode.getText()); //$NON-NLS-1$
      }
      tmpNode = node.selectSingleNode("last-modified"); //$NON-NLS-1$
      if (tmpNode != null) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
        try {
          lastUpdated = format.parse(tmpNode.getText());
        } catch (Exception e) {
          lastUpdated = null;
        }
      }
      size = -1;
      tmpNode = node.selectSingleNode("size"); //$NON-NLS-1$
      if (tmpNode != null) {
        size = new Long(tmpNode.getText()).longValue();
      }
      tmpNode = node.selectSingleNode("author"); //$NON-NLS-1$
      if (tmpNode != null) {
        author = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("icon"); //$NON-NLS-1$
      if (tmpNode != null) {
        iconPath = tmpNode.getText();
      }
      tmpNode = node.selectSingleNode("url"); //$NON-NLS-1$
      if (tmpNode != null) {
        url = tmpNode.getText();
      }
      List parameterList = node.selectNodes("parameters/parameter/name"); //$NON-NLS-1$
      hasParameters = false;
      if (parameterList != null) {
        parameterNames = new ArrayList();
        Iterator it = parameterList.iterator();
        while (it.hasNext()) {
          parameterNames.add(((Node) it.next()).getText());
          hasParameters = true;
        }
      } else {
        parameterNames = null;
      }
    } catch (Exception e) {
      logger.error(Messages.getInstance().getErrorString("FileInfo.ERROR_0002_COULD_NOT_LOAD"), e); //$NON-NLS-1$
    }
  }

  private void initFolderFromNode(final Element node, final ILogger logger) {
    try {
      type = FileInfo.FILE_TYPE_FOLDER;
      path = node.selectSingleNode("path").getText(); //$NON-NLS-1$
      name = node.selectSingleNode("name").getText(); //$NON-NLS-1$  
      description = node.selectSingleNode("description").getText(); //$NON-NLS-1$
      iconPath = node.selectSingleNode("icon").getText(); //$NON-NLS-1$
      visible = "true".equalsIgnoreCase(node.selectSingleNode("@visible").getText()); //$NON-NLS-1$ //$NON-NLS-2$
      solutionId = node.selectSingleNode("solution").getText(); //$NON-NLS-1$
    } catch (Exception e) {
      logger.error(Messages.getInstance().getErrorString("FileInfo.ERROR_0002_COULD_NOT_LOAD"), e); //$NON-NLS-1$
    }
  }

  public Element toXmlNode(final Element parent) {
    Element node = parent.addElement("file"); //$NON-NLS-1$
    if (type != null) {
      node.addAttribute("type", type); //$NON-NLS-1$
    }
    if (path != null) {
      node.addElement("path").setText(path); //$NON-NLS-1$
    }
    if (name != null) {
      node.addElement("name").setText(name); //$NON-NLS-1$
    }
    if (mimeType != null) {
      node.addAttribute("mimetype", mimeType); //$NON-NLS-1$
    }
    if (displayType != null) {
      node.addAttribute("displaytype", displayType); //$NON-NLS-1$
    }
    node.addAttribute("visible", Boolean.toString(visible)); //$NON-NLS-1$
    if (author != null) {
      node.addElement("author").setText(author); //$NON-NLS-1$
    }
    if (fileName != null) {
      node.addElement("filename").setText(fileName); //$NON-NLS-1$
    }
    if (solutionId != null) {
      node.addElement("solution").setText(solutionId); //$NON-NLS-1$
    }
    if (url != null) {
      node.addElement("url").setText(url); //$NON-NLS-1$
    }
    if (size != -1) {
      node.addElement("size").setText(new Long(size).toString()); //$NON-NLS-1$
    }
    if (description != null) {
      node.addElement("description").setText(description); //$NON-NLS-1$
    }
    if (iconPath != null) {
      node.addElement("icon").setText(iconPath); //$NON-NLS-1$
    }
    if (lastUpdated == null) {
      node.addElement("last-modified").setText(""); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
      node.addElement("last-updated").setText(format.format(lastUpdated)); //$NON-NLS-1$
    }
    if (parameterNames != null) {
      node.addElement("has-parameters").setText("true"); //$NON-NLS-1$ //$NON-NLS-2$
      Element parametersNode = node.addElement("parameters"); //$NON-NLS-1$
      for (int idx = 0; idx < parameterNames.size(); idx++) {
        parametersNode.addElement("parameter").addElement("name").setText((String) parameterNames.get(idx)); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } else {
      node.addElement("has-parameters").setText("false"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return node;
  }

  public String getAuthor() {
    return author;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public long getSize() {
    return size;
  }

  public boolean getHasParameters() {
    return hasParameters;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public String getFileName() {
    return fileName;
  }

  public String getPath() {
    return path;
  }

  public String getSolutionId() {
    return solutionId;
  }

  public String getDescription() {
    return description;
  }

  public List getParamterNames() {
    return parameterNames;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public void setLastUpdated(final Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public void setSize(final long size) {
    this.size = size;
  }

  public void setHasParameters(final boolean hasParameters) {
    this.hasParameters = hasParameters;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setParamterNames(final List parameterNames) {
    this.parameterNames = parameterNames;
  }

}
