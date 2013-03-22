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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

public class LocaleFilesProcessor {

	private List<LocaleFileDescriptor> localeFiles;

	public LocaleFilesProcessor() {
		localeFiles = new ArrayList<LocaleFileDescriptor>();
	}

	public boolean isLocaleFile(IRepositoryFileBundle file, String parentPath, byte[] bytes) throws IOException {

		boolean isLocale = false;
		String fileName = file.getFile().getName();
		if (fileName.endsWith(".properties")) {
			InputStream inputStream = new ByteArrayInputStream(bytes);
			Properties properties = new Properties();
			properties.load(inputStream);

			String name = properties.getProperty("name");
			String title = properties.getProperty("title");
			String description = properties.getProperty("description");
			String url_name = properties.getProperty("url_name");
			String url_description = properties.getProperty("url_description");

			if (!StringUtils.isEmpty(url_name)) {
				name = url_name;
			}
			if (!StringUtils.isEmpty(title)) {
				name = title;
			}

			description = !StringUtils.isEmpty(description) ? description : "";
			if (!StringUtils.isEmpty(url_description)) {
				description = url_description;
			}

			if (!StringUtils.isEmpty(name)) {
				String filePath = (file.getPath().equals("/") || file.getPath().equals("\\")) ? "" : file.getPath();
				filePath = RepositoryFilenameUtils.concat(parentPath, filePath);

				LocaleFileDescriptor localeFile = new LocaleFileDescriptor(name, description, filePath, file.getFile(), inputStream);
				localeFiles.add(localeFile);

				if (properties.size() <= 2) {
					isLocale = true;
				}
			}
		}
		return isLocale;
	}

	public void processLocaleFiles(IPlatformImporter importer) throws PlatformImportException {
		RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
		NameBaseMimeResolver mimeResolver = PentahoSystem.get(NameBaseMimeResolver.class);
		String mimeType = mimeResolver.resolveMimeForFileName("file.locale");

		for (LocaleFileDescriptor localeFile : localeFiles) {
			bundleBuilder.name(localeFile.getName());
			bundleBuilder.comment(localeFile.getDescription());
			bundleBuilder.path(localeFile.getPath());
			bundleBuilder.file(localeFile.getFile());
			bundleBuilder.input(localeFile.getInputStream());
			bundleBuilder.mime(mimeType);
			IPlatformImportBundle platformImportBundle = bundleBuilder.build();
			importer.importFile(platformImportBundle);
		}
	}
}
