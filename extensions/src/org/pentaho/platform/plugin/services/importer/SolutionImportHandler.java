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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifest;
import org.pentaho.platform.repository2.unified.exportManifest.ExportManifestEntity;
import org.pentaho.platform.repository2.unified.exportManifest.Parameters;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestMondrian;

public class SolutionImportHandler implements IPlatformImportHandler {

	private static final Log log = LogFactory.getLog(SolutionImportHandler.class);
	private IPlatformImportMimeResolver mimeResolver;
	private List<String> blackList;
	private List<String> whiteList;
  private static final String sep = ";";
  private Map<String,RepositoryFileImportBundle.Builder> cachedImports;

  public SolutionImportHandler(IPlatformImportMimeResolver mimeResolver) {
		this.mimeResolver = mimeResolver;
	}
	
	public ImportSession getImportSession() {
	  return ImportSession.getSession();
	}

	public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {

		RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
		ZipInputStream zipImportStream = new ZipInputStream(bundle.getInputStream());
		SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource(zipImportStream);
		LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
		
    //importSession.set(ImportSession.getSession());
		
		IPlatformImporter importer = PentahoSystem.get(IPlatformImporter.class);

    cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();

    // Process Metadata
    ExportManifest manifest = getImportSession().getManifest();
    List<ExportManifestMetadata> metadataList = manifest.getMetadataList();
    for (ExportManifestMetadata exportManifestMetadata : metadataList) {

      String domainId = exportManifestMetadata.getDomainId();
      boolean overWriteInRepository = true;
      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder()
          .charSet("UTF-8")
          .hidden(false)
          .overwriteFile(overWriteInRepository)
          .mime("text/xmi+xml")
          .withParam("domain-id", domainId);

      cachedImports.put(exportManifestMetadata.getFile(), bundleBuilder);

    }

    // Process Mondrian
    List<ExportManifestMondrian> mondrianList = manifest.getMondrianList();
    for (ExportManifestMondrian exportManifestMondrian : mondrianList) {

      String catName = exportManifestMondrian.getCatalogName();
      Parameters parametersMap = exportManifestMondrian.getParameters();
      StringBuilder parametersStr = new StringBuilder();
      for (String s : parametersMap.keySet()) {
        parametersStr.append(s).append("=").append(parametersMap.get(s)).append(sep);
      }

      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder()
          .charSet("UTF_8").hidden(false)
          .name(catName)
          .overwriteFile(true)
          .mime("application/vnd.pentaho.mondrian+xml")
          .withParam("parameters", parametersStr.toString())
          .withParam("domain-id", catName); // TODO: this is definitely named wrong at the very least.
      //pass as param if not in parameters string
      String xmlaEnabled = ""+ exportManifestMondrian.isXmlaEnabled();
      bundleBuilder.withParam("EnableXmla", xmlaEnabled);

      cachedImports.put(exportManifestMondrian.getFile(), bundleBuilder);
    }

    for (IRepositoryFileBundle file : importSource.getFiles()) {
			String fileName = file.getFile().getName();
			String repositoryFilePath = RepositoryFilenameUtils.concat(PentahoPlatformImporter.computeBundlePath(file.getPath()), fileName);

			// Validate against importing system related artifacts.
			if (isSystemPath(repositoryFilePath)) {
				log.trace("Skipping [" + repositoryFilePath + "], it is in admin / system folders");
				continue;
			} else if(this.cachedImports.containsKey(repositoryFilePath)){

        byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        RepositoryFileImportBundle.Builder builder = cachedImports.get(repositoryFilePath);
        builder.input(new ByteArrayInputStream(bytes));

        importer.importFile(builder.build());
        continue;
      }
			RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();

			InputStream bundleInputStream = null;
			if (file.getFile().isFolder()) {
				bundleBuilder.mime("text/directory");
				bundleBuilder.file(file.getFile());
				fileName = repositoryFilePath;
				repositoryFilePath = importBundle.getPath();
			} else {
				byte[] bytes = IOUtils.toByteArray(file.getInputStream());
				bundleInputStream = new ByteArrayInputStream(bytes);
				// If is locale file store it for later processing.
				if (localeFilesProcessor.isLocaleFile(file, importBundle.getPath(), bytes)) {
				  log.trace("Skipping [" + repositoryFilePath + "], it is a locale property file");
					continue;
				}
				bundleBuilder.input(bundleInputStream);
				bundleBuilder.mime(mimeResolver.resolveMimeForFileName(fileName));
				String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
				repositoryFilePath = RepositoryFilenameUtils.concat(importBundle.getPath(), filePath);
			}

			bundleBuilder.name(fileName);
			bundleBuilder.path(repositoryFilePath);
			
	    String sourcePath;
	    if (file.getPath().startsWith("/")) {
	      sourcePath = RepositoryFilenameUtils.concat(file.getPath().substring(1), fileName);
	    } else {
	      if (file.getFile().isFolder()) {
	          sourcePath = fileName;
	        } else {
	          sourcePath = RepositoryFilenameUtils.concat(file.getPath(), fileName);
	      }
	    }
	    getImportSession().setCurrentManifestKey(sourcePath);

			bundleBuilder.charSet(bundle.getCharset());
			bundleBuilder.overwriteFile(bundle.overwriteInRepository());
			bundleBuilder.hidden(isFileHidden(bundle, sourcePath));
			bundleBuilder.applyAclSettings(bundle.isApplyAclSettings());
			bundleBuilder.retainOwnership(bundle.isRetainOwnership());
			bundleBuilder.overwriteAclSettings(bundle.isOverwriteAclSettings());
			bundleBuilder.acl(getImportSession().processAclForFile(sourcePath));
			IPlatformImportBundle platformImportBundle = bundleBuilder.build();
			importer.importFile(platformImportBundle);

			if (bundleInputStream != null) {
				bundleInputStream.close();
				bundleInputStream = null;
			}
		}
		// Process locale files.
		localeFilesProcessor.processLocaleFiles(importer);
	}

	/**
	 * Determines if the file or folder should be hidden.  If there is a manifest entry for
	 * the file, and we are not ignoring the manifest, then set the hidden flag based on the
	 * manifest.  Otherwise use the blacklist to determine if it is hidden.
	 * 
	 * @param bundle
	 * @param filePath
	 * @return true if file/folder should be hidden, false otherwise
	 */
	private boolean isFileHidden(IPlatformImportBundle bundle, String filePath) {
	  Boolean result = getImportSession().isFileHidden(filePath);
    return (result != null) ? result : isBlackListed(filePath);
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
						IOUtils.copy(zipInputStream, fos);
						fos.close();
					}
					File file = new File(entryName);
					RepositoryFile repoFile = new RepositoryFile.Builder(file.getName()).folder(isDir).hidden(false).build();
					String parentDir = new File(entryName).getParent() == null ? RepositoryFile.SEPARATOR : new File(entryName).getParent() + RepositoryFile.SEPARATOR;
					IRepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, parentDir, tempFile, "UTF-8", null);

					if (file.getName().equals("exportManifest.xml")) {
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

		private void initializeAclManifest(IRepositoryFileBundle file) {
			try {
				byte[] bytes = IOUtils.toByteArray(file.getInputStream());
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				getImportSession().setManifest(ExportManifest.fromXml(in));
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
