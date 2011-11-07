/*
 * Copyright 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Nov 7, 2011 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;
import org.pentaho.platform.repository2.unified.importexport.StreamConverter;

public class MondrianCatalogRepositoryHelper {

	private final static String ETC_MONDRIAN_JCR_FOLDER = RepositoryFile.SEPARATOR + "etc" + RepositoryFile.SEPARATOR + "mondrian";

	private IUnifiedRepository unifiedRepository;

	public MondrianCatalogRepositoryHelper() {
		this.unifiedRepository = PentahoSystem.get(IUnifiedRepository.class);
	}

	public void addSchema(InputStream mondrianFile, String catalogName, String datasourceInfo) throws Exception {
		RepositoryFile catalog = createCatalog(catalogName, datasourceInfo);

		File tempFile = File.createTempFile("tempFile", null);
		tempFile.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		IOUtils.copy(mondrianFile, outputStream);

		RepositoryFile repoFile = new RepositoryFile.Builder("schema.xml").build();
		RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR, tempFile, "UTF-8", "text/xml");

		RepositoryFile schema = unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR + "schema.xml");
		IRepositoryFileData data = new StreamConverter().convert(repoFileBundle.getInputStream(), repoFileBundle.getCharset(), repoFileBundle.getMimeType());
		if (schema == null) {
			unifiedRepository.createFile(catalog.getId(), repoFileBundle.getFile(), data, null);
		} else {
			unifiedRepository.updateFile(schema, data, null);
		}
	}

	/*
	 * Creates "/etc/mondrian/<catalog>"
	 */
	private RepositoryFile createCatalog(String catalogName, String datasourceInfo) {

		/*
		 * This is the default implementation. Use Schema name as defined in the
		 * mondrian.xml schema. Pending create alternate implementation. Use
		 * catalog name from datasources.xml
		 */

		RepositoryFile etcMondrian = unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER);
		RepositoryFile catalog = unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName);
		if (catalog == null) {
			catalog = unifiedRepository.createFolder(etcMondrian.getId(), new RepositoryFile.Builder(catalogName).folder(true).build(), "");
		}
		createDatasourceMetadata(catalog, datasourceInfo);
		return catalog;
	}

	/*
	 * Creates "/etc/mondrian/<catalog>/metadata" and the connection nodes
	 */
	private void createDatasourceMetadata(RepositoryFile catalog, String datasourceInfo) {

		RepositoryFile metadata = unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalog.getName() + RepositoryFile.SEPARATOR + "metadata");

		String definition = "mondrian:/" + catalog.getName();
		DataNode node = new DataNode("catalog");
		node.setProperty("definition", definition);
		node.setProperty("datasourceInfo", datasourceInfo);
		NodeRepositoryFileData data = new NodeRepositoryFileData(node);

		if (metadata == null) {
			unifiedRepository.createFile(catalog.getId(), new RepositoryFile.Builder("metadata").build(), data, null);
		} else {
			unifiedRepository.updateFile(metadata, data, null);
		}
	}

}
