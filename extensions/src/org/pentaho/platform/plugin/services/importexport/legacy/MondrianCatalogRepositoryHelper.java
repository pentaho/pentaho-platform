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

package org.pentaho.platform.plugin.services.importexport.legacy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;

public class MondrianCatalogRepositoryHelper {

  private final static String ETC_MONDRIAN_JCR_FOLDER = RepositoryFile.SEPARATOR + "etc" + RepositoryFile.SEPARATOR + "mondrian";
  private final static String ETC_OLAP_SERVERS_JCR_FOLDER = RepositoryFile.SEPARATOR + "etc" + RepositoryFile.SEPARATOR + "olap-servers";

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
    org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle repoFileBundle = new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle(repoFile, null, ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR, tempFile, "UTF-8", "text/xml");

    RepositoryFile schema = repository.getFile(ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR + "schema.xml");
    IRepositoryFileData data = new org.pentaho.platform.plugin.services.importexport.StreamConverter().convert(repoFileBundle.getInputStream(), repoFileBundle.getCharset(), repoFileBundle.getMimeType());
    if (schema == null) {
      repository.createFile(catalog.getId(), repoFileBundle.getFile(), data, null);
    } else {
      repository.updateFile(schema, data, null);
    }
  }

  public void addOlapServer(
      String name,
      String className,
      String URL,
      String user,
      String password,
      Properties props)
  {
      // Get the /etc/olap-servers folder.
      // Create it if necessary.
      RepositoryFile etcOlapServers =
        repository.getFile(ETC_OLAP_SERVERS_JCR_FOLDER);
      if (etcOlapServers == null) {
        etcOlapServers =
          repository.createFolder(
                    repository.getFile(RepositoryFile.SEPARATOR + "etc").getId(),
                    new RepositoryFile.Builder("olap-servers")
                        .folder(true)
                        .build(),
                    "Creating olap-servers directory in /etc");
      }
      RepositoryFile entry =
        repository.getFile(ETC_OLAP_SERVERS_JCR_FOLDER + RepositoryFile.SEPARATOR + name);
      if (entry == null) {
        entry =
          repository.createFolder(
            etcOlapServers.getId(),
            new RepositoryFile.Builder(name)
              .folder(true)
              .build(),
            "Creating entry for olap server: "
              + name
              + " into folder "
              + ETC_OLAP_SERVERS_JCR_FOLDER);
      }

      final String path =
        ETC_OLAP_SERVERS_JCR_FOLDER
        + RepositoryFile.SEPARATOR
        + name
        + RepositoryFile.SEPARATOR
        + "metadata";

      // Convert the properties to a serializable XML format.
      final String xmlProperties;
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
          props.storeToXML(
              os,
              "Connection properties for server: " + name,
              "UTF-8");
          xmlProperties =
              os.toString("UTF-8");
      } catch (IOException e) {
          // Very bad. Just throw.
          throw new RuntimeException(e);
      } finally {
          try {
              os.close();
          } catch (IOException e) {
              // Don't care. Just cleaning up.
          }
      }

      DataNode node = new DataNode("server");
      node.setProperty("name", name);
      node.setProperty("className", className);
      node.setProperty("URL", URL);
      node.setProperty("user", user);
      node.setProperty("password", password);
      node.setProperty("properties", xmlProperties);
      NodeRepositoryFileData data = new NodeRepositoryFileData(node);

      RepositoryFile metadata = repository.getFile(path);

      if (metadata == null) {
        repository.createFile(
            entry.getId(),
            new RepositoryFile.Builder("metadata").build(),
            data,
            "Creating olap-server metadata for server "
            + name);
      } else {
        repository.updateFile(
            metadata,
            data,
            "Updating olap-server metadata for server "
            + name);
      }
  }

  public void deleteOlapServer(String name) {
      // Get the /etc/olap-servers/[name] folder.
      // Create it if necessary.
      RepositoryFile serverNode =
        repository.getFile(
            ETC_OLAP_SERVERS_JCR_FOLDER
            + RepositoryFile.SEPARATOR
            + name);

      if (serverNode != null) {
        repository.deleteFile(serverNode,
            "Deleting olap server: " + name);
      }
  }

  public List<String> getOlapServers() {
      return getOlapServers(false);
  }

  public List<String> getOlapServers(boolean hostedOnly) {
      List<String> names = new ArrayList<>();

      RepositoryFile hostedFolder =
          repository.getFile(ETC_MONDRIAN_JCR_FOLDER);
      for (RepositoryFile repoFile : repository.getChildren(hostedFolder.getId())) {
          names.add(repoFile.getName());
      }

      if (hostedOnly) {
          return names;
      }

      RepositoryFile serversFolder =
          repository.getFile(ETC_OLAP_SERVERS_JCR_FOLDER);
      if (serversFolder != null) {
          for (RepositoryFile repoFile : repository.getChildren(serversFolder.getId())) {
              names.add(repoFile.getName());
          }
      }
      return names;
  }

  public Olap4jServerInfo getOlap4jServerInfo(String name) {
      RepositoryFile serverNode =
        repository.getFile(
          ETC_OLAP_SERVERS_JCR_FOLDER
            + RepositoryFile.SEPARATOR
            + name
            + RepositoryFile.SEPARATOR
            + "metadata");

      if (serverNode != null) {
        return new Olap4jServerInfo(serverNode);
      } else {
          return null;
      }
  }

  public final class Olap4jServerInfo {
      public final String name;
      public final String className;
      public final String URL;
      public final String user;
      public final String password;
      public final Properties properties;
      private Olap4jServerInfo(RepositoryFile source) {
          final NodeRepositoryFileData data =
              repository.getDataForRead(
                  source.getId(),
                  NodeRepositoryFileData.class);

          this.name = data.getNode().getProperty("name").getString();
          this.className = data.getNode().getProperty("className").getString();
          this.URL = data.getNode().getProperty("URL").getString();
          this.user = data.getNode().getProperty("user").getString();
          this.password = data.getNode().getProperty("password").getString();
          this.properties = new Properties();

          final String propertiesXml =
              data.getNode().getProperty("properties").getString();
          try {
            properties.loadFromXML(
              new ByteArrayInputStream(
                propertiesXml.getBytes("UTF-8")));
        } catch (Exception e) {
            // Very bad.
            throw new RuntimeException(e);
        }
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
       * catalog name.
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
