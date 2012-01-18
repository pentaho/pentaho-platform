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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.importexport.RepositoryFileBundle;
import org.pentaho.platform.repository2.unified.importexport.StreamConverter;

public class MondrianCatalogRepositoryHelper {

  private final static String ETC_MONDRIAN_JCR_FOLDER = RepositoryFile.SEPARATOR + "etc" + RepositoryFile.SEPARATOR + "mondrian";

  private IUnifiedRepository repository;

  public MondrianCatalogRepositoryHelper(final IUnifiedRepository repository) {
    if (repository == null) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
  }

  public void addSchema(InputStream mondrianFile, String catalogName, String datasourceInfo) throws Exception {
    RepositoryFile catalog = createCatalog(catalogName, datasourceInfo);

    File tempFile = File.createTempFile("tempFile", null);
    tempFile.deleteOnExit();
    FileOutputStream outputStream = new FileOutputStream(tempFile);
    IOUtils.copy(mondrianFile, outputStream);

    RepositoryFile repoFile = new RepositoryFile.Builder("schema.xml").build();
    RepositoryFileBundle repoFileBundle = new RepositoryFileBundle(repoFile, null, ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR, tempFile, "UTF-8", "text/xml");

    RepositoryFile schema = repository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR + "schema.xml");
    IRepositoryFileData data = new StreamConverter().convert(repoFileBundle.getInputStream(), repoFileBundle.getCharset(), repoFileBundle.getMimeType());
    if (schema == null) {
      repository.createFile(catalog.getId(), repoFileBundle.getFile(), data, null);
    } else {
      repository.updateFile(schema, data, null);
    }
  }

  public Map<String, InputStream> getModrianSchemaFiles(String catalogName) {
    Map<String, InputStream> values = new HashMap<String, InputStream>();
    RepositoryFile catalogFolder = repository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName);
    for (RepositoryFile repoFile : repository.getChildren(catalogFolder.getId())) {
      RepositoryFileInputStream is;
      try {
        if (repoFile.getName().equals("metadata")) {
          continue;
        }
        is = new RepositoryFileInputStream(repoFile);
      } catch (Exception e) {
        return null;  // This pretty much ensures an exception will be thrown later and passed to the client
      }
      values.put(repoFile.getName(), is);
    }
    return values;
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

    RepositoryFile etcMondrian = repository.getFile(ETC_MONDRIAN_JCR_FOLDER);
    RepositoryFile catalog = repository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName);
    if (catalog == null) {
      catalog = repository.createFolder(etcMondrian.getId(), new RepositoryFile.Builder(catalogName).folder(true).build(), "");
    }
    createDatasourceMetadata(catalog, datasourceInfo);
    return catalog;
  }

  /*
    * Creates "/etc/mondrian/<catalog>/metadata" and the connection nodes
    */
  private void createDatasourceMetadata(RepositoryFile catalog, String datasourceInfo) {

    final String path = ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalog.getName() + RepositoryFile.SEPARATOR + "metadata";
    RepositoryFile metadata = repository.getFile(path);

    String definition = "mondrian:/" + catalog.getName();
    DataNode node = new DataNode("catalog");
    node.setProperty("definition", definition);
    node.setProperty("datasourceInfo", datasourceInfo);
    NodeRepositoryFileData data = new NodeRepositoryFileData(node);

    if (metadata == null) {
      repository.createFile(catalog.getId(), new RepositoryFile.Builder("metadata").build(), data, null);
    } else {
      repository.updateFile(metadata, data, null);
    }
  }

}
