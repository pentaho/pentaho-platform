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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.WAQRFilesMigrationHelper;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;

public class SolutionImportHandler implements IPlatformImportHandler {

	private static final Log log = LogFactory.getLog(SolutionImportHandler.class);
	private IPlatformImportMimeResolver mimeResolver;
	private List<String> blackList;
	private List<String> whiteList;
	private ExportManifest manifest;

	public SolutionImportHandler(IPlatformImportMimeResolver mimeResolver) {
		this.mimeResolver = mimeResolver;
	}
	
	public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {

		RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
		ZipInputStream zipImportStream = new ZipInputStream(bundle.getInputStream());
		SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource(zipImportStream);

		IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);
		for (IRepositoryFileBundle file : importSource.getFiles()) {
			String fileName = file.getFile().getName();
			RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
			String repositoryFilePath = RepositoryFilenameUtils.concat(PentahoPlatformImporter.computeBundlePath(file.getPath()), fileName);

			// Validate against importing system related artifacts.
			if (isSystemPath(repositoryFilePath)) {
				log.trace("Skipping [" + repositoryFilePath + "], it is in admin / system folders");
				continue;
			}

			InputStream bundleInputStream = null;
			if (file.getFile().isFolder()) {
				bundleBuilder.mime("text/directory");
				bundleBuilder.file(file.getFile());
				fileName = repositoryFilePath;
				repositoryFilePath = importBundle.getPath();
			} else {
				bundleInputStream = file.getInputStream();
				bundleBuilder.input(bundleInputStream);
				bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileName));
				String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
				repositoryFilePath = RepositoryFilenameUtils.concat(importBundle.getPath(), filePath);
			}

			bundleBuilder.name(fileName);
			bundleBuilder.path(repositoryFilePath);
			bundleBuilder.acl(processAclForFile(repositoryFilePath, fileName));
			bundleBuilder.charSet(bundle.getCharset());
			bundleBuilder.overwriteFile(bundle.overwriteInRepossitory());
			bundleBuilder.hidden(isBlackListed(fileName));
			bundleBuilder.applyAclSettings(bundle.isApplyAclSettings());
			bundleBuilder.retainOwnership(bundle.isRetainOwnership());
			bundleBuilder.overwriteAclSettings(bundle.isOverwriteAclSettings());
			IPlatformImportBundle platformImportBundle = bundleBuilder.build();
			importer.importFile(platformImportBundle);

			if (bundleInputStream != null) {
				bundleInputStream.close();
				bundleInputStream = null;
			}
		}
	}

	private RepositoryFileAcl processAclForFile(String path, String name) {
		RepositoryFileAcl acl = null;
		try {
			String filePath = RepositoryFilenameUtils.concat(path, name);
			if(manifest != null) {
				ExportManifestEntity entity = manifest.getExportManifestEntity(filePath);
				if(entity != null) {
					acl = entity.getRepositoryFileAcl();
				}
			}
		} catch(Exception e) {
			log.trace(e);
		}
		return acl;
	}
	
	private boolean isSystemPath(final String bundlePath) {
		final String[] split = StringUtils.split(bundlePath, RepositoryFile.SEPARATOR);
		return isSystemDir(split, 0) || isSystemDir(split, 1);
	}
	
	private boolean isSystemDir(final String[] split, final int index) {
		return (split != null && index < split.length && (StringUtils.equals(split[index], "system") || StringUtils.equals(split[index], "admin")));
	}

	private boolean isBlackListed(String fileName) {
		boolean isBlackListed = false;
		for (String extension : blackList) {
			if (fileName.endsWith(extension)) {
				isBlackListed = true;
				break;
			}
		}
		return isBlackListed;
	}

	public void setBlackList(List blackList) {
		this.blackList = blackList;
	}

	public void setWhiteList(List whiteList) {
		this.whiteList = whiteList;
	}

	class SolutionRepositoryImportSource {
		private ZipInputStream zipInputStream;
		private List<IRepositoryFileBundle> files;

		public SolutionRepositoryImportSource(final ZipInputStream zipInputStream) {
			this.zipInputStream = zipInputStream;
			this.files = new ArrayList<IRepositoryFileBundle>();
			initialize();
		}

		protected void initialize() {
			try {
				ZipEntry entry = zipInputStream.getNextEntry();
				while (entry != null) {
					final String entryName = RepositoryFilenameUtils.separatorsToRepository(entry.getName());
					File tempFile = null;
					boolean isDir = entry.isDirectory();
					if (!isDir) {
						if (!isWhiteListed(entryName)) {
							zipInputStream.closeEntry();
							entry = zipInputStream.getNextEntry();
							continue;
						}
						tempFile = File.createTempFile("zip", null);
						tempFile.deleteOnExit();
						FileOutputStream fos = new FileOutputStream(tempFile);
						if (WAQRFilesMigrationHelper.isOldXWAQRFile(entryName)) {
							WAQRFilesMigrationHelper.convertToNewXWAQR(zipInputStream, fos);
						} else if (WAQRFilesMigrationHelper.isOldXreportSpecFile(entryName)) {
							WAQRFilesMigrationHelper.convertToNewXreportSpec(zipInputStream, fos);
						} else {
							IOUtils.copy(zipInputStream, fos);
						}
						fos.close();
					}
					File file = new File(entryName);
					RepositoryFile repoFile = new RepositoryFile.Builder(WAQRFilesMigrationHelper.convertToNewExtension(file.getName())).folder(isDir).hidden(WAQRFilesMigrationHelper.hideFileCheck(file.getName())).build();
					String parentDir = new File(entryName).getParent() == null ? RepositoryFile.SEPARATOR : new File(entryName).getParent() + RepositoryFile.SEPARATOR;
					IRepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, parentDir, tempFile, "UTF-8", null);
					
					if(file.getName().equals("exportManifest.xml")) {
						initializeAclManifest(repoFileBundle);
					} else {
						files.add(repoFileBundle);
					}
					zipInputStream.closeEntry();
					entry = zipInputStream.getNextEntry();
				}
				zipInputStream.close();
			} catch (IOException exception) {
				final String errorMessage = Messages.getInstance().getErrorString("", exception.getLocalizedMessage());
				log.trace(errorMessage);
			}
		}
		
		private void initializeAclManifest(IRepositoryFileBundle file)  {
			try { 
				byte[] bytes = IOUtils.toByteArray(file.getInputStream());
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				manifest = ExportManifest.fromXml(in);
			} catch (Exception e) {
				log.trace(e);
			}
		}

		private boolean isWhiteListed(String fileName) {
			boolean isWhiteListed = false;
			for (String extension : whiteList) {
				if (fileName.endsWith(extension)) {
					isWhiteListed = true;
					break;
				}
			}
			return isWhiteListed;
		}

		public List<IRepositoryFileBundle> getFiles() {
			return this.files;
		}
	}
}
