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
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.repository2.unified.importexport.legacy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ezequiel Cuellar
 */
public class MondrianSchemaImportSource extends AbstractImportSource {

	private IRepositoryFileBundle datasourcesXML;
	private String mainRepositoryLocation;
	private static final Log logger = LogFactory.getLog(MondrianSchemaImportSource.class);

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

	/*
	 * Receives all the mondrian.xml schemas plus the datasources.xml. Mondrian
	 * schemas referenced in the datasources.xml are placed at
	 * "etc/mondrian/<catalog>" The rest are imported as is to ensure legacy
	 * artifacts work. The datasources.xml is filtered out and not imported into
	 * JCR.
	 */
	public Iterable<IRepositoryFileBundle> getFiles() throws IOException {

		List<IRepositoryFileBundle> filesToImport = null;
		try {
			filesToImport = processImportingFiles();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return filesToImport;
	}

	public void addFile(IRepositoryFileBundle file) {
		super.files.add(file);
	}

	private List<IRepositoryFileBundle> processImportingFiles() throws Exception {
		List<IRepositoryFileBundle> importingFiles = new ArrayList<IRepositoryFileBundle>();
		for (IRepositoryFileBundle file : super.files) {
			String datasourceInfo = getDatasourcesXMLDatasourceInfo(file);
			if (datasourceInfo != null) {
				processImportingSchema(file, datasourceInfo);
			} else {
				file.setPath(this.mainRepositoryLocation + RepositoryFile.SEPARATOR +
            RepositoryFilenameUtils.separatorsToRepository(file.getPath()));
				importingFiles.add(file);
			}
		}
		// Do not import datasources.xml
		if (importingFiles.contains(datasourcesXML)) {
			importingFiles.remove(datasourcesXML);
		}
		return importingFiles;
	}

	private void processImportingSchema(IRepositoryFileBundle mondrianFile, String datasourceInfo) throws Exception {

		NodeList schemas = getElementsByTagName(mondrianFile, "Schema");
		Node schema = schemas.item(0);
		Node name = schema.getAttributes().getNamedItem("name");
		String catalogName = name.getTextContent();

		MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper();
		helper.addSchema(mondrianFile.getInputStream(), catalogName, datasourceInfo);
	}

	/*
	 * Parses the datasources.xml and if the current file is referenced in a
	 * <Definition> it returns the <DataSourceInfo>
	 */
	private String getDatasourcesXMLDatasourceInfo(IRepositoryFileBundle file) throws Exception {
		String datasourceInfo = null;
		if (datasourcesXML != null) {
			String fullFileName = file.getPath() + file.getFile().getName();
			fullFileName = RepositoryFilenameUtils.separatorsToRepository(fullFileName);
			NodeList mondrianDefinitions = getElementsByTagName(datasourcesXML, "Definition");
			for (int i = 0; i < mondrianDefinitions.getLength(); i++) {
				Node mondrianDefinition = mondrianDefinitions.item(i);
				if (mondrianDefinition.getTextContent().contains(fullFileName)) {
					Node datasourceInfoNode = null;
					NodeList nodeList = mondrianDefinition.getParentNode().getChildNodes();
					for (int j = 0; j < mondrianDefinitions.getLength(); j++) {
						datasourceInfoNode = nodeList.item(j);
						if (datasourceInfoNode.getNodeName().equals("DataSourceInfo")) {
							break;
						} else {
							datasourceInfoNode = null;
						}
					}
					if (datasourceInfoNode == null || !datasourceInfoNode.getNodeName().equals("DataSourceInfo")) {
						throw new Exception("<DataSourceInfo> not found in datasources.xml for <Definition> " + mondrianDefinition.getTextContent());
					}
					datasourceInfo = datasourceInfoNode.getTextContent();
					break;
				}
			}
		}
		return datasourceInfo;
	}

	private NodeList getElementsByTagName(IRepositoryFileBundle documentSource, String tagName) throws Exception {
		NodeList nodes = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(documentSource.getInputStream());
		nodes = document.getElementsByTagName(tagName);
		return nodes;
	}

	public void setOwnerName(String ownerName) {
		this.mainRepositoryLocation = ownerName;
	}
}
