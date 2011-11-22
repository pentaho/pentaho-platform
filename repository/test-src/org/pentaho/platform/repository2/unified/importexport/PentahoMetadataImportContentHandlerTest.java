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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 * @author dkincade
 */
package org.pentaho.platform.repository2.unified.importexport;

import junit.framework.TestCase;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class Description
 * User: dkincade
 */
public class PentahoMetadataImportContentHandlerTest extends TestCase {
  @Test
  public void testGetName() throws Exception {
    assertNotNull(new PentahoMetadataImportContentHandler().getName());
  }

  @Test
  public void testInitialize() throws Exception {
    try {
      final ImportContentHandler handler = new PentahoMetadataImportContentHandler();
      handler.initialize(null, null, "", "");
      fail("initialization should fail if an IUnifiedRepository is not supplied");
    } catch (InitializationException success) {
    }

    {
      final ImportContentHandler handler = new PentahoMetadataImportContentHandler();
      final IUnifiedRepository mockRepository = new MockUnifiedRepository();
      handler.initialize(mockRepository, null, "", "");
      assertNotNull(((PentahoMetadataImportContentHandler) handler).getDomainRepository());
      assertNotNull(((PentahoMetadataImportContentHandler) handler).getXmiParser());
      assertEquals(mockRepository, ((PentahoMetadataImportContentHandler) handler).getRepository());
    }

    {
      final PentahoMetadataImportContentHandler handler = new PentahoMetadataImportContentHandler();
      final XmiParser xmiParser = new XmiParser();
      final MockDomainRepository domainRepository = new MockDomainRepository();
      final MockUnifiedRepository repository = new MockUnifiedRepository();
      handler.setXmiParser(xmiParser);
      handler.setDomainRepository(domainRepository);
      handler.initialize(repository, null, "", "");
      assertEquals(xmiParser, handler.getXmiParser());
      assertEquals(domainRepository, handler.getDomainRepository());
      assertEquals(repository, handler.getRepository());
    }
  }

  public void testPerformImport() throws Exception {
    final ImportSource.IRepositoryFileBundle bundle = new MockBundle();
    final PentahoMetadataImportContentHandler handler = new PentahoMetadataImportContentHandler();
    final MockDomainRepository domainRepository = new MockDomainRepository();
    final XmiParser xmiParser = new MockXmiParser();
    handler.setDomainRepository(domainRepository);
    handler.setXmiParser(xmiParser);
    handler.initialize(new MockUnifiedRepository(), null, null, null);

    // Conditions where the import should be skipped w/o exceptions thrown
    {
      final ImportContentHandler.Result result = handler.performImport(null, true);
      assertEquals("Import should be skipped if the bundle is null", ImportContentHandler.Result.SKIPPED, result);

      bundle.setPath(null);
      final ImportContentHandler.Result result1 = handler.performImport(bundle, true);
      assertEquals("Import should be skipped if the filename is null", ImportContentHandler.Result.SKIPPED, result1);

      bundle.setPath("/etc/this/that/other");
      final ImportContentHandler.Result result2 = handler.performImport(bundle, true);
      assertEquals("Import should be skipped if there is no extension", ImportContentHandler.Result.SKIPPED, result2);

      bundle.setPath("/etc/this/that/other.xmi/");
      final ImportContentHandler.Result result3 = handler.performImport(bundle, true);
      assertEquals("Import should be skipped if the file is a folder", ImportContentHandler.Result.SKIPPED, result3);

      bundle.setPath("/etc/this/that/myfile.prpt");
      final ImportContentHandler.Result result4 = handler.performImport(bundle, true);
      assertEquals("Import should be skipped if the extension is not xmi", ImportContentHandler.Result.SKIPPED,
          result4);

      bundle.setPath("/etc/this/that/myfile.xmii");
      final ImportContentHandler.Result result5 = handler.performImport(bundle, true);
      assertEquals("Import should be skipped if the extension is not xmi", ImportContentHandler.Result.SKIPPED,
          result5);
    }

    // Items where the import should attempt and fail
    try {
      bundle.setPath("/etc/this/that/test1.xmi");
      ((MockBundle) bundle).setData(null);
      handler.performImport(bundle, true);
      fail("An exception should be thrown if the bundle can't create an InputStream");
    } catch (ImportException success) {
    }

    try {
      ((MockBundle) bundle).setData("This is a test".getBytes());
      ((MockXmiParser) xmiParser).setSuccess(false);
      handler.performImport(bundle, true);
      fail("An exception should be thrown if the Xmi Parser could not parse the XMI data");
    } catch (ImportException success) {
    }

    try {
      bundle.setPath("/etc/this/that/test1.xmi");
      ((MockDomainRepository) domainRepository).setErrorType(MockDomainRepository.ErrorType.DOMAIN_NULL);
      handler.performImport(bundle, true);
      fail("An exception should be thrown if the Domain ID is null");
    } catch (ImportException success) {
    }

    try {
      bundle.setPath("/etc/this/that/test1.xmi");
      ((MockDomainRepository) domainRepository).setErrorType(MockDomainRepository.ErrorType.DOMAIN_DUPLICATE);
      handler.performImport(bundle, false);
      fail("An exception should be thrown if the Domain ID already exists and is duplicated");
    } catch (ImportException success) {
    }

    try {
      bundle.setPath("/etc/this/that/test1.xmi");
      ((MockDomainRepository) domainRepository).setErrorType(MockDomainRepository.ErrorType.ERROR);
      handler.performImport(bundle, true);
      fail("An exception should be thrown if an error occurs storing the domain");
    } catch (ImportException success) {
    }

    // These should succeed
    ((MockXmiParser) xmiParser).setSuccess(true);

    bundle.setPath("/etc/this/that/test1.xmi");
    ((MockDomainRepository) domainRepository).setErrorType(MockDomainRepository.ErrorType.NONE);
    assertEquals("The import should succeed if there are no errors",
        ImportContentHandler.Result.SUCCESS, handler.performImport(bundle, false));

    bundle.setPath("/etc/this/that/test1.xmi");
    ((MockDomainRepository) domainRepository).setErrorType(MockDomainRepository.ErrorType.DOMAIN_DUPLICATE);
    assertEquals("The import should succeed if this is a duplicate and overwrite is true",
        ImportContentHandler.Result.SUCCESS, handler.performImport(bundle, true));
  }

  private class MockUnifiedRepository implements IUnifiedRepository {
    public RepositoryFile getFile(final String path) {
      return null;
    }

    public RepositoryFileTree getTree(final String path, final int depth, final String filter, final boolean showHidden) {
      return null;
    }

    public RepositoryFile getFileAtVersion(final Serializable fileId, final Serializable versionId) {
      return null;
    }

    public RepositoryFile getFileById(final Serializable fileId) {
      return null;
    }

    public RepositoryFile getFile(final String path, final boolean loadLocaleMaps) {
      return null;
    }

    public RepositoryFile getFileById(final Serializable fileId, final boolean loadLocaleMaps) {
      return null;
    }

    public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass) {
      return null;
    }

    public <T extends IRepositoryFileData> T getDataAtVersionForRead(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
      return null;
    }

    public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass) {
      return null;
    }

    public <T extends IRepositoryFileData> T getDataAtVersionForExecute(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
      return null;
    }

    public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
      return null;
    }

    public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
      return null;
    }

    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      return null;
    }

    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
      return null;
    }

    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final String versionMessage) {
      return null;
    }

    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final RepositoryFileAcl acl, final String versionMessage) {
      return null;
    }

    public List<RepositoryFile> getChildren(final Serializable folderId) {
      return null;
    }

    public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
      return null;
    }

    public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      return null;
    }

    public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
    }

    public void deleteFile(final Serializable fileId, final String versionMessage) {
    }

    public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    }

    public void copyFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    }

    public void undeleteFile(final Serializable fileId, final String versionMessage) {
    }

    public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath) {
      return null;
    }

    public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
      return null;
    }

    public List<RepositoryFile> getDeletedFiles() {
      return null;
    }

    public boolean canUnlockFile(final Serializable fileId) {
      return false;
    }

    public void lockFile(final Serializable fileId, final String message) {
    }

    public void unlockFile(final Serializable fileId) {
    }

    public RepositoryFileAcl getAcl(final Serializable fileId) {
      return null;
    }

    public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
      return null;
    }

    public boolean hasAccess(final String path, final EnumSet<RepositoryFilePermission> permissions) {
      return false;
    }

    public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId) {
      return null;
    }

    public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId, final boolean forceEntriesInheriting) {
      return null;
    }

    public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
      return null;
    }

    public List<VersionSummary> getVersionSummaryInBatch(final List<RepositoryFile> files) {
      return null;
    }

    public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
      return null;
    }

    public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    }

    public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
    }

    public List<RepositoryFile> getReferrers(final Serializable fileId) {
      return null;
    }

    public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
    }

    public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
      return null;
    }
  }

  private static class MockDomainRepository implements IMetadataDomainRepository {
    private enum ErrorType {
      NONE, DOMAIN_NULL, DOMAIN_DUPLICATE, ERROR
    }

    ;
    private ErrorType errorType = ErrorType.NONE;

    public void storeDomain(final Domain domain, final boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
      switch (errorType) {
        case DOMAIN_NULL:
          throw new DomainIdNullException("Domain ID is null");
        case ERROR:
          throw new DomainStorageException("Domain Storage Exception", new Exception("Internal Error"));
        case DOMAIN_DUPLICATE:
          if (!overwrite)
            throw new DomainAlreadyExistsException("Domain already exists");
        case NONE:
          return;
      }
    }

    public Domain getDomain(final String id) {
      return null;
    }

    public Set<String> getDomainIds() {
      return null;
    }

    public void reloadDomains() {
    }

    public void flushDomains() {
    }

    public void removeDomain(final String domainId) {
    }

    public void removeModel(final String domainId, final String modelId) throws DomainIdNullException, DomainStorageException {
    }

    public String generateRowLevelSecurityConstraint(final LogicalModel model) {
      return null;
    }

    public boolean hasAccess(final int accessType, final IConcept aclHolder) {
      return false;
    }

    public void setErrorType(final ErrorType errorType) {
      this.errorType = errorType;
    }

    public ErrorType getErrorType() {
      return errorType;
    }
  }

  private class MockBundle implements ImportSource.IRepositoryFileBundle {
    private byte[] data;
    private String path;
    private String filename;

    public RepositoryFile getFile() {
      return new RepositoryFile.Builder(path, filename).build();
    }

    public RepositoryFileAcl getAcl() {
      return null;
    }

    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(data);
    }

    public String getPath() {
      return path;
    }

    public void setPath(final String path) {
      this.path = path;
      this.filename = FilenameUtils.getName(path);
    }

    public String getCharset() {
      return null;
    }

    public String getMimeType() {
      return null;
    }

    public void setData(final byte[] data) {
      this.data = data;
    }

    public String getFilename() {
      return filename;
    }

    public void setFilename(final String filename) {
      this.filename = filename;
    }
  }

  private class MockXmiParser extends XmiParser {
    private boolean success = true;

    public void setSuccess(final boolean success) {
      this.success = success;
    }

    public Domain parseXmi(final InputStream xmi) throws Exception {
      if (success) {
        return new MockDomain("domain id");
      }
      throw new Exception("Exception parsing XMI file");
    }
  }

  private class MockDomain extends Domain {
    public MockDomain(final String domainId) {
      setId(domainId);
    }
  }
}
