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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class LocaleImportHandler implements IPlatformImportHandler {

	private List<String> artifacts;
	private IUnifiedRepository unifiedRepository;

	public LocaleImportHandler(List<String> artifacts) {
		this.unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
		this.artifacts = artifacts;
	}

	public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException, DomainAlreadyExistsException, DomainStorageException, IOException {
		RepositoryFileImportBundle locale = (RepositoryFileImportBundle) bundle;
		RepositoryFile localeParent = getLocaleParent(locale);

		Properties localeProperties = new Properties();
		localeProperties.setProperty("file.title", locale.getName());
		localeProperties.setProperty("file.description", locale.getComment());

		if (localeParent != null && unifiedRepository != null) {
			unifiedRepository.setLocalePropertiesForFile(localeParent, extractLocaleCode(locale), localeProperties);
		}
	}

	private String extractLocaleCode(RepositoryFileImportBundle localeBundle) {
		String localeCode = "default";
		String localeFileName = localeBundle.getFile().getName();
		for (Locale locale : Locale.getAvailableLocales()) {
			if (localeFileName.endsWith("_" + locale + ".properties")) {
				localeCode = locale.toString();
				break;
			}
		}
		return localeCode;
	}

	private RepositoryFile getLocaleParent(RepositoryFileImportBundle locale) {
		if (unifiedRepository == null) {
			return null;
		}

		RepositoryFile localeParent = null;
		String localeFileName = locale.getFile().getName();
		RepositoryFile localeFolder = unifiedRepository.getFile(locale.getPath());

		if (localeFileName.startsWith("index") && localeFileName.endsWith(".properties")) {
			localeParent = localeFolder;
		} else {
			List<RepositoryFile> localeFolderChildren = unifiedRepository.getChildren(localeFolder.getId());
			for (RepositoryFile localeChild : localeFolderChildren) {

				String localeChildName = extractFileName(localeChild.getName());
				String localeChildExtension = extractExtension(localeChild.getName());

				if (localeFileName.startsWith(localeChildName) && artifacts.contains(localeChildExtension)) {
					localeParent = localeChild;
					break;
				}
			}
		}
		return localeParent;
	}

	private String extractExtension(String name) {
		int idx = name.lastIndexOf(".");
		if (idx == -1 || idx == name.length()) {
			return name;
		}
		return name.substring(idx + 1);
	}

	private String extractFileName(String name) {
		int idx = name.lastIndexOf(".");
		if (idx == -1 || idx == name.length()) {
			return name;
		}
		return name.substring(0, idx);
	}
}
