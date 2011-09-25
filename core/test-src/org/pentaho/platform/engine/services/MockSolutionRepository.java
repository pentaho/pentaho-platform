package org.pentaho.platform.engine.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;

public class MockSolutionRepository implements ISolutionRepository {

  public static Map<String,String> files = new HashMap<String,String>();
  
  public void addPermission(ISolutionFile arg0, IPermissionRecipient arg1, IPermissionMask arg2) {
    // TODO Auto-generated method stub

  }

  public int addSolutionFile(String arg0, String arg1, String arg2, File arg3, boolean arg4) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int addSolutionFile(String arg0, String arg1, String arg2, byte[] arg3, boolean arg4) {
    
    String path = arg1+'/'+arg2;
    if( files.containsKey( path ) && !arg4 ) {
      return ISolutionRepository.FILE_EXISTS;
    }
    
    if( arg1.startsWith( "baduser" ) ) {
      return ISolutionRepository.FILE_ADD_INVALID_USER_CREDENTIALS;
    }

    if( !arg1.startsWith( "test" ) ) {
      return ISolutionRepository.FILE_ADD_FAILED;
    }
    
    String state = new String( arg3 );
    files.put(path, state);
    return ISolutionRepository.FILE_ADD_SUCCESSFUL;
    
  }

  public ISolutionFile createFolder(File arg0) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public IActionSequence getActionSequence(String arg0, String arg1, String arg2, int arg3, int arg4) {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getAllActionSequences() {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<IPermissionRecipient, IPermissionMask> getEffectivePermissions(ISolutionFile arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getFileByPath(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getFullSolutionTree(int arg0, ISolutionFilter arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getLocalizedFileProperty(ISolutionFile arg0, String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public List getMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getNavigationUIDocument(String arg0, String arg1, int arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(ISolutionFile arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRepositoryName() {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getRootFolder() {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(IActionSequenceResource arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public long getSolutionFileLastModified(String arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  public Document getSolutionStructure(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutionTree(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutionTree(int arg0, ISolutionFilter arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutions(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getSolutions(String arg0, String arg1, int arg2, boolean arg3) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getXSLName(Document arg0, String arg1, String arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasAccess(ISolutionFile arg0, int arg1) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasAccess(IPermissionRecipient arg0, ISolutionFile arg1, int arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public void init(IPentahoSession arg0) {
    // TODO Auto-generated method stub

  }

  public void localizeDoc(Node arg0, ISolutionFile arg1) {
    // TODO Auto-generated method stub

  }

  public int publish(String arg0, String arg1, String arg2, byte[] arg3, boolean arg4)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub
    return 0;
  }

  public int publish(String arg0, String arg1, String arg2, File arg3, boolean arg4)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub
    return 0;
  }

  public void reloadSolutionRepository(IPentahoSession arg0, int arg1) {
    // TODO Auto-generated method stub

  }

  public boolean removeSolutionFile(String arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean removeSolutionFile(String arg0, String arg1, String arg2) {
    // TODO Auto-generated method stub
    return false;
  }

  public void resetRepository() {
    // TODO Auto-generated method stub

  }

  public boolean resourceExists(String arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  public long resourceSize(String arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

  public void setMessages(List arg0) {
    // TODO Auto-generated method stub

  }

  public void setPermissions(ISolutionFile arg0, Map<IPermissionRecipient, IPermissionMask> arg1)
      throws PentahoAccessControlException {
    // TODO Auto-generated method stub

  }

  public void share(ISolutionFile arg0, List<IPermissionRecipient> arg1) {
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

  public boolean synchronizeSolutionWithSolutionSource(IPentahoSession arg0) throws UnsupportedOperationException {
    // TODO Auto-generated method stub
    return false;
  }

  public void unshare(ISolutionFile arg0, List<IPermissionRecipient> arg1) {
    // TODO Auto-generated method stub

  }

  public void debug(String arg0) {
    // TODO Auto-generated method stub

  }

  public void debug(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public void error(String arg0) {
    // TODO Auto-generated method stub

  }

  public void error(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public void fatal(String arg0) {
    // TODO Auto-generated method stub

  }

  public void fatal(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public int getLoggingLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void info(String arg0) {
    // TODO Auto-generated method stub

  }

  public void info(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public void setLoggingLevel(int arg0) {
    // TODO Auto-generated method stub

  }

  public void trace(String arg0) {
    // TODO Auto-generated method stub

  }

  public void trace(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public void warn(String arg0) {
    // TODO Auto-generated method stub

  }

  public void warn(String arg0, Throwable arg1) {
    // TODO Auto-generated method stub

  }

  public String[] getAllActionSequences(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Document getFullSolutionTree(int arg0, ISolutionFilter arg1, ISolutionFile arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getLocalizedFileProperty(ISolutionFile arg0, String arg1, int arg2) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getRootFolder(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(String arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(String arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public ISolutionFile getSolutionFile(IActionSequenceResource arg0, int arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public long getSolutionFileLastModified(String arg0, int arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean resourceExists(String arg0, int arg1) {
    // TODO Auto-generated method stub
    return false;
  }

  public long resourceSize(String arg0, int arg1) {
    // TODO Auto-generated method stub
    return 0;
  }

}
