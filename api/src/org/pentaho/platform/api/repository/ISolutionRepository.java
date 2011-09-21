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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jun 21, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.api.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;

/**
 * Defines methods for getting information out of a location holding the Pentaho
 * solutions.
 * 
 * @author jdixon
 *
 */

public interface ISolutionRepository extends ILogger {

  // TODO mlowery Put these in an Enum.
  // TODO mlowery These constant values now tie this interface to an implementation.
  // TODO mlowery Each related group of domain objects that require access control should define their own AclEntry
  // type.
  public static final int ACTION_EXECUTE = IPentahoAclEntry.PERM_EXECUTE; //Document being requested for execution

  public static final int ACTION_ADMIN = IPentahoAclEntry.PERM_ADMINISTRATION; //Document being requested for administration

  public static final int ACTION_SUBSCRIBE = IPentahoAclEntry.PERM_SUBSCRIBE;

  public static final int ACTION_CREATE = IPentahoAclEntry.PERM_CREATE;

  public static final int ACTION_UPDATE = IPentahoAclEntry.PERM_UPDATE;

  public static final int ACTION_DELETE = IPentahoAclEntry.PERM_DELETE;

  public static final int ACTION_SHARE = IPentahoAclEntry.PERM_UPDATE_PERMS;

  // TODO mlowery Put these in an Enum.
  public static final int FILE_EXISTS = 1;

  public static final int FILE_ADD_FAILED = 2;

  public static final int FILE_ADD_SUCCESSFUL = 3;

  public static final int FILE_ADD_INVALID_PUBLISH_PASSWORD = 4;

  public static final int FILE_ADD_INVALID_USER_CREDENTIALS = 5;

  public static final String INDEX_FILENAME = "index.xml"; //$NON-NLS-1$

  public static final char SEPARATOR = '/';
  
  public static final String REPOSITORY_SERVICE_CACHE_REGION = "repository-service-cache";
  
  /**
   * Retrieves the action sequence from the repository. Should return <code>null</code> if the
   * requested action seqeuence is not found.
   * @param solutionName The name of the solution - like the root folder
   * @param actionPath The relative path (from the solution) of where the file is stored
   * @param actionName The name of the action sequence
   * @param loggingLevel The level at which to log messages
   * @param actionOperation Whether the action sequence is being retrieved for administration or execution
   * @return
   */
  public IActionSequence getActionSequence(String solutionName, String actionPath, String actionName, int loggingLevel,
      int actionOperation);

  /**
   * Initializes the solution repository with the user session
   * @param session The current user session
   */
  public void init(IPentahoSession session);

  /**
   * Gets an XML <tt>Document</tt> representing all the solutions and all the files within all the solutions
   * @param actionOperation Indicates what the list will be used for - execution or administration
   * @return Document
   */
  public Document getSolutions(int actionOperation);

  /**
   * Gets an XML <tt>Document</tt> representing all the files within a certain path within a solution
   * @param solutionName The name of the solution to get
   * @param pathName The path from which to retrieve
   * @param actionOperation Indicates what the list will be used for - execution or administration
   * @return Document
   */
  public Document getSolutions(String solutionName, String pathName, int actionOperation, boolean visibleOnly);

  /**
   * Returns an XML document that represents the parent/child relationship of the current solution repository
   * 
   * @param actionOperation
   * @return an XML document
   */
  public Document getSolutionStructure(int actionOperation);

  /**
   * Returns a list of documents located in the solution and path.
   * 
   * @param solution - The solution to use
   * @param path - The path within the solution
   * @param subDirectories - Should this recursively process sub-directories?
   * @param visibleOnly - Only include action sequences marked as visible
   * @param actionOperation - Type of action operation to be performed
   * 
   * @return - An XML document that represents the documents
   */
  //public Document getActionSequences(String solution, String path, boolean subDirectories, boolean visibleOnly, int actionOperation);
  /**
   * Loads or reloads a solution repository after a structure change.
   * 
   * @param session - The session associated with this solution repository
   * @param loggingLevel - The requested level of logging
   */
  public void reloadSolutionRepository(IPentahoSession session, int loggingLevel);

  public String getRepositoryName();

  /**
   * Removes the file (fileName) from the path defined by the solution and path.
   * 
   * @param solution
   * @param path
   * @param fileName
   * @return - boolean indicating success
   */
  public boolean removeSolutionFile(String solution, String path, String fileName);

  public boolean removeSolutionFile(String solutionPath);

  /**
   * Adds a solution to the solution repository defined by the url that is built by
   * concatenating baseUrl, path, and fileName.  The fileName that is added has its
   * data populated by the data from File (on disk).
   * 
   * @param baseUrl
   * @param path
   * @param fileName
   * @param fi
   * @param overwrite
   * @return - int indicating status of return
   */
  public int addSolutionFile(String baseUrl, String path, String fileName, File f, boolean overwrite);

  /**
   * Adds a solution to the solution repository defined by the url that is built by
   * concatenating baseUrl, path, and fileName.  The fileName that is added has its
   * data populated by the data.
   * 
   * @param baseUrl
   * @param path
   * @param fileName
   * @param data
   * @param overwrite
   * @return - int indicating status of return
   */
  public int addSolutionFile(String baseUrl, String path, String fileName, byte[] data, boolean overwrite);

  /**
   * Returns an appropriate class loader for a specific path
   * 
   * @param path
   * @return - A ClassLoader
   */
  public ClassLoader getClassLoader(String path);

  public Document getFullSolutionTree(int actionOperation, ISolutionFilter filter);
  
  public Document getFullSolutionTree(int actionOperation, ISolutionFilter filter, ISolutionFile startFile);

  /**
   * Returns an XML document that defines the entire solution tree.  This general purpose method
   * return all files/folders in the solution in an easily understood XML document.
   * 
   * @param actionOperation
   * @return
   */
  public Document getSolutionTree(int actionOperation);

  /**
   * @param actionOperation
   * @param filter an implementation of a ISolutionFilter that determines which files will be returned
   * @return Document representing the solution tree
   */
  public Document getSolutionTree(int actionOperation, ISolutionFilter filter);

  /*
   * The following method do not use the solution, path, action model for resolving resource location.
   * Instead where indicated a complete path should be provided.
   */

  /**
   * Returns a true if the specified resource exists.
   * Default action operation to execute
   * @param solutionPath - path to the resource
   * @return - boolean true if resource exists
   */
  public boolean resourceExists(String solutionPath);

  /**
   * Returns a true if the specified resource exists.
   * 
   * @param solutionPath - path to the resource
   * @param actionOperation - Type of action operation to be performed
   * @return - boolean true if resource exists
   */
  public boolean resourceExists(String solutionPath, int actionOperation);


  /**
   * An array of Strings where each string is the fully qualified path of
   *      every *.xaction contained in the repository.
   * @param actionOperation - Type of action operation to be performed
   * @return
   */

  public String[] getAllActionSequences(int actionOperation);

  /**
   * Allows the caller to provide a List that will contain all log messages generated by the Solution Repository 
   * represented by this interface
   *   
   * @param messages a List that String messages will be appended to
   */
  @SuppressWarnings("unchecked")
  public void setMessages(List messages);

  /**
   * Return the message list, if any, that all log messages generated by this Solution Repository 
   * are being appended to
   *   
   * @return List of String messages
   */
  @SuppressWarnings("unchecked")
  public List getMessages();

  /**
   * Get an XML document that describes the structure of the solution repository.
   * Returns the document used to construct the navigation UI. Also used
   * by WAQR and jpivot to construct the repository browswer
   * 
   * @param solution String The name of the solution. If this is empty (null or ""), and path
   * is empty, return the root of the document. Otherwise return a document that
   * starts at the node specified by the solution and path in the solution
   * @param path String The path of the interested folder. See notes for
   * parameter <param>solution</param>.
   * @param actionOperation - Type of action operation to be performed
   * @return Document XML document that describes the structure of the solution repository.
   */
  public Document getNavigationUIDocument(String solution, String path, int actionOperation);

  /**
   * This method resets the caches used by the solution repository.
   */
  public void resetRepository();

  /**
   * @param actionOperation - Type of action operation to be performed
   * @return the ISolutionFile for the root of the repository
   */
  public ISolutionFile getRootFolder(int actionOperation);

  public void localizeDoc(Node document, ISolutionFile file);

  /**
   * @return whether or not the concrete versions of this interface support access controls
   */
  public boolean supportsAccessControls();

  /**
   * @param path
   * @param actionOperation - Type of action operation to be performed
   * @return
   */
  public ISolutionFile getSolutionFile(String path, int actionOperation);
  
  /**
   * @param actionResource
   * @param actionOperation - Type of action operation to be performed
   * @return
   */
  public ISolutionFile getSolutionFile(IActionSequenceResource actionResource, int actionOperation);

  /**
   * This method creates a new folder in the Repository
   * 
   * @param newFolder
   *          The File that points to the new folder to create
   * @return The RepositoryFile object created
   * @throws IOException
   */
  public ISolutionFile createFolder(File newFolder) throws IOException;

  /**
   * Checks permission. Although implementations should enforce permissions, some clients may want to see beforehand
   * whether or not an operation will succeed. An example is enabling/disable UI controls based on access.
   * 
   * @param aFile domain instance to check
   * @param actionOperation permission requested (A constant from ISolutionRepository.)
   * @return true if actionOperation is allowed for this aFile
   */
  public boolean hasAccess(ISolutionFile aFile, int actionOperation);

  /**
   * Share file with recipient. This version is appropriate for sharing with a particular role. The semantics of share
   * are encapsulated in the implementation. (The sharer comes from the IPentahoSession.)
   * @param aFile file to share
   * @param shareRecipients the users or roles with which to share (aka share-ees)
   */
  public void share(ISolutionFile aFile, List<IPermissionRecipient> shareRecipients);

  /**
   * Put a file into the solution repo. This method differs from addSolutionFile. Only Pentaho administrators can
   * successfully execute addSolutionFile. addSolutionFile is a low-level operation. There is potentially more logic 
   * in the implementation of this method than in the implementation of addSolutionFile. 
   * @param baseUrl
   * @param path
   * @param fileName
   * @param data
   * @param overwrite
   * @return
   * @throws PentahoAccessControlException 
   */
  public int publish(String baseUrl, String path, String fileName, byte[] data, boolean overwrite)
      throws PentahoAccessControlException;

  /**
   * Put a file into the solution repo. This method differs from addSolutionFile. Only Pentaho administrators can
   * successfully execute addSolutionFile. addSolutionFile is a low-level operation. There is potentially more logic 
   * in the implementation of this method than in the implementation of addSolutionFile. 
   * @param baseUrl
   * @param path
   * @param fileName
   * @param f
   * @param overwrite
   * @return constant from ISolutionRepository
   * @throws PentahoAccessControlException 
   */
  public int publish(String baseUrl, String path, String fileName, File f, boolean overwrite)
      throws PentahoAccessControlException;

  /**
   * Adds to the ACL associated with <code>aFile</code>.
   * @param aFile file whose ACL is to be modified
   * @param recipient recipient of the permission
   * @param permission right to an action on this file by this user
   */
  public void addPermission(ISolutionFile aFile, IPermissionRecipient recipient, IPermissionMask permission);

  /**
   * Replaces the ACL on <code>aFile</code>. 
   * @param aFile file whose ACL is to be modified
   * @param acl new ACL
   */
  public void setPermissions(ISolutionFile aFile, Map<IPermissionRecipient, IPermissionMask> acl)
      throws PentahoAccessControlException;

  /**
   * Returns the ACL for the given file.
   * @param aFile file whose ACL is to be returned
   * @return ACL
   */
  public Map<IPermissionRecipient, IPermissionMask> getPermissions(ISolutionFile aFile);

  /**
   * Returns the ACL for the given file. If there are no access control entries for the given file, return the access
   * control entries of an ancestor file.
   * @param aFile file whose ACL is to be returned
   * @return ACL
   */
  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions(ISolutionFile aFile);
  
  /**
   * For ISolutionRepository implementations that have a source and a
   * destination (for example, DBBasedSolutionRepository which has 
   * a file-system source and a DB destination) this is the entry point
   * that allows synchronization. Implementors should throw an
   * UnsupportedOperationException if there is no synchronization
   * necessary (for example, the FileBasedSolutionRepository).
   * @param session
   * @throws UnsupportedOperationException
   * @return boolean true if the synchronization succeeded
   */
  public boolean synchronizeSolutionWithSolutionSource(IPentahoSession session) throws UnsupportedOperationException;

  /**
   * This method retrieves a locale aware value given a key and a file.
   * The intention is to allow the repository to be used as an API, so we
   * can ask it for the "title" or "description" or "author" of a given
   * file with localization in effect.
   * 
   * @param resourceFile
   * @param key
   * @param actionOperation - Type of action operation to be performed
   * @return the String value found for the given key
   */

  public String getLocalizedFileProperty(ISolutionFile resourceFile, String key, int actionOperation);

}
