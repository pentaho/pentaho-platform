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
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository.pmd.IPentahoMetadataDomainRepositoryImporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Class Description
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MetadataImporterTest extends TestCase {
  public void testCreation() throws Exception {
    try {
      new MetadataImporter(null);
      fail("Invalid parameters should throw exception");
    } catch (IllegalArgumentException success) {
    }

    try {
      new MetadataImporter(null, null);
      fail("Invalid parameters should throw exception");
    } catch (IllegalArgumentException success) {
    }

    final MockPMDRImporter mockImporter = new MockPMDRImporter();
    try {
      new MetadataImporter(null, mockImporter);
      fail("Invalid parameters should throw exception");
    } catch (IllegalArgumentException success) {
    }

    final IUnifiedRepository repo = new MockRepository();
    {
      final MetadataImporter importer = new MetadataImporter(repo);
      assertEquals(repo, importer.getRepository());
      assertNull(importer.getMetadataRepositoryImporter());
      importer.setMetadataRepositoryImporter(mockImporter);
      assertEquals(mockImporter, importer.getMetadataRepositoryImporter());
    }

    {
      final MetadataImporter importer = new MetadataImporter(repo, mockImporter);
      assertEquals(repo, importer.getRepository());
      assertEquals(mockImporter, importer.getMetadataRepositoryImporter());
      try {
        importer.setMetadataRepositoryImporter(null);
        fail();
      } catch (Exception success) {
      }
    }

    {
      final MetadataImporter importer = new MetadataImporter(repo);
      try {
        importer.doImport(new MockImportSource(), "", true);
        fail("Can import without a valid MetadataRepositoryImporter");
      } catch (IllegalStateException success) {
      }

      try {
        importer.setMetadataRepositoryImporter(mockImporter);
        importer.doImport(null, "", true);
        fail("Bad parameters should throw exception");
      } catch (IllegalArgumentException success) {
      }
    }
  }

  public void testDoImport() throws Exception {
  }

  /**
   *
   */
  private class MockPMDRImporter implements IPentahoMetadataDomainRepositoryImporter {
    @Override
    public void storeDomain(final InputStream inputStream, final String domainId, final boolean overwrite)
        throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    }

    @Override
    public void addLocalizationFile(final String domainId, final String locale, final InputStream inputStream, final boolean overwrite)
        throws DomainStorageException {
    }
  }

  /**
   *
   */
  private class MockRepository implements IUnifiedRepository {
    @Override
    public RepositoryFile getFile(final String path) {
      return null;
    }

    @Override
    public RepositoryFileTree getTree(final String path, final int depth, final String filter, final boolean showHidden) {
      return null;
    }

    @Override
    public RepositoryFile getFileAtVersion(final Serializable fileId, final Serializable versionId) {
      return null;
    }

    @Override
    public RepositoryFile getFileById(final Serializable fileId) {
      return null;
    }

    @Override
    public RepositoryFile getFile(final String path, final boolean loadLocaleMaps) {
      return null;
    }

    @Override
    public RepositoryFile getFileById(final Serializable fileId, final boolean loadLocaleMaps) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> T getDataForRead(final Serializable fileId, final Class<T> dataClass) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> T getDataAtVersionForRead(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> T getDataForExecute(final Serializable fileId, final Class<T> dataClass) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> T getDataAtVersionForExecute(final Serializable fileId, final Serializable versionId, final Class<T> dataClass) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> List<T> getDataForReadInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
      return null;
    }

    @Override
    public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch(final List<RepositoryFile> files, final Class<T> dataClass) {
      return null;
    }

    @Override
    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      return null;
    }

    @Override
    public RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file, final IRepositoryFileData data, final RepositoryFileAcl acl, final String versionMessage) {
      return null;
    }

    @Override
    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final String versionMessage) {
      return null;
    }

    @Override
    public RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file, final RepositoryFileAcl acl, final String versionMessage) {
      return null;
    }

    @Override
    public List<RepositoryFile> getChildren(final Serializable folderId) {
      return null;
    }

    @Override
    public List<RepositoryFile> getChildren(final Serializable folderId, final String filter) {
      return null;
    }

    @Override
    public RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data, final String versionMessage) {
      return null;
    }

    @Override
    public void deleteFile(final Serializable fileId, final boolean permanent, final String versionMessage) {
    }

    @Override
    public void deleteFile(final Serializable fileId, final String versionMessage) {
    }

    @Override
    public void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    }

    @Override
    public void copyFile(final Serializable fileId, final String destAbsPath, final String versionMessage) {
    }

    @Override
    public void undeleteFile(final Serializable fileId, final String versionMessage) {
    }

    @Override
    public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath) {
      return null;
    }

    @Override
    public List<RepositoryFile> getDeletedFiles(final String origParentFolderPath, final String filter) {
      return null;
    }

    @Override
    public List<RepositoryFile> getDeletedFiles() {
      return null;
    }

    @Override
    public boolean canUnlockFile(final Serializable fileId) {
      return false;
    }

    @Override
    public void lockFile(final Serializable fileId, final String message) {
    }

    @Override
    public void unlockFile(final Serializable fileId) {
    }

    @Override
    public RepositoryFileAcl getAcl(final Serializable fileId) {
      return null;
    }

    @Override
    public RepositoryFileAcl updateAcl(final RepositoryFileAcl acl) {
      return null;
    }

    @Override
    public boolean hasAccess(final String path, final EnumSet<RepositoryFilePermission> permissions) {
      return false;
    }

    @Override
    public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId) {
      return null;
    }

    @Override
    public List<RepositoryFileAce> getEffectiveAces(final Serializable fileId, final boolean forceEntriesInheriting) {
      return null;
    }

    @Override
    public VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId) {
      return null;
    }

    @Override
    public List<VersionSummary> getVersionSummaryInBatch(final List<RepositoryFile> files) {
      return null;
    }

    @Override
    public List<VersionSummary> getVersionSummaries(final Serializable fileId) {
      return null;
    }

    @Override
    public void deleteFileAtVersion(final Serializable fileId, final Serializable versionId) {
    }

    @Override
    public void restoreFileAtVersion(final Serializable fileId, final Serializable versionId, final String versionMessage) {
    }

    @Override
    public List<RepositoryFile> getReferrers(final Serializable fileId) {
      return null;
    }

    @Override
    public void setFileMetadata(final Serializable fileId, final Map<String, Serializable> metadataMap) {
    }

    @Override
    public Map<String, Serializable> getFileMetadata(final Serializable fileId) {
      return null;
    }
  }

  private class MockImportSource implements ImportSource {
    @Override
    public void initialize(final IUnifiedRepository repository) {
    }

    @Override
    public IRepositoryFileBundle getFile(final String path) {
      return null;
    }

    @Override
    public Iterable<IRepositoryFileBundle> getFiles() throws IOException {
      return null;
    }

    @Override
    public void addFile(final IRepositoryFileBundle file) {
    }

    @Override
    public List<ImportSource> getDependentImportSources() {
      return null;
    }

    @Override
    public void setRequiredCharset(final String charset) {
    }

    @Override
    public String getRequiredCharset() {
      return null;
    }

    @Override
    public void setOwnerName(final String ownerName) {
    }
  }
}
