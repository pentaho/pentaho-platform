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
 */
package org.pentaho.platform.repository.solution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IFileFilter;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISessionContainer;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.PentahoMessenger;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository.solution.filebased.FileSolutionFile;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

public abstract class SolutionRepositoryBase extends PentahoMessenger implements ISolutionRepository, IPentahoInitializer, ISessionContainer {
  private static final long serialVersionUID = 6367444546398801343L;

  public SolutionRepositoryBase() {
    super();
  }

  protected static final Log logger = LogFactory.getLog(SolutionRepositoryBase.class);

  protected static final int BROWSE_DEPTH = 2;

  protected static final String ROOT_NODE_NAME = "repository"; //$NON-NLS-1$

  protected static final String LOCATION_ATTR_NAME = "location"; //$NON-NLS-1$

  protected static final String EMPTY_STR = ""; //$NON-NLS-1$

  protected static final boolean debug = PentahoSystem.debug;

  protected static final String ENTRY_NODE_NAME = "entry"; //$NON-NLS-1$

  protected static final String TYPE_ATTR_NAME = "type"; //$NON-NLS-1$

  protected static final String NAME_ATTR_NAME = "name"; //$NON-NLS-1$

  protected static final String DIRECTORY_ATTR = "directory"; //$NON-NLS-1$

  protected static final String FILE_ATTR = "file"; //$NON-NLS-1$

  protected static final long PUBLISH_TIMEOUT = 1500; // 1.5 seconds

  protected static final Map<String, Properties> propertyMap = new HashMap<String, Properties>();

  protected static final String LOG_NAME = "SOLUTION-REPOSITORY"; //$NON-NLS-1$

  protected static final String PROPERTIES_SUFFIX = ".properties"; //$NON-NLS-1$

  protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
  /*
   * matches 0 or 1 "/" followed by any non-"/" followed by either an end of string or (a "/" followed by 0 or more of anything).
   */
  private static final String RE_SYSTEM_PATH = "^[/\\\\]?system($|[/\\\\].*$)"; //$NON-NLS-1$

  private static final String RE_SYSTEM_TMP_PATH = "^[/\\\\]?system/tmp($|[/\\\\].*$)"; //$NON-NLS-1$

  private static final Pattern SYSTEM_PATH_PATTERN = Pattern.compile(SolutionRepositoryBase.RE_SYSTEM_PATH);

  private static final Pattern SYSTEM_TMP_PATH_PATTERN = Pattern.compile(SolutionRepositoryBase.RE_SYSTEM_TMP_PATH);

  protected ThreadLocal session = new ThreadLocal();

  protected String rootPath;

  protected File rootFile;

  protected String rootCanonicalName; 

  @Override
  public Log getLogger() {
    return SolutionRepositoryBase.logger;
  }

  protected Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  protected void init() {
    rootFile = getFile("", false); //$NON-NLS-1$
    rootPath = rootFile.getAbsolutePath() + File.separator;
    setLogId(SolutionRepositoryBase.LOG_NAME + ": "); //$NON-NLS-1$
    try {
      rootCanonicalName = rootFile.getCanonicalPath();
    } catch (IOException ex) {
      // If we get an IO error here, we have really bad problems.
      ex.printStackTrace();
    }
  }

  /**
   * NOTE regarding old code: the cacheManager cannot be cached, because it is possible that the SolutionRepository implementation has a scope that is longer
   * lived than the cacheManager. When the cacheManager goes out of scope, this class would have maintained a stale reference to a now obsolete cacheManager.
   * (sbarkdull)
   * 
   */
  public void init(final IPentahoSession pentahoSession) {
    setSession(pentahoSession);
    init();
  }

  protected IPentahoSession getSession() {
    Object threadSession = session.get();
    return (IPentahoSession) threadSession;
  }

  public void setSession(final IPentahoSession inSession) {
    session.set(inSession);
  }

  // TODO sbarkdull, this code is very similar to XmlHelper.getLocalizedFile(). they
  // likely should be resolved into a single method in an appropriate utility class/package.
  protected ISolutionFile getLocalizedFile(final ISolutionFile resourceFile, final int actionOperation) {
    String fileName = resourceFile.getFileName();
    int idx = fileName.lastIndexOf('.');
    String baseName = idx == -1 ? fileName : fileName.substring(0, idx); // These two lines fix an index out of bounds
    String extension = idx == -1 ? "" : fileName.substring(idx); // Exception that occurs when a filename has no extension //$NON-NLS-1$
    String directory = resourceFile.getSolutionPath();
    if (directory.lastIndexOf(fileName) != -1) {
      directory = new StringBuffer(directory).delete(directory.lastIndexOf(fileName), directory.length()).toString();
    }
    if (!directory.endsWith(ISolutionRepository.SEPARATOR + "")) { //$NON-NLS-1$
      directory += ISolutionRepository.SEPARATOR;
    }
    String language = getLocale().getLanguage();
    String country = getLocale().getCountry();
    String variant = getLocale().getVariant();
    ISolutionFile localeFile = null;
    if (!variant.equals("")) { //$NON-NLS-1$
      localeFile = getFileByPath(directory + baseName + "_" + language + "_" + country + "_" + variant + extension, actionOperation); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    if (localeFile == null) {
      localeFile = getFileByPath(directory + baseName + "_" + language + "_" + country + extension, actionOperation); //$NON-NLS-1$//$NON-NLS-2$
    }
    if (localeFile == null) {
      localeFile = getFileByPath(directory + baseName + "_" + language + extension, actionOperation); //$NON-NLS-1$
    }
    if (localeFile == null) {
      localeFile = getFileByPath(directory + baseName + extension, actionOperation);
    }
    if (localeFile != null) {
      return localeFile;
    } else {
      return resourceFile;
    }
  }

  protected ISolutionFile getFileByPath(final String path, final int actionOperation) {
    File file = new File(PentahoSystem.getApplicationContext().getSolutionPath(path));
    if ((isPathedUnderSolutionRoot(file)) && (file.exists())) {
      return new FileSolutionFile(file, rootFile);
    } else {
      return null;
    }
  }

  protected boolean isPathedUnderSolutionRoot(String fName) {
    return isPathedUnderSolutionRoot(new File(fName));
  }
  
  protected boolean isPathedUnderSolutionRoot(File aFile) {
    String fc = null;
    try {
      fc= aFile.getCanonicalPath();
    } catch (IOException logitOnly) {
      debug("", logitOnly); //$NON-NLS-1$
      return false;
    }
    return ( fc.startsWith(rootCanonicalName) );
  }
  
  protected File getFile(final String path, boolean create) {
    File f = new File(PentahoSystem.getApplicationContext().getSolutionPath(path));
    
    // Because the startup path calls this method to
    // set the rootFile, check if the rootFile is null
    if ((rootFile != null) && !(isPathedUnderSolutionRoot(f))) {
      return null;
    }
    
    if (!f.exists() && !create) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0001_FILE_DOES_NOT_EXIST", path)); //$NON-NLS-1$
      return null;
    }
    if (!f.exists()) {
      f.mkdirs();
    }
    // TODO: caching
    if (SolutionRepositoryBase.debug) {
      debug(Messages.getInstance().getErrorString("SolutionRepository.DEBUG_FILE_PATH", f.getAbsolutePath())); //$NON-NLS-1$
    }
    return f;
  }

  protected static boolean isSystemPath(final String path) {

    Matcher m = SolutionRepositoryBase.SYSTEM_PATH_PATTERN.matcher(path.toLowerCase());
    return m.matches();
  }

  /**
   * Returns true if the path is the tmp directory in the system solution.
   */
  protected static boolean isSystemTmpPath(final String path) {
    Matcher m = SolutionRepositoryBase.SYSTEM_TMP_PATH_PATTERN.matcher(path.toLowerCase());
    return m.matches();
  }

  public void localizeDoc(final Node document, final ISolutionFile file) {
    String fileName = file.getFileName();
    int dotIndex = fileName.indexOf('.');
    String baseName = fileName.substring(0, dotIndex);
    // TODO read in nodes from the locale file and use them to override the
    // ones in the main document
    try {
      List nodes = document.selectNodes("descendant::*"); //$NON-NLS-1$
      Iterator nodeIterator = nodes.iterator();
      while (nodeIterator.hasNext()) {
        Node node = (Node) nodeIterator.next();
        String name = node.getText();
        if (name.startsWith("%") && !node.getPath().endsWith("/text()")) { //$NON-NLS-1$ //$NON-NLS-2$
          try {
            String localeText = getLocaleString(name, baseName, file, true);
            if (localeText != null) {
              node.setText(localeText);
            }
          } catch (Exception e) {
            warn(Messages.getInstance().getString("SolutionRepository.WARN_MISSING_RESOURCE_PROPERTY", name.substring(1), baseName, getLocale().toString())); //$NON-NLS-1$
          }
        }
      }
    } catch (Exception e) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0007_COULD_NOT_READ_PROPERTIES", file.getFullPath()), e); //$NON-NLS-1$
    }
  }

  protected String getLocaleString(final String key, final String baseName, final String baseFilePath, final int actionOperation) {
    ISolutionFile file = getFileByPath(baseFilePath, actionOperation);
    return getLocaleString(key, baseName, file, true);
  }

  // TODO sbarkdull, needs to be refactored, consider if
  // how it should work with/, etc. XmlHelper getLocalizedFile
  protected String getLocaleString(final String key, String baseName, final ISolutionFile baseFile, boolean marchUpParents) {

    ISolutionFile searchDir = baseFile.retrieveParent();
    if (baseFile.isDirectory()) {
      searchDir = baseFile;
    }
    try {
      boolean searching = true;
      while (searching) {

        ISolutionFile[] propertyFiles = searchDir.listFiles(new IFileFilter() {
          public boolean accept(ISolutionFile file) {
            return file.getFileName().toLowerCase().endsWith(PROPERTIES_SUFFIX);
          }
        });
        ISolutionFile blcv = null;
        ISolutionFile blc = null;
        ISolutionFile bl = null;
        ISolutionFile b = null;
        for (ISolutionFile element : propertyFiles) {
          if (element.getFileName().equalsIgnoreCase(
              baseName + '_' + getLocale().getLanguage() + '_' + getLocale().getCountry() + '_' + getLocale().getVariant() + PROPERTIES_SUFFIX)) {
            blcv = element;
          }
          if (element.getFileName().equalsIgnoreCase(baseName + '_' + getLocale().getLanguage() + '_' + getLocale().getCountry() + PROPERTIES_SUFFIX)) {
            blc = element;
          }
          if (element.getFileName().equalsIgnoreCase(baseName + '_' + getLocale().getLanguage() + PROPERTIES_SUFFIX)) {
            bl = element;
          }
          if (element.getFileName().equalsIgnoreCase(baseName + PROPERTIES_SUFFIX)) {
            b = element;
          }
        }

        String localeText = getLocaleText(key, blcv);
        if (localeText == null) {
          localeText = getLocaleText(key, blc);
          if (localeText == null) {
            localeText = getLocaleText(key, bl);
            if (localeText == null) {
              localeText = getLocaleText(key, b);
            }
          }
        }
        if (localeText != null) {
          return localeText;
        }
        if (searching && marchUpParents) {
          if (!baseName.equals("messages")) { //$NON-NLS-1$
            baseName = "messages"; //$NON-NLS-1$
          } else {
            if (searchDir.isRoot()) {
              searching = false;
            } else {
              searchDir = searchDir.retrieveParent();
            }
          }
        } else if (!marchUpParents) {
          searching = false;
        }
      }
      return null;
    } catch (Exception e) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0007_COULD_NOT_READ_PROPERTIES", baseFile.getFullPath()), e); //$NON-NLS-1$
    }
    return null;
  }

  protected String getLocaleText(final String key, final ISolutionFile file) throws IOException {
    if (file != null) {
      Properties p = (Properties) SolutionRepositoryBase.propertyMap.get(file.getFullPath());
      if (p == null) {
        p = new Properties();
        p.load(new ByteArrayInputStream(file.getData()));
        SolutionRepositoryBase.propertyMap.put(file.getFullPath(), p);
      }
      String localeText = p.getProperty(key.substring(1));
      if (localeText == null) {
        localeText = p.getProperty(key);
      }
      if (localeText != null) {
        return localeText;
      }
    }
    return null;
  }

  protected IFileInfo getFileInfo(final String solution, final String path, final String fileName,
      final String extension, IPluginManager pluginManager, final int actionOperation) {
    IFileInfo fileInfo = null;
    String fullPath = solution + ISolutionRepository.SEPARATOR
        + ((StringUtil.isEmpty(path)) ? "" : path + ISolutionRepository.SEPARATOR) + fileName; //$NON-NLS-1$
    try {
      ISolutionFile file = getFileByPath(fullPath, actionOperation);
      IActionSequenceResource resource = new ActionSequenceResource("", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "", //$NON-NLS-1$ //$NON-NLS-2$
          fullPath);
      InputStream in = resource.getInputStream(actionOperation, LocaleHelper.getLocale());
      fileInfo = pluginManager.getFileInfo(extension, getSession(), file, in);
    } catch (Exception e) {
      error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0021_FILE_NOT_ADDED", fullPath), e); //$NON-NLS-1$
    }
    return fileInfo;
  }
  
  public Document getSolutions(final int actionOperation) {
    return getSolutions(null, null, actionOperation, false);
  }

  protected List getSolutionNames(final String solutionName, final String pathName, final int actionOperation, final boolean visibleOnly) {
    Document solns = getSolutions(solutionName, pathName, actionOperation, visibleOnly);
    String xPath = "/repository/file"; //$NON-NLS-1$
    return solns.selectNodes(xPath);
  }

  /**
   * Clears cached data for ALL users
   */
  public void resetRepository() {

    ICacheManager cacheManager = PentahoSystem.getCacheManager(getSession());

    if (cacheManager != null) {
      cacheManager.killSessionCaches();
      cacheManager.clearRegionCache(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION);
    }
    // clear propertymap
    SolutionRepositoryBase.propertyMap.clear();
  }

  public ISolutionFile getRootFolder(final int actionOperation) {
    return new FileSolutionFile(rootFile, rootFile);
  }


  public Document getNavigationUIDocument(final String solution, final String path, final int actionOperation) {
    Document document = this.getSolutions(solution, path, ISolutionRepository.ACTION_EXECUTE, false);
    return document;
  }

  /**
   * @return int possible values: ISolutionRepository.FILE_ADD_SUCCESSFUL ISolutionRepository.FILE_EXISTS ISolutionRepository.FILE_ADD_FAILED
   * 
   * TODO mlowery Why can't this delegate to the other addSolutionFile?
   */
  public int addSolutionFile(final String baseUrl, String path, final String fileName, final File f, boolean overwrite) {
    if (!path.endsWith("/") && !path.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
      path += File.separator;
    }
    File fNew = new File(baseUrl + path + fileName);
    int status = ISolutionRepository.FILE_ADD_SUCCESSFUL;
    if (fNew.exists() && !overwrite) {
      status = ISolutionRepository.FILE_EXISTS;
    } else {
      FileChannel in = null, out = null;
      try {
        in = new FileInputStream(f).getChannel();
        out = new FileOutputStream(fNew).getChannel();
        out.transferFrom(in, 0, in.size());
        resetRepository();
      } catch (Exception e) {
        SolutionRepositoryBase.logger.error(e.toString());
        status = ISolutionRepository.FILE_ADD_FAILED;
      } finally {
        try {
          if (in != null) {
            in.close();
          }
          if (out != null) {
            out.close();
          }
        } catch (Exception e) {
          // TODO, we should probably log the error, and return a failure status
        }
      }
    }
    return status;
  }

  public int addSolutionFile(final String baseUrl, String path, final String fileName, final byte[] data, boolean overwrite) {
    // baseUrl = baseUrl + path;
    if (!path.endsWith("/") && !path.endsWith("\\")) { //$NON-NLS-1$//$NON-NLS-2$
      path += File.separator;
    }

    // do not allow publishing to root path
    if (path.equals(File.separator)) {
      logger.error(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0023_INVALID_PUBLISH_LOCATION_ROOT")); //$NON-NLS-1$
      return ISolutionRepository.FILE_ADD_FAILED;
    }

    File fNew = new File(baseUrl + path + fileName);
    int status = ISolutionRepository.FILE_ADD_SUCCESSFUL;
    if (fNew.exists() && !overwrite) {
      status = ISolutionRepository.FILE_EXISTS;
    } else {
      FileOutputStream fNewOut = null;
      try {
        if (!fNew.exists()) {
          fNew.getParentFile().mkdirs();
          fNew.createNewFile();
        }
        fNewOut = new FileOutputStream(fNew);
        fNewOut.write(data);
        resetRepository();
      } catch (Exception e) {
        status = ISolutionRepository.FILE_ADD_FAILED;
        SolutionRepositoryBase.logger.error(e.toString());
      } finally {
        try {
          fNewOut.close();
        } catch (Exception e) {
          // TODO, we should probably log the error, and return a failure status
        }
      }
    }
    return status;
  }

  public boolean removeSolutionFile(String solutionPath) {
    if (isSystemPath(solutionPath)) {
      return false;
    }
    solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(solutionPath);
    File deleteFile = new File(solutionPath);
    try {
      if (deleteFile.exists()) {
        if (!deleteFile.isDirectory()) {
          boolean deleted = deleteFile.delete();
          if (deleted) {
            AuditHelper
                .audit(
                    "", getSession().getName(), "", getClass().toString(), "", MessageTypes.UNKNOWN_ENTRY, Messages.getInstance().getString("SOLREPO.AUDIT_DEL_FILE", solutionPath), "", 0.0f, null); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
          }
          return deleted;
        } else { // recursively delete all the files under this directory
          // and then delete the directory
          return deleteFolder(deleteFile);
        }
      }
    } finally {
      resetRepository();
    }
    return false;
  }

  /**
   * this is the file based removeSolutionFile, used by subclasses
   */
  public boolean removeSolutionFile(final String solution, final String path, final String fileName) {
    return removeSolutionFile(solution + path + fileName);
  }

  private boolean deleteFolder(final File dir) {
    if (!dir.isDirectory()) {
      SolutionRepositoryBase.logger.warn(Messages.getInstance().getString("SolutionRepository.USER_DELETE_FOLDER_WARNING")); //$NON-NLS-1$
      return false;
    }
    String[] files = dir.list();
    for (String element : files) {
      String filePath = dir.getAbsolutePath() + File.separator + element;
      File file = new File(filePath);
      if (file.isDirectory()) {
        if (deleteFolder(file)) {
          AuditHelper
              .audit(
                  "", getSession().getName(), "", getClass().toString(), "", MessageTypes.UNKNOWN_ENTRY, Messages.getInstance().getString("SOLREPO.AUDIT_DEL_FOLDER", filePath), "", 0.0f, null); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
        }
      } else {
        if (file.delete()) {
          AuditHelper
              .audit(
                  "", getSession().getName(), "", getClass().toString(), "", MessageTypes.UNKNOWN_ENTRY, Messages.getInstance().getString("SOLREPO.AUDIT_DEL_FILE", filePath), "", 0.0f, null); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
        }
      }
    }
    String filePath = dir.getAbsolutePath();
    boolean deleted = dir.delete();
    if (deleted) {
      AuditHelper
          .audit(
              "", getSession().getName(), "", getClass().toString(), "", MessageTypes.UNKNOWN_ENTRY, Messages.getInstance().getString("SOLREPO.AUDIT_DEL_FOLDER", filePath), "", 0.0f, null); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
    }
    return deleted;
  }

  public Document getFullSolutionTree(final int actionOperation, final ISolutionFilter filter) {
    return getFullSolutionTree(actionOperation, filter, null);
  }
  
  public Document getFullSolutionTree(final int actionOperation, final ISolutionFilter filter, ISolutionFile startingFile) {
    startingFile = startingFile == null ? new FileSolutionFile(rootFile, rootFile) : startingFile;
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(SolutionReposHelper.TREE_NODE_NAME);

    SolutionReposHelper.processSolutionTree(root, startingFile, filter, SolutionReposHelper.ADD_NOTHING_CONTRIBUTOR, actionOperation);

    return document;  
  }

  protected boolean isCachingAvailable() {
    IPentahoSession pentahoSession = getSession();
    ICacheManager cacheManager = PentahoSystem.getCacheManager(pentahoSession);
    return (cacheManager != null) && cacheManager.cacheEnabled();
  }

  /**
   * Caches the repository object
   * 
   * @param key -
   *          String value of the key
   * @param value -
   *          Object referred to by key
   * 
   * @return null if unable to catch otherwise returns the object that was cached
   */
  protected Object putRepositoryObjectInCache(final String key, final Object value) {
    if (isCachingAvailable()) {
      ICacheManager cacheManager = PentahoSystem.getCacheManager(getSession());
      cacheManager.putInSessionCache(getSession(), key, value);
      return value;
    }

    return null;
  }

  /**
   * Gets the object from the session cache defined by the parameter key
   * 
   * @param key -
   *          String value of the key to lookup
   * 
   * @return Object that is referred to by the key. Null if not in the cache or if caching is unavailable
   */
  protected Object getRepositoryObjectFromCache(final String key) {
    if (isCachingAvailable()) {
      ICacheManager cacheManager = PentahoSystem.getCacheManager(getSession());
      return cacheManager.getFromSessionCache(getSession(), key);
    }

    return null;
  }

  public ISolutionFile getSolutionFile(final String path, final int actionOperation) {
    ISolutionFile solutionFile = null;
    if (!SolutionRepositoryBase.isSystemPath(path)) {
      // Not checking here because the underlying
      // has the check.
      solutionFile = getFileByPath(path, actionOperation);
    } else {
      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(path);
      if ((isPathedUnderSolutionRoot(solutionPath))) {
        solutionFile = new FileSolutionFile(new File(solutionPath), rootFile);
      }
    }
    return solutionFile;
  }
  
  public ISolutionFile getSolutionFile(final IActionSequenceResource actionResource, final int actionOperation) {
    ISolutionFile solutionFile = null;
    int resourceSource = actionResource.getSourceType();
    String realPath = null;
    if (resourceSource == IActionSequenceResource.SOLUTION_FILE_RESOURCE) {
      realPath = actionResource.getAddress();
      solutionFile = getSolutionFile(realPath, actionOperation);
    } else if (resourceSource == IActionSequenceResource.FILE_RESOURCE) {
      realPath = actionResource.getAddress();
      if ((isPathedUnderSolutionRoot(realPath))) {
      solutionFile = new FileSolutionFile(new File(realPath), rootFile);
    }
    }
    if ((solutionFile == null) || !solutionFile.exists()) {
      solutionFile = null;
    }
    return solutionFile;
  }

  public ISolutionFile createFolder(final File newFolder) throws IOException {
      if (!(isPathedUnderSolutionRoot(newFolder))) {
        throw new IOException(Messages.getInstance().getErrorString("SolutionRepository.ERROR_0021_FILE_NOT_ADDED", newFolder.getName())); //$NON-NLS-1$
      }
    newFolder.mkdirs();
    FileSolutionFile fsf = new FileSolutionFile(newFolder, rootFile);
    return fsf;
  }
  
  public String getLocalizedFileProperty(final ISolutionFile resourceFile, final String key, final int actionOperation) {
    if (!hasAccess(resourceFile, actionOperation)) {
      return null;
    }
    // look for .properties file for this file
    String fileName = resourceFile.getFileName();
    int idx = fileName.lastIndexOf('.');
    String baseName = idx == -1 ? fileName : fileName.substring(0, idx);
    String localizedName;
    if (resourceFile.isDirectory()) {
      // directory info is stored in index.properties files
      // name.startsWith("%")
      // look in index.xml for attributes first, if they start with % then do getLocaleString
      ISolutionFile possibleIndexFiles[] = resourceFile.listFiles(new IFileFilter() {

        public boolean accept(ISolutionFile file) {
          if (file.getFileName().equals("index.xml")) { //$NON-NLS-1$
            return true;
          }
          return false;
        }

      });
      String value = null;
      if (possibleIndexFiles.length > 0) {
        // figure out what the XML string says the encoding is
        String xml = new String(possibleIndexFiles[0].getData());
        Document document = null;
        try {
          document = XmlDom4JHelper.getDocFromString(xml, new SolutionURIResolver());
          value = document.selectSingleNode("/index/" + key).getText(); //$NON-NLS-1$
        } catch (Throwable t) {
          t.printStackTrace();
          value = null;
        }
      }
      if ((value == null) || "".equals(value)) { //$NON-NLS-1$
        localizedName = getLocaleString(key, "index", resourceFile, false); //$NON-NLS-1$
      } else if (value.startsWith("%")) { //$NON-NLS-1$
        localizedName = getLocaleString(value.substring(1), "index", resourceFile, false); //$NON-NLS-1$
      } else {
        localizedName = value;
      }
    } else {
      // if the file is an xaction/url we need to open those files to check for the property
      String value = null;
      // figure out what the XML string says the encoding is
      if (resourceFile.getFileName().endsWith(".xaction")) { //$NON-NLS-1$
        String xml = new String(resourceFile.getData());
        Document document = null;
        try {
          document = XmlDom4JHelper.getDocFromString(xml, new SolutionURIResolver());
          value = document.selectSingleNode("/action-sequence/" + key).getText(); //$NON-NLS-1$
        } catch (Throwable t) {
          value = null;
        }
      }
      if ((value == null) || "".equals(value)) { //$NON-NLS-1$
        localizedName = getLocaleString(key, baseName, resourceFile, false);
      } else if (value.startsWith("%")) { //$NON-NLS-1$
        // regular file info is stored in basename.properties files
        localizedName = getLocaleString(value.substring(1), baseName, resourceFile, false);
      } else {
        localizedName = value;
      }
    }
    return localizedName;
  }

  protected String buildDirectoryPath(final String solution, final String path, final String action) {
    String localDirStr = EMPTY_STRING;
    localDirStr += ISolutionRepository.SEPARATOR;
    if ((solution != null) && (solution.length() > 0)) {
      localDirStr += solution;
      if ((path != null) && (path.length() > 0)) {
        localDirStr += ISolutionRepository.SEPARATOR;
        localDirStr += path;
      }
    }
    if ((action != null) && (action.length() > 0)) {
      String seperator = new String() + ISolutionRepository.SEPARATOR;
      if (!localDirStr.endsWith(seperator)) {
        localDirStr += ISolutionRepository.SEPARATOR;
      }
      localDirStr += action;
    }
    return localDirStr;
  }

  protected String buildDirectoryPath(final String repositoryName, final String path) {
    String seperator = null;
    seperator += ISolutionRepository.SEPARATOR;
    int initialStartingPoint = path.indexOf(repositoryName.replaceAll(seperator, EMPTY_STRING));
    if(initialStartingPoint >=0) {
      int start = path.indexOf(ISolutionRepository.SEPARATOR, initialStartingPoint);
      if(start >= 0) {
        return path.substring(start + 1, path.length());    
      }
    }
    return path;
  }
  
  // TODO sbarkdull, refactor, this should should be in the XmlHelper class
  // maybe rename getNodeTextOrDefault
  protected String getValue(final Document doc, final String xPath, final String defaultValue) {
    if (doc != null) {
      Node node = doc.selectSingleNode(xPath);
      if (node == null) {
        return defaultValue;
      }
      return node.getText();
    }
    return defaultValue;
  }
  
  @Deprecated
  public boolean resourceExists(String solutionPath) {
    return resourceExists(solutionPath, ISolutionRepository.ACTION_EXECUTE);
  }

}
