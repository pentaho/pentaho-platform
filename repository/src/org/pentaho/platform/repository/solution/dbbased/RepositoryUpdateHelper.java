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
package org.pentaho.platform.repository.solution.dbbased;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.FileHelper;

/**
 * This class is used for handling all the bits and twiddles of updating the RDBMS Solution Repository. All state during update is handled here.
 * 
 * @author mbatchel
 */
public class RepositoryUpdateHelper {

  protected static final Log logger = LogFactory.getLog(RepositoryUpdateHelper.class);

  String fromBase; // The directory name of the solution

  String toBase; // The name of the root folder in the RDBMS

  // Holds from->to name replacements
  //
  // E.g. From: c:\workspace\pentaho\solutions\samples\myfile.xaction
  // To: /solutions/samples/myfile.xaction
  Map nameReplacementMap = new HashMap();

  Map reposFileStructure; // Passed in contains the RDBMS files and folders with last modified dates

  Map createdOrRetrievedFolders = new HashMap(); // As the name states

  List updatedFiles = new ArrayList();

  List newFolders = new ArrayList();

  List newFiles = new ArrayList();

  List updatedFolders = new ArrayList();

  DbBasedSolutionRepository dbBasedRepository;

  private static final Pattern SlashPattern = Pattern.compile("\\\\"); //$NON-NLS-1$

  protected RepositoryUpdateHelper(final String fromBase, final String toBase, final Map reposFileStructure,
      final DbBasedSolutionRepository inRepository) {
    this.fromBase = fromBase;
    this.toBase = toBase;
    this.reposFileStructure = reposFileStructure;
    dbBasedRepository = inRepository;
  }

  /**
   * Converts the name from a DOS/Windows/Unix canonical name into the name in the RDBMS repository For example: From: c:\workspace\pentaho\solutions\samples\myfile.xaction To: /solutions/samples/myfile.xaction
   * 
   * @param fName
   *          Canonical file name
   * @return Fixed file name within the RDBMS repository
   */
  protected String convertFileName(final String fName) {
    String rtn = (String) nameReplacementMap.get(fName); // Check to see if I've already done this
    if (rtn == null) {
      // Need to do the conversion
      rtn = toBase + RepositoryUpdateHelper.SlashPattern.matcher(fName.substring(fromBase.length())).replaceAll("/"); //$NON-NLS-1$
      nameReplacementMap.put(fName, rtn);
    }
    return rtn;
  }

  /**
   * Process additions (files and folders) by adding them to the repository tree
   * 
   * @throws IOException
   */
  protected void processAdditions() throws IOException {
    //
    // Process New Folders
    //
    for (int i = 0; i < newFolders.size(); i++) {
      File newFolder = (File) newFolders.get(i);
      RepositoryFile newFolderObject = createFolder(newFolder);
      RepositoryUpdateHelper.logger.info(Messages.getInstance().getString(
          "SolutionRepository.INFO_0004_ADDED_FOLDER", newFolderObject.getFullPath())); //$NON-NLS-1$
    }
    //
    // Process New Files
    //
    for (int i = 0; i < newFiles.size(); i++) {
      File newFile = (File) newFiles.get(i);
      RepositoryFile newFileObject = createNewFile(newFile);
      RepositoryUpdateHelper.logger.info(Messages.getInstance().getString(
          "SolutionRepository.INFO_0006_ADDED_FILE", newFileObject.getFullPath())); //$NON-NLS-1$
    }
  }

  /**
   * Process updates (files and folders) by updating their time and/or updating contents
   * 
   * @throws IOException
   */
  protected void processUpdates() throws IOException {
    //
    // Process Updated Files
    //
    for (int i = 0; i < updatedFiles.size(); i++) {
      File updatedFile = (File) updatedFiles.get(i);
      String updRepoFileName = convertFileName(updatedFile.getAbsolutePath());
      RepositoryFile updRepoFileObject = (RepositoryFile) dbBasedRepository.internalGetFileByPath(updRepoFileName); // Hibernate Query
      byte[] data = FileHelper.getBytesFromFile(updatedFile);
      updRepoFileObject.setLastModified(updatedFile.lastModified());
      updRepoFileObject.setData(data);
      RepositoryUpdateHelper.logger.info(Messages.getInstance().getString(
          "SolutionRepository.INFO_0007_UPDATED_FILE", updRepoFileObject.getFullPath())); //$NON-NLS-1$
    }
    //
    // Process Updated Folders
    //
    RepositoryFile updFolderObject = null;
    for (int i = 0; i < updatedFolders.size(); i++) {
      File updatedFolder = (File) updatedFolders.get(i);
      String folderNameCorrected = this.convertFileName(updatedFolder.getAbsolutePath());
      // Check for it to already be there...
      updFolderObject = (RepositoryFile) createdOrRetrievedFolders.get(folderNameCorrected);
      if (updFolderObject == null) {
        updFolderObject = (RepositoryFile) dbBasedRepository.internalGetFileByPath(folderNameCorrected); // Hibernate Query
        createdOrRetrievedFolders.put(folderNameCorrected, updFolderObject); // Put it here so we can use it later if needed
      }
      updFolderObject.setLastModified(updatedFolder.lastModified()); // Update the date/time stamp
      RepositoryUpdateHelper.logger.info(Messages.getInstance().getString(
          "SolutionRepository.INFO_0002_UPDATED_FOLDER", folderNameCorrected)); //$NON-NLS-1$
    }
  }

  /**
   * Processes deletions by looking at the InfoHolder object for items that weren't touched during traversal of the file system.
   * 
   * @param deleteOrphans
   *          Whether to actually delete the items from Hibernate
   * @return
   */
  protected List processDeletions(final boolean deleteOrphans) {
    // Return (and optionally process) deletions
    List deletions = new ArrayList();
    Iterator it = reposFileStructure.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry me = (Map.Entry) it.next();
      InfoHolder info = (InfoHolder) me.getValue();
      if (!info.touched) {
        deletions.add(me.getKey());
      }
    }
    if (deleteOrphans) {
      performHibernateDelete(deletions);
    }
    return deletions;
  }

  /**
   * Actually deletes the RepositoryFile objects from Hibernate
   * 
   * @param deletions
   *          List of files deleted
   */
  protected void performHibernateDelete(final List deletions) {

    // TODO: This should really be handled with a subQuery rather than 
    // an in clause... Oracle in clauses are notoriously bad performers.
    // Not changing the implementation now as we are releasing 1.6GA soon. 

    List listOfDeletions = new ArrayList();
    Criteria criteria = null;

    if ((deletions != null) && (deletions.size() > 0)) {

      if (HibernateUtil.isOracleDialect()) {
        for (int i = 0; i < deletions.size(); i += 500) {
          // Oracle sets a limit on the number of items contained in  
          // the "in" clause on a SQL statement (< 1000). So we are chunking
          // deletions by 500, in case we run into the case where we exceed the Oracle
          // limit. 
          // See the following threads for details:
          // http://www.dbforums.com/showthread.php?t=369013
          // http://jira.pentaho.org:8080/browse/BISERVER-372
          int maxChunkIndex = (i + 500) > deletions.size() ? deletions.size() : (i + 500);
          List chunkDeletions = deletions.subList(i, maxChunkIndex);
          criteria = HibernateUtil.getSession().createCriteria(RepositoryFile.class);
          criteria.add(Restrictions.in("fullPath", chunkDeletions)); // Get all objects to be deleted. //$NON-NLS-1$
          listOfDeletions.add(criteria);
        }
      } else { // all other dialects
        criteria = HibernateUtil.getSession().createCriteria(RepositoryFile.class);
        criteria.add(Restrictions.in("fullPath", deletions)); // Get all objects to be deleted. //$NON-NLS-1$
        // Due to a bug in the Oracle JDBC driver, we must disable outer join fetching in 
        // Hibernate in order for deletions to execute successfully.
        // See the following threads for details:
        // http://forum.hibernate.org/viewtopic.php?t=82
        // http://forum.hibernate.org/viewtopic.php?t=930650
        // http://jira.pentaho.org/browse/BISERVER-232
        criteria.setFetchMode("parent", FetchMode.JOIN); //$NON-NLS-1$
        listOfDeletions.add(criteria);
      }

      for (Iterator iter = listOfDeletions.iterator(); iter.hasNext();) {
        Criteria element = (Criteria) iter.next();
        List deleteResult = element.list(); // Should return all things to be deleted.
        for (int i = 0; i < deleteResult.size(); i++) {
          RepositoryFile toBeDeleted = (RepositoryFile) deleteResult.get(i);
          RepositoryFile deletedParent = (RepositoryFile) toBeDeleted.retrieveParent();
          if (deletedParent != null) {
            deletedParent.removeChildFile(toBeDeleted);
          }
          HibernateUtil.makeTransient(toBeDeleted);
        }
      }
    }
  }

  /**
   * Determines whether the folder already exists, or needs to be added.
   * 
   * @param aFile
   *          The File object pointing to the folder on the drive
   * @throws IOException
   */
  protected void recordFolder(final File aFile) throws IOException {
    String fixedFileName = convertFileName(aFile.getAbsolutePath());
    InfoHolder infoHolder = (InfoHolder) reposFileStructure.get(fixedFileName);
    if (infoHolder != null) {
      infoHolder.touched = true;
      if (aFile.lastModified() != infoHolder.lastModifiedDate) {
        updatedFolders.add(aFile);
      }
    } else {
      newFolders.add(aFile);
    }
  }

  /**
   * Determines whether a file has been updated or was added.
   * 
   * @param f
   *          File object pointing to the file on the hard drive
   * @return true if the file has been changed.
   * @throws IOException
   */
  protected boolean recordFile(final File f) throws IOException {
    boolean changed = false;
    // First, convert the file - this code will move soon
    String fName = f.getAbsolutePath();
    String convertedSolnFileName = convertFileName(fName);
    long lastRDBMSModDate = getLastModifiedDateFromMap(convertedSolnFileName);
    if (lastRDBMSModDate > 0) {
      // File is in RDBMS. Check the mode date
      if (f.lastModified() != lastRDBMSModDate) {
        updatedFiles.add(f);
        changed = true;
      }
    } else {
      // This file is brand-spankin' new
      newFiles.add(f);
    }
    return changed;
  }

  /**
   * Retrieves the last modified date from the map returned from Hibernate. Also touches the object in the map to indicate it was traversed during the filesystem crawl
   * 
   * @param fileName
   *          The name of the file to lookup in the map
   * @return null if the file isn't already in the RDBMS, or the last modified date/time of the file
   */
  protected long getLastModifiedDateFromMap(final String fileName) {
    InfoHolder info = (InfoHolder) reposFileStructure.get(fileName);
    if (info != null) {
      info.touched = true;
      return info.lastModifiedDate;
    }
    return -1;
  }

  /**
   * Gets the parent folder for the file/folder. May result in a Hibernate query if the folder wasn't one that was created during the process. There shouldn't be a way for the folder to not exist. Either it was previously created during this update cycle (in which case it'll already be in the createdOrRetrievedFolders map) or it already existed in the RDBMS Repo in which case it will be retrieved and put in the map.
   * 
   * @param parentName
   *          The solution path to the parent.
   * @return RepositoryFile The parent object
   */
  protected ISolutionFile getParent(final String parentName) {
    // Check the map first
    ISolutionFile theParent = (RepositoryFile) createdOrRetrievedFolders.get(parentName);
    if (theParent == null) {
      // It's not there - need to get it from the RDBMS
      theParent = dbBasedRepository.internalGetFileByPath(parentName); // Hibernate Query
      createdOrRetrievedFolders.put(parentName, theParent);
    }
    return theParent;
  }

  /**
   * This method creates a new folder in the RDBMS Repository. This means finding the correct parent (a Hibernate Query may have to be executed to get the parent).
   * 
   * @param newFolder
   *          The File that points to the new folder to create
   * @return The RepositoryFile object created
   * @throws IOException
   */
  protected RepositoryFile createFolder(final File newFolder) throws IOException {
    // Determine the corrected file name of the new folder
    String fixedFolderName = convertFileName(newFolder.getAbsolutePath());
    // Get the file's parent folder
    File parentFolder = newFolder.getParentFile();
    // Get the corrected file name of the parent folder
    String fixedParentFolderName = convertFileName(parentFolder.getAbsolutePath());
    // Get the Parent Folder either from our map or from Hibernate if necessary
    RepositoryFile parentFolderObject = (RepositoryFile) getParent(fixedParentFolderName);
    if (parentFolderObject == null) {
      parentFolderObject = (RepositoryFile)dbBasedRepository.internalGetRootFolder();
    }
    // Now, we have the parent in hand, we can create the RepositoryFile object
    RepositoryFile newFolderObject = new RepositoryFile(newFolder.getName(), parentFolderObject, null, newFolder
        .lastModified());
    createdOrRetrievedFolders.put(fixedFolderName, newFolderObject); // Add to map for later potential use
    return newFolderObject;
  }

  /**
   * Creates a new RepositoryFile object from the File on the hard drive
   * 
   * @param newFile
   *          The File object pointing to the file on the hard drive
   * @return RepositoryFile object created
   * @throws IOException
   */
  protected RepositoryFile createNewFile(final File newFile) throws IOException {
    File parentFolder = newFile.getParentFile(); // Gets the parent folder of the File object
    String fixedParentFolderName = convertFileName(parentFolder.getAbsolutePath()); // Get the parent RepositoryFile object
    RepositoryFile parentFolderObject = (RepositoryFile) getParent(fixedParentFolderName); // Fix up the name for the solution repository
    // Create the new Object
    RepositoryFile newFileObject = new RepositoryFile(newFile.getName(), parentFolderObject, FileHelper
        .getBytesFromFile(newFile), newFile.lastModified());
    return newFileObject;
  }
  /*
   * private void deleteFilesFromSolutionTree(List deleteList) { if (deleteList != null) { Iterator iter = deleteList.iterator(); while (iter.hasNext()) { RepositoryFile file = (RepositoryFile) iter.next(); RepositoryFile parent = file.getParent(); if (parent != null) { // this take care of the case of deleting the // repository completely parent.removeChildFile(file); } } } }
   */
}

/** *************************** END New Update DB Repository Methods ******************************* */
