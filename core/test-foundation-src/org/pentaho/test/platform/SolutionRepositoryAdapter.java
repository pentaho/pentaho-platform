/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform;

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
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;

public class SolutionRepositoryAdapter implements ISolutionRepository {

  public void addPermission(ISolutionFile file, IPermissionRecipient recipient, IPermissionMask permission) {
    // TODO Auto-generated method stub

  }

  public int addSolutionFile(String baseUrl, String path, String fileName, File f, boolean overwrite) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int addSolutionFile(String baseUrl, String path, String fileName, byte[] data, boolean overwrite) {
    // TODO Auto-generated method stub
    return 0;
  }

  public ISolutionFile createFolder(File newFolder) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public IActionSequence getActionSequence(String solutionName, String actionPath, String actionName, int loggingLevel,
      int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getAllActionSequences() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getAllActionSequences(int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions(ISolutionFile file) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getFileByPath(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getFullSolutionTree(int actionOperation, ISolutionFilter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getFullSolutionTree(int actionOperation, ISolutionFilter filter, ISolutionFile startFile) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getLocalizedFileProperty(ISolutionFile resourceFile, String key) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getLocalizedFileProperty(ISolutionFile resourceFile, String key, int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public List getMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getNavigationUIDocument(String solution, String path, int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(ISolutionFile file) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getRootFolder(int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(String path, int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(IActionSequenceResource actionResource) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(IActionSequenceResource actionResource, int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public long getSolutionFileLastModified(String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getSolutionFileLastModified(String path, int actionOperation) {
    // TODO Auto-generated method stub
    return 0;
  }

  public Document getSolutionStructure(int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutionTree(int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutionTree(int actionOperation, ISolutionFilter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutions(int actionOperation) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutions(String solutionName, String pathName, int actionOperation, boolean visibleOnly) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getXSLName(Document doc, String solution, String inputXSLName) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasAccess(ISolutionFile file, int actionOperation) {
    // TODO Auto-generated method stub
    return false;
  }

  public void init(IPentahoSession session) {
    // TODO Auto-generated method stub

  }

  public void localizeDoc(Node document, ISolutionFile file) {
    // TODO Auto-generated method stub

  }

  public int publish(String baseUrl, String path, String fileName, byte[] data, boolean overwrite)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub
    return 0;
  }

  public int publish(String baseUrl, String path, String fileName, File f, boolean overwrite)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub
    return 0;
  }

  public void reloadSolutionRepository(IPentahoSession session, int loggingLevel) {
    // TODO Auto-generated method stub

  }

  public boolean removeSolutionFile(String solution, String path, String fileName) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean removeSolutionFile(String solutionPath) {
    // TODO Auto-generated method stub
    return false;
  }

  public void resetRepository() {
    // TODO Auto-generated method stub

  }

  public boolean resourceExists(String solutionPath) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean resourceExists(String solutionPath, int actionOperation) {
    // TODO Auto-generated method stub
    return false;
  }

  public long resourceSize(String solutionPath) {
    // TODO Auto-generated method stub
    return 0;
  }

  public long resourceSize(String solutionPath, int actionOperation) {
    // TODO Auto-generated method stub
    return 0;
  }

  public void setMessages(List messages) {
    // TODO Auto-generated method stub

  }

  public void setPermissions(ISolutionFile file, Map<IPermissionRecipient, IPermissionMask> acl)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub

  }

  public void share(ISolutionFile file, List<IPermissionRecipient> shareRecipients) {
    // TODO Auto-generated method stub

  }

  public boolean solutionSynchronizationSupported() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean supportsAccessControls() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean synchronizeSolutionWithSolutionSource(IPentahoSession session) throws UnsupportedOperationException {
    // TODO Auto-generated method stub
    return false;
  }

  public void debug(String message) {
    // TODO Auto-generated method stub

  }

  public void debug(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void error(String message) {
    // TODO Auto-generated method stub

  }

  public void error(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void fatal(String message) {
    // TODO Auto-generated method stub

  }

  public void fatal(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public int getLoggingLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void info(String message) {
    // TODO Auto-generated method stub

  }

  public void info(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void setLoggingLevel(int loggingLevel) {
    // TODO Auto-generated method stub

  }

  public void trace(String message) {
    // TODO Auto-generated method stub

  }

  public void trace(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

  public void warn(String message) {
    // TODO Auto-generated method stub

  }

  public void warn(String message, Throwable error) {
    // TODO Auto-generated method stub

  }

}
