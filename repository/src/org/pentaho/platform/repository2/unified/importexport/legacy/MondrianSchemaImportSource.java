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
 * @created Jan 28, 2011 
 * @author wseyler
 */

package org.pentaho.platform.repository2.unified.importexport.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ezequiel Cuellar
 * 
 */
public class MondrianSchemaImportSource extends AbstractImportSource {

	private IRepositoryFileBundle datasourcesXML;
	private final static String ETC_MONDRIAN_JCR_FOLDER = File.separator + "etc" + File.separator + "mondrian";

	public MondrianSchemaImportSource() {
	}

	public void initialize(IUnifiedRepository repository) {
		super.initialize(repository);
		for (IRepositoryFileBundle file : super.files) {
			if (file.getFile().getName().equals("datasources.xml")) {
				datasourcesXML = file;
				break;
			}
		}
	}

	public Iterable<IRepositoryFileBundle> getFiles() throws IOException {
		List<IRepositoryFileBundle> filesToImport = null;
		try {
			if (datasourcesXML == null) {
				filesToImport = super.files;
			} else {
				filesToImport = processImportingFiles();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesToImport;
	}

	public void addFile(IRepositoryFileBundle file) {
		super.files.add(file);
	}

	private List<IRepositoryFileBundle> processImportingFiles() throws Exception {
		List<IRepositoryFileBundle> importingFiles = new ArrayList<IRepositoryFileBundle>();
		for (IRepositoryFileBundle file : super.files) {

			if (isReferencedInDatasourcesXML(file)) {
				processImportingSchema(file, importingFiles);
			} else {
				file.setPath("public" + File.separator + file.getPath());
				importingFiles.add(file);
			}
		}
		if (importingFiles.contains(datasourcesXML)) {
			importingFiles.remove(datasourcesXML);
		}
		return importingFiles;
	}

	private void processImportingSchema(IRepositoryFileBundle mondrianFile, List<IRepositoryFileBundle> importingFiles) throws Exception {
		String repoPath = createCatalog(mondrianFile);

		File tempFile = File.createTempFile("tempFile", null);
		tempFile.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		IOUtils.copy(mondrianFile.getInputStream(), outputStream);

		RepositoryFile repoFile = new RepositoryFile.Builder("schema.xml").build();
		RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, ETC_MONDRIAN_JCR_FOLDER + File.separator + repoPath + File.separator, tempFile, "UTF-8", mimeTypes.get("xml".toLowerCase()));
		importingFiles.add(repoFileBundle);
	}

	private String createCatalog(IRepositoryFileBundle file) throws Exception {

		/*
		 * This is the default implementation. Use Schema name as defined in the
		 * mondrian.xml schema. Pending create alternate implementation. Use
		 * catalog name from datasources.xml
		 */

		NodeList schemas = getElementsByTagName(file, "Schema");
		Node schema = schemas.item(0);
		Node name = schema.getAttributes().getNamedItem("name");
		String catalogName = name.getTextContent();

		RepositoryFile etcMondrian = super.unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER);
		RepositoryFile catalog = super.unifiedRepository.getFile(ETC_MONDRIAN_JCR_FOLDER + File.separator + catalogName);
		if (catalog == null) {
			catalog = super.unifiedRepository.createFolder(etcMondrian.getId(), new RepositoryFile.Builder(catalogName).folder(true).build(), "");
		}
		return catalogName;
	}

	private boolean isReferencedInDatasourcesXML(IRepositoryFileBundle file) throws Exception {

		boolean isReferenced = false;
		String fullFileName = file.getPath() + file.getFile().getName();

		NodeList mondrianFiles = getElementsByTagName(datasourcesXML, "Definition");
		for (int i = 0; i < mondrianFiles.getLength(); i++) {
			Node mondrianFile = mondrianFiles.item(i);
			if (mondrianFile.getTextContent().contains(fullFileName)) {
				isReferenced = true;
				break;
			}
		}
		return isReferenced;
	}

	private NodeList getElementsByTagName(IRepositoryFileBundle documentSource, String tagName) throws Exception {
		NodeList nodes = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(documentSource.getInputStream());
		nodes = document.getElementsByTagName(tagName);
		return nodes;
	}

}
