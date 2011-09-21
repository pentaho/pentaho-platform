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
 * Created on Jun 21, 2005
 */

package org.pentaho.platform.repository.solution.filebased;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.actionsequence.SequenceDefinition;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.solution.SolutionRepositoryBase;
import org.pentaho.platform.util.FileHelper;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * @author James Dixon TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class FileBasedSolutionRepository extends SolutionRepositoryBase {
  /**
   * 
   */
  private static final long serialVersionUID = -8270135463210017284L;
  
  private ISolutionFilter defaultSolutionFilter = new DefaultSolutionFilter();

  private boolean useActionSequenceCaching = false;

  private boolean repositoryInit = false;
  
  public FileBasedSolutionRepository() {
    super();
    init();
  }

  @Override
  public void init() {
    if (!repositoryInit) {
      super.init();

      String flag = PentahoSystem.getSystemSetting("filebased-solution-cache", "false"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        useActionSequenceCaching = Boolean.getBoolean(flag);
      } catch (Exception e) {
        useActionSequenceCaching = false;
      }
      repositoryInit = true;
    }
  }

  public ClassLoader getClassLoader(final String path) {
    File localeDir = new File(PentahoSystem.getApplicationContext().getSolutionPath(path));
    try {
      URLClassLoader loader = new URLClassLoader(new URL[] { localeDir.toURL() }, null);
      return loader;
    } catch (Exception e) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0024_CREATING_CLASSLOADER")); //$NON-NLS-1$
    }
    return null;
  }

  public IActionSequence getActionSequence(final String solutionName, final String actionPath,
      final String sequenceName, final int localLoggingLevel, final int actionOperation) {
    String action;

    IActionSequence actionSequence = null;
    if ((actionPath != null) && !"".equals(actionPath)) { //$NON-NLS-1$
      action = solutionName + File.separator + actionPath + File.separator + sequenceName;
    } else {
      action = solutionName + File.separator + sequenceName;
    }

    if (useActionSequenceCaching) {
      actionSequence = (IActionSequence) getRepositoryObjectFromCache(action);
    }
    if (actionSequence != null) {
      return actionSequence;
    }
    Document actionSequenceDocument = getSolutionDocument(action, actionOperation);
    if (actionSequenceDocument == null) {
      return null;
    }

    actionSequence = SequenceDefinition.ActionSequenceFactory(actionSequenceDocument, actionPath, 
         this, PentahoSystem.getApplicationContext(), localLoggingLevel);
    if (actionSequence == null) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0016_FAILED_TO_CREATE_ACTION_SEQUENCE", action)); //$NON-NLS-1$
      return null;
    }

    if (useActionSequenceCaching) {
      putRepositoryObjectInCache(action, actionSequence);
    }

    return actionSequence;
  }

  public Document getSolutionDocument(final String solutionName, final String actionPath, final String actionName, final int actionOperation) {
    return getSolutionDocument(solutionName + File.separator + actionPath + File.separator + actionName, actionOperation);
  }

  public Document getSolutionDocument(final String documentPath, final int actionOperation) {
    // TODO: caching
    File file = getFile(documentPath, false);
    // Handle the exception when you have the best knowledge about it.
    if (null == file) {
      // takes care of null pointer exception
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0001_FILE_DOES_NOT_EXIST", documentPath)); //$NON-NLS-1$
      return null;
    } else {
      if (!file.exists()) {
        error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0001_FILE_DOES_NOT_EXIST", documentPath)); //$NON-NLS-1$
        return null;
      } else if (!file.canRead()) {
        error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0020_FILE_IS_NOT_READABLE", documentPath)); //$NON-NLS-1$
        return null;
      }
    }

    Document document = null;
    try {
      document = XmlDom4JHelper.getDocFromFile(file, new SolutionURIResolver());
    } catch (Exception e) {
      error(Messages.getInstance().getString("SolutionRepository.ERROR_0009_INVALID_DOCUMENT", documentPath)); //$NON-NLS-1$
      return null;
    }
    localizeDoc(document, new FileSolutionFile(file, ((FileSolutionFile) getRootFolder(actionOperation)).getFile()));
    return document;
  }

  private File getFile(final IRuntimeContext runtimeContext, final String path) {
    if (runtimeContext == null) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0002_NULL_RUNTIME_CONTEXT")); //$NON-NLS-1$
      return null;
    }
    File f = getFile(path, false);
    if (!f.exists()) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0003_NULL_SOLUTION_FILE", path)); //$NON-NLS-1$
      return null;
    }
    // TODO: caching
    if (SolutionRepositoryBase.debug) {
      debug(Messages.getInstance().getString("SolutionRepository.DEBUG_FILE_PATH", f.getAbsolutePath())); //$NON-NLS-1$
    }
    return f;
  }

  protected String getActionDefinition(final IRuntimeContext runtimeContext, final String actionPath) {
    // TODO: caching
    if ((runtimeContext == null) || (runtimeContext.getInstanceId() == null)
        || (runtimeContext.getActionName() == null)) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0004_INVALID_CONTEXT")); //$NON-NLS-1$
    }
    genLogIdFromInfo(runtimeContext.getInstanceId(), SolutionRepositoryBase.LOG_NAME, runtimeContext.getActionName());
    File f = getFile(runtimeContext, actionPath);
    if (f == null) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0005_INVALID_SOLUTION_FILE") + actionPath); //$NON-NLS-1$
    }
    return FileHelper.getStringFromFile(f);
  }

  protected Document getActionDefinitionDocument(final IRuntimeContext runtimeContext, final String actionPath) {
    // TODO: caching
    genLogIdFromInfo(runtimeContext.getInstanceId(), SolutionRepositoryBase.LOG_NAME, runtimeContext.getActionName());
    File f = getFile(runtimeContext, actionPath);
    if (f == null) {
      return null;
    }
    Document document = null;
    try {
      document = XmlDom4JHelper.getDocFromFile(f, new SolutionURIResolver());
    } catch (Exception e) {
      error(Messages.getInstance().getString("SolutionRepository.ERROR_0009_INVALID_DOCUMENT", actionPath), e); //$NON-NLS-1$
      return null;
    }
    return document;
  }

  // Old prototype code that needs to be redone somehow...
  public void reloadSolutionRepository(final IPentahoSession localSession, final int localLoggingLevel) {
    if (isCachingAvailable()) {
      File rootDir = getFile("", false); //$NON-NLS-1$
      Document repository = DocumentHelper.createDocument();
      Element rootNode = repository.addElement("repository"); //$NON-NLS-1$
      processDir(rootNode, rootDir, null, 0, ISolutionRepository.ACTION_ADMIN);
      resetRepository();
      putRepositoryObjectInCache(getRepositoryKey(), repository);
    }
  }

  protected void processDir(final Element parentNode, final File parentDir, final String solutionId, int pathIdx,
      final int actionOperation) {
    File files[] = parentDir.listFiles();
    for (File element : files) {
      if (!element.isDirectory()) {
        String fileName = element.getName();
        processFile( fileName, element, parentNode, solutionId, pathIdx, actionOperation );
      }
    }
    for (File element : files) {
      if (element.isDirectory()
          && (!element.getName().equalsIgnoreCase("system")) && (!element.getName().equalsIgnoreCase("CVS")) && (!element.getName().equalsIgnoreCase(".svn"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
        dirNode.addAttribute("type", FileInfo.FILE_TYPE_FOLDER); //$NON-NLS-1$
        // TODO read this from the directory index file
        String thisSolution;
        String path = ""; //$NON-NLS-1$
        if (solutionId == null) {
          thisSolution = element.getName();
          pathIdx = rootPath.length() + File.separator.length() + thisSolution.length();
        } else {
          thisSolution = solutionId;
          path = element.getAbsolutePath().substring(pathIdx);
          // windows \ characters in the path gets messy in urls, so
          // switch them to /
          path = path.replace('\\', '/');
          dirNode.addElement("path").setText(path); //$NON-NLS-1$
        }
        File indexFile = new File(element, ISolutionRepository.INDEX_FILENAME);
        Document indexDoc = null;
        if (indexFile.exists()) {
          indexDoc = getSolutionDocument(thisSolution, path, ISolutionRepository.INDEX_FILENAME, actionOperation);
        }
        if (indexDoc != null) {
          addIndexToRepository(indexDoc, element, dirNode, path, thisSolution);
        } else {
          dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
          String dirName = element.getName();
          dirNode.addAttribute("name", XmlHelper.encode(dirName)); //$NON-NLS-1$
          dirNode.addElement("title").setText(dirName); //$NON-NLS-1$
        }
        processDir(dirNode, element, thisSolution, pathIdx, actionOperation);
      } else if ((solutionId == null) && element.getName().equalsIgnoreCase(ISolutionRepository.INDEX_FILENAME)) {
        Document indexDoc = null;
        indexDoc = getSolutionDocument("", "", ISolutionRepository.INDEX_FILENAME, actionOperation); //$NON-NLS-1$ //$NON-NLS-2$
        if (indexDoc != null) {
          addIndexToRepository(indexDoc, parentDir, parentNode, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
  }

protected void processFile( String fileName, File element, final Element parentNode, final String solutionId, int pathIdx,
      final int actionOperation ) {
    if (fileName.equals("Entries") || fileName.equals("Repository") || fileName.equals("Root")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // ignore any CVS files
        return;
      }
    int lastPoint = fileName.lastIndexOf('.');
    if( lastPoint == -1 ) {
      // ignore anything with no extension
      return;
    }
    String extension = fileName.substring( lastPoint+1 ).toLowerCase();
      String solutionPath = element.getAbsolutePath().substring(rootPath.length());
      if ("url".equals( extension )) { //$NON-NLS-1$
        addUrlToRepository(element, parentNode, solutionPath, actionOperation);
      }
      boolean addFile = "xaction".equals( extension ); //$NON-NLS-1$
      IPluginManager pluginManager = (IPluginManager) PentahoSystem.get(IPluginManager.class, getSession());
      if( pluginManager != null ) {
          Set<String> types = pluginManager.getContentTypes();
          addFile |= types != null && types.contains( extension );
      }
      if( !addFile ) {
        return;
      }
      String path = element.getAbsolutePath().substring(pathIdx);
      if (!path.equals(fileName)) {
        path = path.substring(0, path.length() - fileName.length() - 1);
        // windows \ characters in the path gets messy in urls, so
        // switch them to /
        path = path.replace('\\', '/');
      } else {
        path = ""; //$NON-NLS-1$
      }
      if (fileName.toLowerCase().endsWith(".xaction")) { //$NON-NLS-1$ 
        // create an action sequence document from this
        info(Messages.getInstance().getString("SolutionRepository.DEBUG_ADDING_ACTION", fileName)); //$NON-NLS-1$
        IActionSequence actionSequence = getActionSequence(solutionId, path, fileName, loggingLevel, actionOperation);
        if (actionSequence == null) {
          error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0006_INVALID_SEQUENCE_DOCUMENT", fileName)); //$NON-NLS-1$
        } else {
          addToRepository(actionSequence, parentNode, element);
        }
      }
      else if( pluginManager != null ) {
        String fullPath = solutionId+ISolutionRepository.SEPARATOR+((StringUtil.isEmpty(path)) ? "" : path+ISolutionRepository.SEPARATOR )+fileName; //$NON-NLS-1$
        try {
            IFileInfo fileInfo = getFileInfo( solutionId, path, fileName, extension, pluginManager, ACTION_EXECUTE );
              addToRepository( fileInfo, solutionId, path, fileName, parentNode, element);
        } catch (Exception e) {
          error( Messages.getInstance().getErrorString( "SolutionRepository.ERROR_0021_FILE_NOT_ADDED", fullPath ), e ); //$NON-NLS-1$
        }
      }

}

private void addToRepository( final IFileInfo info, final String solution, final String path, final String fileName, final Element parentNode, final File file) {
    Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
    dirNode.addAttribute("type", FileInfo.FILE_TYPE_ACTIVITY); //$NON-NLS-1$
    dirNode.addElement("filename").setText(fileName); //$NON-NLS-1$
    dirNode.addElement("path").setText(path); //$NON-NLS-1$
    dirNode.addElement("solution").setText(solution); //$NON-NLS-1$
    dirNode.addElement("title").setText( info.getTitle() ); //$NON-NLS-1$
    String description = info.getDescription();
    if (description == null) {
      dirNode.addElement("description"); //$NON-NLS-1$
    } else {
      dirNode.addElement("description").setText(description); //$NON-NLS-1$
    }
    String author = info.getAuthor();
    if (author == null) {
      dirNode.addElement("author"); //$NON-NLS-1$
    } else {
      dirNode.addElement("author").setText(author); //$NON-NLS-1$
    }
    String iconPath = info.getIcon();
    if ((iconPath != null) && !iconPath.equals("")) { //$NON-NLS-1$
      String rolloverIconPath = null;
      int rolloverIndex = iconPath.indexOf("|"); //$NON-NLS-1$
      if (rolloverIndex > -1) {
        rolloverIconPath = iconPath.substring(rolloverIndex + 1);
        iconPath = iconPath.substring(0, rolloverIndex);
      }
      if (publishIcon(file.getParentFile().getAbsolutePath(), iconPath)) {
        dirNode.addElement("icon").setText("getImage?image=icons/" + iconPath); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        dirNode.addElement("icon").setText(info.getIcon()); //$NON-NLS-1$
      }
      if (rolloverIconPath != null) {
        if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(
            solution + File.separator + path), rolloverIconPath)) {
          dirNode.addElement("rollovericon").setText("getImage?image=icons/" + rolloverIconPath); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          dirNode.addElement("rollovericon").setText(rolloverIconPath); //$NON-NLS-1$
        }
      }
    }
    String displayType = info.getDisplayType();
    if ((displayType == null) || ("none".equalsIgnoreCase(displayType))) { //$NON-NLS-1$
      // this should be hidden from users
      dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      dirNode.addAttribute("visible", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      dirNode.addAttribute("displaytype", displayType); //$NON-NLS-1$
    }

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getSession());
    boolean subscribable = false;
    if (subscriptionRepository != null) {
      subscribable = subscriptionRepository.getContentByActionReference(solution + ISolutionRepository.SEPARATOR
          + path + ISolutionRepository.SEPARATOR + fileName ) != null;
    }
    dirNode.addElement("properties").setText("subscribable=" + Boolean.toString(subscribable)); //$NON-NLS-1$ //$NON-NLS-2$

  }
  // TODO sbarkdull, needs to be refactored, consider if
  // how it should work with/, etc. XmlHelper getLocalizedFile
  protected String getLocaleString(final String key, String baseName, final ISolutionFile baseFile) {
    File searchDir = ((FileSolutionFile) baseFile.retrieveParent()).getFile();
    try {
      boolean searching = true;
      while (searching) {
        // look to see if this exists
        URLClassLoader loader = new URLClassLoader(new URL[] { searchDir.toURL() }, null);
        String localeText = null;
        try {
          ResourceBundle rb = ResourceBundle.getBundle(baseName, getLocale(), loader);
          localeText = rb.getString(key.substring(1));
        } catch (Exception e) {
          // couldn't load bundle, move along
        }
        if (localeText != null) {
          return localeText;
        }
        // if we get to here, we couldn't use the resource bundle to find the string, so we will use this another approach
        // change the basename to messages (Messages.getInstance().properties) and go up a directory in our searching
        if (searching) {
          if (!baseName.equals("messages")) { //$NON-NLS-1$
            baseName = "messages"; //$NON-NLS-1$
          } else {
            if (searchDir.equals(rootFile)) {
              searching = false;
            } else {
              searchDir = searchDir.getParentFile();
            }
          }
        }
      }
      return null;
    } catch (Exception e) {
      error(
          Messages.getInstance().getErrorString("SolutionRepository.ERROR_0007_COULD_NOT_READ_PROPERTIES", baseFile.getFullPath()), e); //$NON-NLS-1$
    }
    return null;
  }

  protected void addIndexToRepository(final Document indexDoc, final File directoryFile, final Element directoryNode,
      final String path, final String solution) {
    // TODO see if there is a localized attribute file for the current
    // locale
    String dirName = getValue(indexDoc, "/index/name", directoryFile.getName().replace('_', ' ')); //$NON-NLS-1$
    String description = getValue(indexDoc, "/index/description", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String iconPath = getValue(indexDoc, "/index/icon", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String displayType = getValue(indexDoc, "/index/display-type", "icons"); //$NON-NLS-1$ //$NON-NLS-2$
    boolean visible = getValue(indexDoc, "/index/visible", "false").equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if (solution == null) {
      directoryNode.addAttribute("name", solution); //$NON-NLS-1$
    } else {
      directoryNode.addAttribute("name", XmlHelper.encode(directoryFile.getName())); //$NON-NLS-1$
    }
    directoryNode.addElement("title").setText(dirName); //$NON-NLS-1$
    // directoryNode.addElement( "path" ).setText( path ); //$NON-NLS-1$
    directoryNode.addAttribute("path", XmlHelper.encode(path)); //$NON-NLS-1$
    directoryNode.addElement("description").setText(description); //$NON-NLS-1$
    if (!StringUtil.isEmpty(iconPath)) {
      String rolloverIconPath = null;
      int rolloverIndex = iconPath.indexOf("|"); //$NON-NLS-1$
      if (rolloverIndex > -1) {
        rolloverIconPath = iconPath.substring(rolloverIndex + 1);
        iconPath = iconPath.substring(0, rolloverIndex);
      }
      if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(solution + File.separator + path), iconPath)) {
        directoryNode.addElement("icon").setText("getImage?image=icons/" + iconPath); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        directoryNode.addElement("icon").setText(iconPath); //$NON-NLS-1$
      }
      if (rolloverIconPath != null) {
        if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(solution + File.separator + path),
            rolloverIconPath)) {
          directoryNode.addElement("rollovericon").setText("getImage?image=icons/" + rolloverIconPath); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          directoryNode.addElement("rollovericon").setText(rolloverIconPath); //$NON-NLS-1$
        }
      }
    }
    directoryNode.addAttribute("visible", Boolean.toString(visible)); //$NON-NLS-1$
    directoryNode.addAttribute("displaytype", displayType); //$NON-NLS-1$
    directoryNode.addElement("solution").setText(solution); //$NON-NLS-1$
  }

  protected void addUrlToRepository(final File file, final Element parentNode, final String solutionPath, final int actionOperation) {
    // parse the .url file to get the contents
    ActionSequenceResource urlResource = new ActionSequenceResource(file.getName(), IActionSequenceResource.SOLUTION_FILE_RESOURCE,
        "text/url", solutionPath); //$NON-NLS-1$
    try {
      byte[] bytes = IOUtils.toByteArray(urlResource.getInputStream(actionOperation, LocaleHelper.getLocale()));
      String urlContent = new String(bytes, LocaleHelper.getSystemEncoding());
      StringTokenizer tokenizer = new StringTokenizer(urlContent, "\n"); //$NON-NLS-1$
      String url = null;
      String title = file.getName();
      String description = null;
      String iconPath = null;
      String target = null;
      while (tokenizer.hasMoreTokens()) {
        String line = tokenizer.nextToken();
        int pos = line.indexOf('=');
        if (pos > 0) {
          String name = line.substring(0, pos);
          String value = line.substring(pos + 1);
          if ((value != null) && (value.length() > 0) && (value.charAt(value.length() - 1) == '\r')) {
            value = value.substring(0, value.length() - 1);
          }
          if ("URL".equalsIgnoreCase(name)) { //$NON-NLS-1$
            url = value;
          }
          if ("name".equalsIgnoreCase(name)) { //$NON-NLS-1$
            title = value;
          }
          if ("description".equalsIgnoreCase(name)) { //$NON-NLS-1$
            description = value;
          }
          if ("icon".equalsIgnoreCase(name)) { //$NON-NLS-1$
            iconPath = value;
          }
          if ("target".equalsIgnoreCase(name)) { //$NON-NLS-1$
            target = value;
          }
        }
      }
      if (url != null) {
        // now create an entry for the database
        Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
        dirNode.addAttribute("type", FileInfo.FILE_TYPE_URL); //$NON-NLS-1$
        dirNode.addElement("filename").setText(file.getName()); //$NON-NLS-1$
        dirNode.addElement("title").setText(title); //$NON-NLS-1$
        if (target != null) {
          dirNode.addElement("target").setText(target); //$NON-NLS-1$
        }
        if (description != null) {
          dirNode.addElement("description").setText(description); //$NON-NLS-1$
        }
        if ((iconPath != null) && !iconPath.equals("")) { //$NON-NLS-1$
          String rolloverIconPath = null;
          int rolloverIndex = iconPath.indexOf("|"); //$NON-NLS-1$
          if (rolloverIndex > -1) {
            rolloverIconPath = iconPath.substring(rolloverIndex + 1);
            iconPath = iconPath.substring(0, rolloverIndex);
          }
          if (publishIcon(file.getParentFile().getAbsolutePath(), iconPath)) {
            dirNode.addElement("icon").setText("getImage?image=icons/" + iconPath); //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            dirNode.addElement("icon").setText(iconPath); //$NON-NLS-1$
          }
          if (rolloverIconPath != null) {
            if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(solutionPath), rolloverIconPath)) {
              dirNode.addElement("rollovericon").setText("getImage?image=icons/" + rolloverIconPath); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              dirNode.addElement("rollovericon").setText(rolloverIconPath); //$NON-NLS-1$
            }
          }
        }
        dirNode.addElement("url").setText(url); //$NON-NLS-1$
        dirNode.addAttribute("visible", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        dirNode.addAttribute("displaytype", FileInfo.FILE_DISPLAY_TYPE_URL); //$NON-NLS-1$
        localizeDoc(dirNode, new FileSolutionFile(file, ((FileSolutionFile) getRootFolder(actionOperation)).getFile()));
      }
    } catch (IOException e) {
    }
  }

  protected void addToRepository(final IActionSequence actionSequence, final Element parentNode, final File file) {
    Element dirNode = parentNode.addElement("file"); //$NON-NLS-1$
    dirNode.addAttribute("type", FileInfo.FILE_TYPE_ACTIVITY); //$NON-NLS-1$
    if ((actionSequence.getSequenceName() == null) || (actionSequence.getSolutionPath() == null)
        || (actionSequence.getSolutionName() == null)) {
      error(Messages.getInstance().getString("SolutionRepository.ERROR_0008_ACTION_SEQUENCE_NAME_INVALID")); //$NON-NLS-1$
      return;
    }
    dirNode.addElement("filename").setText(actionSequence.getSequenceName()); //$NON-NLS-1$
    dirNode.addElement("path").setText(actionSequence.getSolutionPath()); //$NON-NLS-1$
    dirNode.addElement("solution").setText(actionSequence.getSolutionName()); //$NON-NLS-1$
    String title = actionSequence.getTitle();
    if (title == null) {
      dirNode.addElement("title").setText(actionSequence.getSequenceName()); //$NON-NLS-1$
    } else {
      dirNode.addElement("title").setText(title); //$NON-NLS-1$
    }
    String description = actionSequence.getDescription();
    if (description == null) {
      dirNode.addElement("description"); //$NON-NLS-1$
    } else {
      dirNode.addElement("description").setText(description); //$NON-NLS-1$
    }
    String author = actionSequence.getAuthor();
    if (author == null) {
      dirNode.addElement("author"); //$NON-NLS-1$
    } else {
      dirNode.addElement("author").setText(author); //$NON-NLS-1$
    }
    String iconPath = actionSequence.getIcon();
    if ((iconPath != null) && !iconPath.equals("")) { //$NON-NLS-1$
      String rolloverIconPath = null;
      int rolloverIndex = iconPath.indexOf("|"); //$NON-NLS-1$
      if (rolloverIndex > -1) {
        rolloverIconPath = iconPath.substring(rolloverIndex + 1);
        iconPath = iconPath.substring(0, rolloverIndex);
      }
      if (publishIcon(file.getParentFile().getAbsolutePath(), iconPath)) {
        dirNode.addElement("icon").setText("getImage?image=icons/" + iconPath); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        dirNode.addElement("icon").setText(actionSequence.getIcon()); //$NON-NLS-1$
      }
      if (rolloverIconPath != null) {
        if (publishIcon(PentahoSystem.getApplicationContext().getSolutionPath(
            actionSequence.getSolutionName() + File.separator + actionSequence.getSolutionPath()), rolloverIconPath)) {
          dirNode.addElement("rollovericon").setText("getImage?image=icons/" + rolloverIconPath); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          dirNode.addElement("rollovericon").setText(rolloverIconPath); //$NON-NLS-1$
        }
      }
    }
    String displayType = actionSequence.getResultType();
    if ((displayType == null) || ("none".equalsIgnoreCase(displayType))) { //$NON-NLS-1$
      // this should be hidden from users
      dirNode.addAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      dirNode.addAttribute("visible", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      dirNode.addAttribute("displaytype", displayType); //$NON-NLS-1$
    }

    ISubscriptionRepository subscriptionRepository = PentahoSystem.get(ISubscriptionRepository.class, getSession());
    boolean subscribable = false;
    if (subscriptionRepository != null) {
      subscribable = subscriptionRepository.getContentByActionReference(actionSequence.getSolutionName() + '/'
          + actionSequence.getSolutionPath() + '/' + actionSequence.getSequenceName()) != null;
    }
    dirNode.addElement("properties").setText("subscribable=" + Boolean.toString(subscribable)); //$NON-NLS-1$ //$NON-NLS-2$

  }

  protected boolean publishIcon(final String dirPath, final String iconPath) {
    File iconSource = new File(dirPath + File.separator + iconPath);
    if (iconSource.exists()) {
      File tmpDir = getFile("system/tmp/icons", true); //$NON-NLS-1$
      tmpDir.mkdirs();
      File iconDestintation = new File(tmpDir.getAbsoluteFile() + File.separator + iconPath);
      if (iconDestintation.exists()) {
        iconDestintation.delete();
      }
      try {
        InputStream stream = new BufferedInputStream(new FileInputStream(iconSource));
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(iconDestintation));
        try {
          IOUtils.copy(stream, outputStream);
        } finally {
          outputStream.close();
          stream.close();
        }
      } catch (FileNotFoundException e) {
        // this one is not very likey
        error(e.getLocalizedMessage());
      } catch (IOException e) {
        error(e.getLocalizedMessage());
      }
      return true;
    } else {
      return false;
    }
  }

  public Document getSolutions(final String solutionName, final String pathName, final int actionOperation,
      final boolean visibleOnly) {
    return getSolutions(actionOperation);
  }

  @Override
  public Document getSolutions(final int actionOperation) {
    Object cachedRepo = getRepositoryObjectFromCache(getRepositoryKey());
    if (cachedRepo == null) {
      reloadSolutionRepository(getSession(), loggingLevel);
      cachedRepo = getRepositoryObjectFromCache(getRepositoryKey());
    }
    return (Document) cachedRepo;
  }

  private Document getActionSequences(final String solution, final String path, final boolean subDirectories,
      final boolean visibleOnly, final int actionOperation) {
    List nodes;
    if (solution == null) {
      nodes = getSolutionNames(solution, path, actionOperation, visibleOnly);
    } else {
      nodes = getFileListIterator(solution, path, subDirectories, visibleOnly);
    }
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement("files"); //$NON-NLS-1$
    Element pathNames = root.addElement("location"); //$NON-NLS-1$

    pathNames.setText(getPathNames(solution, path));

    Iterator nodeIterator = nodes.iterator();
    while (nodeIterator.hasNext()) {
      Node node = (Node) nodeIterator.next();
      root.add((Node) node.clone());
    }
    return document;
  }

  private String getPathNames(final String solutionId, final String path) {
    Document repository = (Document) getRepositoryObjectFromCache(getRepositoryKey());
    if (repository == null) {
      reloadSolutionRepository(getSession(), loggingLevel);
      repository = (Document) getRepositoryObjectFromCache(getRepositoryKey());
    }
    if (solutionId == null) {
      return ""; //$NON-NLS-1$
    }

    // TODO sbarkdull, extract to a method, this same code is repeated in this class
    String xPath = "/repository/file[@type=\"" + FileInfo.FILE_TYPE_FOLDER + "\"][@name=\"" + XmlHelper.encode(solutionId) + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if (path != null) {
      String folders[] = path.split("/"); //$NON-NLS-1$
      if (folders != null) {
        for (String element : folders) {
          xPath += "/file[@type=\"" + FileInfo.FILE_TYPE_FOLDER + "\"][@name=\"" + XmlHelper.encode(element) + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          xPath += "[@visible=\"true\"]"; //$NON-NLS-1$
        }
      }
    }

    StringBuffer sb = new StringBuffer();
    List list = repository.selectNodes(xPath);
    if ((list != null) && (list.size() > 0)) {
      // grab the first one
      Element node = (Element) list.get(0);
      // walk up the ancestors
      boolean done = false;
      while ((node != null) && !done) {
        Node titleNode = node.selectSingleNode("title"); //$NON-NLS-1$
        if (titleNode != null) {
          String name = titleNode.getText();
          sb.insert(0, name + "/"); //$NON-NLS-1$
        } else {
          // if we don't have a title node then there is nothing more we can do to construct the path
          done = true;
        }
        node = node.getParent();
      }
    }
    return sb.toString();

  }

  private List getFileListIterator(final String solutionId, final String path, final boolean subDirectories,
      final boolean visibleOnly) {
    Document repository = (Document) getRepositoryObjectFromCache(getRepositoryKey());
    if (repository == null) {
      reloadSolutionRepository(getSession(), loggingLevel);
      repository = (Document) getRepositoryObjectFromCache(getRepositoryKey());
    }
    String xPath;
    if (solutionId == null) {
      xPath = "/repository/file[@type=\"" + FileInfo.FILE_TYPE_FOLDER + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      xPath = "/repository/file[@type=\"" + FileInfo.FILE_TYPE_FOLDER + "\"][@name=\"" + XmlHelper.encode(solutionId) + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    if (path != null) {
      String folders[] = path.split("/"); //$NON-NLS-1$
      if (folders != null) {
        for (String element : folders) {
          xPath += "/file[@type=\"" + FileInfo.FILE_TYPE_FOLDER + "\"][@name=\"" + XmlHelper.encode(element) + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          if (visibleOnly) {
            xPath += "[@visible=\"true\"]"; //$NON-NLS-1$
          }
        }
      }
    }
    if (subDirectories) {
      xPath = "descendant-or-self::" + xPath; //$NON-NLS-1$
    }
    if (SolutionRepositoryBase.debug) {
      debug(Messages.getInstance().getString("SolutionRepository.DEBUG_FILE_LIST_XPATH", xPath)); //$NON-NLS-1$
    }
    return repository.selectNodes(xPath);
  }
  
  public Document getSolutionStructure(final int actionOperation) {
    Document document = DocumentHelper.createDocument();
    File rootDir = getFile(SolutionRepositoryBase.EMPTY_STR, false);
    Element root = document.addElement(SolutionRepositoryBase.ROOT_NODE_NAME).addAttribute(
        SolutionRepositoryBase.LOCATION_ATTR_NAME, rootDir.getAbsolutePath());
    processSolutionTree(root, rootDir);
    return document;
  }

  public Document getSolutionTree(final int actionOperation, final ISolutionFilter filter) {
		// create a document to return
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(SolutionReposHelper.TREE_NODE_NAME);
		// process the tree using the filter provided as a parameter
    SolutionReposHelper.processSolutionTree(root, new FileSolutionFile(rootFile, rootFile), filter, actionOperation);
    return document;
  }

  public Document getSolutionTree(final int actionOperation) {
    return getSolutionTree(actionOperation, defaultSolutionFilter);
  }

  private void processSolutionTree(final Element parentNode, final File targetFile) {
    if (targetFile.isDirectory()) {
      if (!SolutionReposHelper.ignoreDirectory(targetFile.getName())) {
        Element childNode = parentNode.addElement(SolutionReposHelper.ENTRY_NODE_NAME).addAttribute(
            SolutionReposHelper.TYPE_ATTR_NAME, SolutionReposHelper.DIRECTORY_ATTR).addAttribute(
            SolutionReposHelper.NAME_ATTR_NAME, targetFile.getName());
        File files[] = targetFile.listFiles();
        for (File file : files) {
          processSolutionTree(childNode, file);
        }
      }
    } else {
      if (!targetFile.isHidden() && !SolutionReposHelper.ignoreFile(targetFile.getName())) {
        parentNode.addElement(SolutionReposHelper.ENTRY_NODE_NAME).addAttribute(SolutionReposHelper.TYPE_ATTR_NAME,
            SolutionReposHelper.FILE_ATTR).addAttribute(SolutionReposHelper.NAME_ATTR_NAME, targetFile.getName());
      }
    }
  }

  // -----------------------------------------------------------------------
  // Methods from PentahoSystem
  public boolean resourceExists(final String solutionPath, final int actionOperation) {
    String filePath = PentahoSystem.getApplicationContext().getSolutionPath(solutionPath);
    File file = new File(filePath);
    return file.exists();
  }

  public String[] getAllActionSequences(final int actionOperation) {
    File rootDir = getFile("", false); //$NON-NLS-1$
    List files = new ArrayList();
    files = getAllActionSequences(rootDir, files);
    String[] value = new String[files.size()];
    Iterator iter = files.iterator();
    int i = 0;
    int solutionPathOffset = PentahoSystem.getApplicationContext().getSolutionPath("").length(); //$NON-NLS-1$
    while (iter.hasNext()) {
      File file = (File) iter.next();
      String filePath = file.getAbsolutePath();
      filePath = filePath.substring(solutionPathOffset);
      filePath = filePath.replace('\\', '/');
      value[i++] = filePath;
    }
    return value;
  }

  private List getAllActionSequences(final File rootDir, final List files) {
    if (!rootDir.isDirectory()) {
      return files;
    }
    File[] fileArray = rootDir.listFiles(new xActionFileFilter());
    for (File element : fileArray) {
      files.add(element);
    }
    fileArray = rootDir.listFiles();
    for (File element : fileArray) {
      if (element.isDirectory()) {
        getAllActionSequences(element, files);
      }
    }
    return files;
  }

  class xActionFileFilter implements FilenameFilter {
    public boolean accept(final File dir, final String name) {
      int seperatorIndex = name.lastIndexOf('.');
      if (seperatorIndex != -1) {
        return name.substring(name.lastIndexOf('.')).equalsIgnoreCase(".xaction"); //$NON-NLS-1$
      }
      return false;
    }
  }

  @Override
  public Document getNavigationUIDocument(final String solution, final String path, final int actionOperation) {
    Document document = null;
    if (StringUtil.isEmpty(solution)) {
      document = this.getSolutions(ISolutionRepository.ACTION_EXECUTE);
    } else {
      document = getActionSequences(solution, path, false, true, ISolutionRepository.ACTION_EXECUTE);
    }
    return document;
  }

  public String getRepositoryName() {
    return "";//$NON-NLS-1$
  }

  public boolean supportsAccessControls() {
    return false;
  }

  private String getRepositoryKey() {
    return "repository" + getLocale().toString();//$NON-NLS-1$
  }

  /**
   * No access control is enforced in this implementation.
   * @see supportsAccessControls
   */
  public boolean hasAccess(final ISolutionFile file, final int actionOperation) {
    return true;
  }

  public int publish(final String baseUrl, final String path, final String fileName, final byte[] data,
      final boolean overwrite) throws PentahoAccessControlException {
    return addSolutionFile(baseUrl, path, fileName, data, overwrite);
  }

  public int publish(final String baseUrl, final String path, final String fileName, final File f,
      final boolean overwrite) throws PentahoAccessControlException {
    return addSolutionFile(baseUrl, path, fileName, f, overwrite);
  }

  public void share(final ISolutionFile file, final List<IPermissionRecipient> shareRecipient) {
  }

  public void addPermission(final ISolutionFile file, final IPermissionRecipient recipient,
      final IPermissionMask permission) {
  }

  public void setPermissions(final ISolutionFile file, final Map<IPermissionRecipient, IPermissionMask> acl)
      throws PentahoAccessControlException {
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(final ISolutionFile file) {
    return Collections.emptyMap();
  }

  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions(final ISolutionFile file) {
    return Collections.emptyMap();
  }
  
  public boolean synchronizeSolutionWithSolutionSource(final IPentahoSession pSession) {
    throw new UnsupportedOperationException("Synchronization is not supported by this implementor"); //$NON-NLS-1$
  }

  private class DefaultSolutionFilter implements ISolutionFilter {
    public boolean keepFile(final ISolutionFile solutionFile, final int actionOperation) {
      // No filtering of solution files/folders in the file-based repository
      // yet.
      return true;
    }
  }
}
