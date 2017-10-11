/*
 * Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * This software was developed by Hitachi Vantara and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.plugin.services.importexport.legacy;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import static org.pentaho.platform.repository.solution.filebased.MondrianVfs.ANNOTATIONS_XML;
import static org.pentaho.platform.repository.solution.filebased.MondrianVfs.ANNOTATOR_KEY;
import static org.pentaho.platform.repository.solution.filebased.MondrianVfs.SCHEMA_XML;


public class MondrianCatalogRepositoryHelper {

  public static final String ETC_MONDRIAN_JCR_FOLDER =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "mondrian";
  public static final String ETC_OLAP_SERVERS_JCR_FOLDER =
      ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "olap-servers";
  private boolean isSecured = false;

  private IUnifiedRepository repository;

  public MondrianCatalogRepositoryHelper( final IUnifiedRepository repository ) {
    if ( repository == null ) {
      throw new IllegalArgumentException();
    }
    this.repository = repository;
    try {
      if ( PentahoSystem.get( IUserRoleListService.class ) != null ) {
        isSecured = true;
      }
    } catch ( Throwable t ) {
      // That's ok. The API throws an exception and there is no method to check
      // if security is on or off.
    }
    initOlapServersFolder();
  }

  @Deprecated
  public void addSchema( InputStream mondrianFile, String catalogName, String datasourceInfo ) throws Exception {
    this.addHostedCatalog( mondrianFile, catalogName, datasourceInfo );
  }

  public void addHostedCatalog( InputStream mondrianFile, String catalogName, String datasourceInfo ) throws Exception {
    RepositoryFile catalog = createCatalog( catalogName, datasourceInfo );

    File tempFile = File.createTempFile( "tempFile", null );
    tempFile.deleteOnExit();
    FileOutputStream outputStream = new FileOutputStream( tempFile );
    IOUtils.copy( mondrianFile, outputStream );

    RepositoryFile repoFile = new RepositoryFile.Builder( "schema.xml" ).build();
    org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle repoFileBundle =
        new org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle( repoFile, null,
            ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR, tempFile,
            "UTF-8", "text/xml" );

    RepositoryFile schema =
        repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName + RepositoryFile.SEPARATOR
            + "schema.xml" );
    IRepositoryFileData data =
        new StreamConverter().convert(
            repoFileBundle.getInputStream(), repoFileBundle.getCharset(), repoFileBundle.getMimeType() );
    if ( schema == null ) {
      RepositoryFile schemaFile = repository.createFile( catalog.getId(), repoFileBundle.getFile(), data, null );
      if ( schemaFile != null ) {
        // make sure the folder is not set to hidden if the schema is not hidden
        RepositoryFile catalogFolder =
          repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName );
        if ( catalogFolder.isHidden() != schemaFile.isHidden() ) {
          RepositoryFile unhiddenFolder =
            ( new RepositoryFile.Builder( catalogFolder ) ).hidden( schemaFile.isHidden() ).build();
          repository.updateFolder( unhiddenFolder, "" );
        }
      }
    } else {
      repository.updateFile( schema, data, null );
    }
  }

  public void deleteCatalog( String catalogName ) {
    deleteHostedCatalog( catalogName );
    deleteOlap4jServer( catalogName );
  }

  public void deleteHostedCatalog( String catalogName ) {

    final RepositoryFile catalogNode =
        repository.getFile(
            ETC_MONDRIAN_JCR_FOLDER
                + RepositoryFile.SEPARATOR
                + catalogName
      );

    if ( catalogNode != null ) {
      repository.deleteFile(
          catalogNode.getId(), true,
          "Deleting hosted catalog: "
              + catalogName
      );
    }
  }

  private void initOlapServersFolder() {
    final RepositoryFile etcOlapServers =
        repository.getFile( ETC_OLAP_SERVERS_JCR_FOLDER );
    if ( etcOlapServers == null ) {
      final Callable<Void> callable = new Callable<Void>() {
        public Void call() throws Exception {
          repository.createFolder(
              repository.getFile( RepositoryFile.SEPARATOR + "etc" ).getId(),
              new RepositoryFile.Builder( "olap-servers" )
                  .folder( true )
                  .build(),
              "Creating olap-servers directory in /etc"
          );
          return null;
        }
      };
      try {
        if ( isSecured ) {
          SecurityHelper.getInstance().runAsSystem( callable );
        } else {
          callable.call();
        }
      } catch ( Exception e ) {
        throw new RuntimeException(
            "Failed to create folder /etc/olap-servers in the repository.",
            e );
      }
    }
  }

  public void addOlap4jServer(
      String name,
      String className,
      String URL,
      String user,
      String password,
      Properties props ) {

    final RepositoryFile etcOlapServers =
        repository.getFile( ETC_OLAP_SERVERS_JCR_FOLDER );

    RepositoryFile entry =
        repository.getFile(
            ETC_OLAP_SERVERS_JCR_FOLDER
                + RepositoryFile.SEPARATOR
                + name
      );

    if ( entry == null ) {
      entry =
          repository.createFolder(
              etcOlapServers.getId(),
              new RepositoryFile.Builder( name )
                  .folder( true )
                  .build(),
              "Creating entry for olap server: "
                  + name
                  + " into folder "
                  + ETC_OLAP_SERVERS_JCR_FOLDER
        );
    }

    final String path =
        ETC_OLAP_SERVERS_JCR_FOLDER
            + RepositoryFile.SEPARATOR
            + name
            + RepositoryFile.SEPARATOR
            + "metadata";

    // Convert the properties to a serializable XML format.
    final String xmlProperties;
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      props.storeToXML(
          os,
          "Connection properties for server: " + name,
          "UTF-8" );
      xmlProperties =
          os.toString( "UTF-8" );
    } catch ( IOException e ) {
      // Very bad. Just throw.
      throw new RuntimeException( e );
    } finally {
      try {
        os.close();
      } catch ( IOException e ) {
        // Don't care. Just cleaning up.
      }
    }

    final DataNode node = new DataNode( "server" );
    node.setProperty( "name", name );
    node.setProperty( "className", className );
    node.setProperty( "URL", URL );
    node.setProperty( "user", user );
    node.setProperty( "password", password );
    node.setProperty( "properties", xmlProperties );
    NodeRepositoryFileData data = new NodeRepositoryFileData( node );

    final RepositoryFile metadata = repository.getFile( path );

    if ( metadata == null ) {
      repository.createFile(
          entry.getId(),
          new RepositoryFile.Builder( "metadata" ).build(),
          data,
          "Creating olap-server metadata for server "
              + name
      );
    } else {
      repository.updateFile(
          metadata,
          data,
          "Updating olap-server metadata for server "
              + name
      );
    }
  }

  public void deleteOlap4jServer( String name ) {
    // Get the /etc/olap-servers/[name] folder.
    final RepositoryFile serverNode =
        repository.getFile(
            ETC_OLAP_SERVERS_JCR_FOLDER
                + RepositoryFile.SEPARATOR
                + name
      );

    if ( serverNode != null ) {
      repository.deleteFile(
          serverNode.getId(), true,
          "Deleting olap server: "
              + name
      );
    }
  }

  /**
   * Provides a list of the catalog names which are not hosted on this server.
   * (generic olap4j connections)
   */
  public List<String> getOlap4jServers() {
    final RepositoryFile hostedFolder =
        repository.getFile( ETC_OLAP_SERVERS_JCR_FOLDER );

    if ( hostedFolder == null ) {
      // This can happen on old systems when this code first kicks in.
      // The folder gets created in addOlap4jServer
      return Collections.emptyList();
    }

    final List<String> names = new ArrayList<String>();

    for ( RepositoryFile repoFile : repository.getChildren( hostedFolder.getId() ) ) {
      names.add( repoFile.getName() );
    }

    return names;
  }

  public Olap4jServerInfo getOlap4jServerInfo( String name ) {
    final RepositoryFile serverNode =
        repository.getFile(
            ETC_OLAP_SERVERS_JCR_FOLDER
                + RepositoryFile.SEPARATOR
                + name
                + RepositoryFile.SEPARATOR
                + "metadata"
      );

    if ( serverNode != null ) {
      return new Olap4jServerInfo( serverNode );
    } else {
      return null;
    }
  }

  /**
   * Provides a list of the catalog names hosted locally on this server.
   */
  public List<String> getHostedCatalogs() {
    final List<String> names = new ArrayList<String>();

    final RepositoryFile serversFolder =
        repository.getFile( ETC_MONDRIAN_JCR_FOLDER );

    if ( serversFolder != null ) {
      for ( RepositoryFile repoFile : repository.getChildren( serversFolder.getId() ) ) {
        names.add( repoFile.getName() );
      }
    }
    return names;
  }

  public HostedCatalogInfo getHostedCatalogInfo( String name ) {
    final RepositoryFile catalogNode =
        repository.getFile(
            ETC_MONDRIAN_JCR_FOLDER
                + RepositoryFile.SEPARATOR
                + name
                + RepositoryFile.SEPARATOR
                + "metadata"
      );

    if ( catalogNode != null ) {
      return new HostedCatalogInfo( name, catalogNode );
    } else {
      return null;
    }
  }

  /**
   * Provides information on a catalog that the server hosts locally.
   */
  public final class HostedCatalogInfo {
    public final String name;
    public final String dataSourceInfo;
    public final String definition;

    public HostedCatalogInfo(
        String name,
        RepositoryFile source ) {
      final NodeRepositoryFileData data =
          repository.getDataForRead(
              source.getId(),
              NodeRepositoryFileData.class );
      this.name = name;
      this.dataSourceInfo =
          data.getNode().getProperty( "datasourceInfo" ).getString();
      this.definition =
          data.getNode().getProperty( "definition" ).getString();
    }

    public HostedCatalogInfo(
        String name,
        String dataSourceInfo,
        String definition ) {
      this.name = name;
      this.dataSourceInfo = dataSourceInfo;
      this.definition = definition;
    }
  }

  public final class Olap4jServerInfo {
    public final String name;
    public final String className;
    public final String URL;
    public final String user;
    public final String password;
    public final Properties properties;

    private Olap4jServerInfo( RepositoryFile source ) {

      final NodeRepositoryFileData data =
          repository.getDataForRead(
              source.getId(),
              NodeRepositoryFileData.class );

      this.name = data.getNode().getProperty( "name" ).getString();
      this.className = data.getNode().getProperty( "className" ).getString();
      this.URL = data.getNode().getProperty( "URL" ).getString();

      final DataProperty userProp = data.getNode().getProperty( "user" );
      this.user = userProp == null ? null : userProp.getString();

      final DataProperty passwordProp = data.getNode().getProperty( "password" );
      this.password = passwordProp == null ? null : passwordProp.getString();

      this.properties = new Properties();

      final String propertiesXml =
          data.getNode().getProperty( "properties" ).getString();
      try {
        properties.loadFromXML(
            new ByteArrayInputStream(
                propertiesXml.getBytes( "UTF-8" ) )
        );
      } catch ( Exception e ) {
        // Very bad.
        throw new RuntimeException( e );
      }
    }
  }

  public Map<String, InputStream> getModrianSchemaFiles( String catalogName ) {
    Map<String, InputStream> values = new HashMap<String, InputStream>();

    RepositoryFile catalogFolder =
        repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName );

    for ( RepositoryFile repoFile : repository.getChildren( catalogFolder.getId() ) ) {
      RepositoryFileInputStream is;
      if ( repoFile.getName().equals( "metadata" ) ) {
        continue;
      }
      try {
        is = new RepositoryFileInputStream( repoFile, repository );
      } catch ( FileNotFoundException e ) {
        throw new RepositoryException( e );
      }
      values.put( repoFile.getName(), is );
    }
    if ( values.containsKey( ANNOTATIONS_XML ) && values.containsKey( SCHEMA_XML ) ) {
      return includeAnnotatedSchema( values );
    }
    return values;
  }

  private Map<String, InputStream> includeAnnotatedSchema( final Map<String, InputStream> values ) {
    MondrianSchemaAnnotator annotator =
        PentahoSystem.get( MondrianSchemaAnnotator.class, ANNOTATOR_KEY, PentahoSessionHolder.getSession() );
    try {
      if ( annotator != null ) {
        byte[] schemaBytes = IOUtils.toByteArray( values.get( SCHEMA_XML ) );
        byte[] annotationBytes = IOUtils.toByteArray( values.get( ANNOTATIONS_XML ) );
        values.put( SCHEMA_XML, new ByteArrayInputStream( schemaBytes ) );
        values.put( ANNOTATIONS_XML, new ByteArrayInputStream( annotationBytes ) );
        values.put( "schema.annotated.xml",
            annotator.getInputStream(
              new ByteArrayInputStream( schemaBytes ), new ByteArrayInputStream( annotationBytes ) ) );
      }
    } catch ( IOException e ) {
      throw new RepositoryException( e );
    }
    return values;
  }

  public RepositoryFile getMondrianCatalogFile( String catalogName ) {
    return repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalogName );
  }

  /*
   * Creates "/etc/mondrian/<catalog>"
   */
  private RepositoryFile createCatalog( String catalogName, String datasourceInfo ) {

    /*
     * This is the default implementation. Use Schema name as defined in the mondrian.xml schema. Pending create
     * alternate implementation. Use catalog name.
     */

    RepositoryFile catalog = getMondrianCatalogFile( catalogName );
    if ( catalog == null ) {
      RepositoryFile etcMondrian = repository.getFile( ETC_MONDRIAN_JCR_FOLDER );
      catalog =
          repository.createFolder( etcMondrian.getId(), new RepositoryFile.Builder( catalogName ).folder( true )
              .build(), "" );
    }
    createDatasourceMetadata( catalog, datasourceInfo );
    return catalog;
  }

  /*
   * Creates "/etc/mondrian/<catalog>/metadata" and the connection nodes
   */
  private void createDatasourceMetadata( RepositoryFile catalog, String datasourceInfo ) {

    final String path =
        ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + catalog.getName() + RepositoryFile.SEPARATOR + "metadata";
    RepositoryFile metadata = repository.getFile( path );

    String definition = "mondrian:/" + catalog.getName();
    DataNode node = new DataNode( "catalog" );
    node.setProperty( "definition", encodeUrl( definition ) );
    node.setProperty( "datasourceInfo", datasourceInfo );
    NodeRepositoryFileData data = new NodeRepositoryFileData( node );

    if ( metadata == null ) {
      repository.createFile( catalog.getId(), new RepositoryFile.Builder( "metadata" ).build(), data, null );
    } else {
      repository.updateFile( metadata, data, null );
    }
  }

  private String makeHostedPath( String name ) {
    return
        MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
            + RepositoryFile.SEPARATOR
            + name;
  }

  private String makeGenericPath( String name ) {
    return
        MondrianCatalogRepositoryHelper.ETC_OLAP_SERVERS_JCR_FOLDER
            + RepositoryFile.SEPARATOR
            + name;
  }

  private boolean isHosted( String name ) {
    if ( getHostedCatalogs().contains( name ) ) {
      return true;
    } else {
      return false;
    }
  }

  private String makePath( String catalogName ) {
    if ( isHosted( catalogName ) ) {
      return makeHostedPath( catalogName );
    } else {
      return makeGenericPath( catalogName );
    }
  }

  public boolean hasAccess(
      final String catalogName,
      final EnumSet<RepositoryFilePermission> perms,
      IPentahoSession session ) {

    if ( session == null ) {
      // No session is equivalent to root access.
      return true;
    }

    // If the connection doesn't exist yet and we're trying to create it,
    // we need to check the parent folder instead.
    final String path;
    if ( !getHostedCatalogs().contains( catalogName )
        && !getOlap4jServers().contains( catalogName )
        && perms.contains( RepositoryFilePermission.WRITE ) ) {
      path = isHosted( catalogName )
          ? ETC_MONDRIAN_JCR_FOLDER
          : ETC_OLAP_SERVERS_JCR_FOLDER;
    } else {
      path = makePath( catalogName );
    }

    final IPentahoSession origSession = PentahoSessionHolder.getSession();
    PentahoSessionHolder.setSession( session );
    try {
      return repository.hasAccess(
          path,
          perms );
    } catch ( Exception e ) {
      throw new IOlapServiceException( e );
    } finally {
      PentahoSessionHolder.setSession( origSession );
    }
  }

  /**
   * Ensure URLs are properly encoded to accommodate
   *
   * @param urlStr
   * @return
   */
  private String encodeUrl( String urlStr ) {
    // make sure catalog definition url is properly encoded
    // try to encode the url before use
    String protocol = urlStr.substring( 0, urlStr.indexOf( ":" ) + 1 );
    String datasourcePath = urlStr.substring( protocol.length() );
    String[] folders = datasourcePath.split( "/" );
    StringBuilder encodedPath = new StringBuilder( urlStr.length() * 2 );
    for ( int i = 0; i < folders.length; i++ ) {
      String pathPart;

      try {
        final Charset urlCharset = Charset.forName( "UTF-8" );
        pathPart = URLEncoder.encode( folders[ i ], urlCharset.name() );
      } catch ( UnsupportedEncodingException e ) {
        pathPart = folders[i];
      }

      encodedPath.append( pathPart );

      if ( i != folders.length - 1 || urlStr.endsWith( "/" ) ) {
        encodedPath.append( "/" );
      }
    }

    return protocol + encodedPath.toString();
  }
}
