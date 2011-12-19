/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2011 Pentaho Corporation. All rights reserved.
 */
package org.pentaho.platform.repository2.unified.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.messages.Messages;
import org.pentaho.platform.repository2.unified.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles the importing of Mondrian datasource information
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class MondrianImportHandler implements ImportHandler {
  private static final Log log = LogFactory.getLog(MondrianImportHandler.class);
  private static final Messages messages = Messages.getInstance();

  private final IUnifiedRepository repository;

  public MondrianImportHandler(final IUnifiedRepository repository) {
    if (null == repository) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }

  /**
   * Returns the name of this Import Handler
   */
  @Override
  public String getName() {
    return "Mondrian Import Handler";
  }

  /**
   * Processes the list of files and performs any processing required to import that data into the repository. If
   * during processing it handles file(s) which should not be handled by downstream import handlers, then it
   * should remove them from the set of files provided.
   *
   * @param importFileSet   the set of files to be imported - any files handled to completion by this Import Handler
   *                        should remove this files from this list
   * @param destinationPath the requested destination location in the repository
   * @param comment         the import comment provided
   * @param overwrite       indicates if the process is authorized to overwrite existing content in the repository
   * @throws ImportException indicates a significant error during import processing
   */
  @Override
  public void doImport(final Iterable<ImportSource.IRepositoryFileBundle> importFileSet, final String destinationPath,
                       final String comment, final boolean overwrite) throws ImportException {
    if (null == importFileSet) {
      throw new IllegalArgumentException();
    }

    // Find the datasources.xml file and keep track of the mondrian files we will need to process later
    ImportSource.IRepositoryFileBundle datasourcesXML = null;
    final List<ImportSource.IRepositoryFileBundle> schemaList = new ArrayList<ImportSource.IRepositoryFileBundle>();
    for (Iterator it = importFileSet.iterator(); it.hasNext(); ) {
      final ImportSource.IRepositoryFileBundle file = (ImportSource.IRepositoryFileBundle) it.next();
      final String filename = file.getFile().getName();
      if (StringUtils.equals(filename, "datasources.xml")) {
        if (datasourcesXML != null) {
          log.warn("multiple datasources.xml files found ... old=" + datasourcesXML + " new=" + file); // TODO I18N
        }
        datasourcesXML = file;
        it.remove();
      } else if (filename.endsWith(".mondrian.xml")) {
        schemaList.add(file);
        it.remove();
      }
    }

    // If we didn't find the datasourceXML file, just stop here
    if (datasourcesXML == null) {
      if (!schemaList.isEmpty()) {
        log.warn("Found mondrian schemas to import but didn't find the datasources XML file to use - skipping "
            + schemaList.size() + " files..."); // TODO I18N
      }
    } else {
      for (final ImportSource.IRepositoryFileBundle file : schemaList) {
        try {
          final String datasourceInfo = getDatasourcesXMLDatasourceInfo(datasourcesXML, file);
          if (datasourceInfo != null) {
            processSchema(file, datasourceInfo);
          }
        } catch (Exception e) {
          final String errorMessage = messages.getErrorString("", e.getLocalizedMessage()); // TODO I18N
          log.error(errorMessage, e);
        }
      }
    }
  }

  private void processSchema(ImportSource.IRepositoryFileBundle mondrianFile, String datasourceInfo) throws Exception {
    NodeList schemas = getElementsByTagName(mondrianFile, "Schema");
    Node schema = schemas.item(0);
    Node name = schema.getAttributes().getNamedItem("name");
    String catalogName = name.getTextContent();

    MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(repository);
    helper.addSchema(mondrianFile.getInputStream(), catalogName, datasourceInfo);
  }


  /*
  * Parses the datasources.xml and if the current file is referenced in a
  * <Definition> it returns the <DataSourceInfo>
  */
  private String getDatasourcesXMLDatasourceInfo(ImportSource.IRepositoryFileBundle datasourcesXML,
                                                 ImportSource.IRepositoryFileBundle file) throws Exception {
    String datasourceInfo = null;
    if (datasourcesXML != null) {
      final String fullFileName = RepositoryFilenameUtils.separatorsToRepository(
          RepositoryFilenameUtils.concat(file.getPath(), file.getFile().getName()));
      final NodeList mondrianDefinitions = getElementsByTagName(datasourcesXML, "Definition");
      for (int i = 0; i < mondrianDefinitions.getLength(); i++) {
        final Node mondrianDefinition = mondrianDefinitions.item(i);
        if (mondrianDefinition.getTextContent().contains(fullFileName)) {
          Node datasourceInfoNode = null;
          final NodeList nodeList = mondrianDefinition.getParentNode().getChildNodes();
          for (int j = 0; j < mondrianDefinitions.getLength(); j++) {
            datasourceInfoNode = nodeList.item(j);
            if (datasourceInfoNode.getNodeName().equals("DataSourceInfo")) {
              break;
            } else {
              datasourceInfoNode = null;
            }
          }
          if (datasourceInfoNode == null || !datasourceInfoNode.getNodeName().equals("DataSourceInfo")) {
            throw new Exception("<DataSourceInfo> not found in datasources.xml for <Definition> "
                + mondrianDefinition.getTextContent());
          }
          datasourceInfo = datasourceInfoNode.getTextContent();
          break;
        }
      }
    }
    return datasourceInfo;
  }

  private NodeList getElementsByTagName(ImportSource.IRepositoryFileBundle documentSource, String tagName) throws Exception {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document document = builder.parse(documentSource.getInputStream());
    return document.getElementsByTagName(tagName);
  }
}