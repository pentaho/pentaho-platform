/*!
 *
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
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.scheduler2.IJobScheduleRequest;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestEntityDto;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * The Primary Object which represents the ExportManifest XML file by the same name stored in the Repository Export zip
 * file during a repository export.
 *
 * @author tkafalas
 */
public class ExportManifest {
  private HashMap<String, ExportManifestEntity> exportManifestEntities;

  private ExportManifestDto.ExportManifestInformation manifestInformation;
  private List<ExportManifestMetadata> metadataList = new ArrayList<>();
  private List<ExportManifestMondrian> mondrianList = new ArrayList<>();
  private List<IJobScheduleRequest> scheduleList = new ArrayList<>();
  private List<DatabaseConnection> datasourceList = new ArrayList<>();
  private List<UserExport> userExports = new ArrayList<>();
  private List<RoleExport> roleExports = new ArrayList<>();
  private List<ExportManifestUserSetting> globalUserSettings = new ArrayList<>();
  private ExportManifestMetaStore metaStore;

  public ExportManifest() {
    this.exportManifestEntities = new HashMap<>();
    this.manifestInformation = new ExportManifestDto.ExportManifestInformation();
    IJobScheduleRequest request;
  }

  public ExportManifest( ExportManifestDto exportManifestDto ) {
    this();
    manifestInformation = exportManifestDto.getExportManifestInformation();
    List<ExportManifestEntityDto> exportManifestEntityList = exportManifestDto.getExportManifestEntity();
    for ( ExportManifestEntityDto exportManifestEntityDto : exportManifestEntityList ) {
      exportManifestEntities
        .put( exportManifestEntityDto.getPath(), new ExportManifestEntity( exportManifestEntityDto ) );
    }
    mondrianList = exportManifestDto.getExportManifestMondrian();
    metadataList = exportManifestDto.getExportManifestMetadata();
    scheduleList = ExportManifestUtil.fromBindingToSchedulerRequest( exportManifestDto.getExportManifestSchedule() );
    datasourceList = exportManifestDto.getExportManifestDatasource();
    userExports = exportManifestDto.getExportManifestUser();
    roleExports = exportManifestDto.getExportManifestRole();
    globalUserSettings = exportManifestDto.getGlobalUserSettings();
    setMetaStore( exportManifestDto.getExportManifestMetaStore() );
  }

  /**
   * @param repositoryFile
   * @param repositoryFileAcl
   * @throws ExportManifestFormatException if the RepositoryFile path does not start with the manifest's rootFolder of manifest is
   */
  public void add( RepositoryFile repositoryFile, RepositoryFileAcl repositoryFileAcl )
    throws ExportManifestFormatException {
    ExportManifestEntity exportManifestEntity =
      new ExportManifestEntity( manifestInformation.getRootFolder(), repositoryFile, repositoryFileAcl );
    this.add( exportManifestEntity );
  }

  public void add( File file, String userId, String projectId, Boolean isFolder, Boolean isHidden,
                   Boolean isSchedulable ) throws ExportManifestFormatException {
    ExportManifestEntity exportManifestEntity =
      new ExportManifestEntity( file, userId, projectId, isFolder, isHidden, isSchedulable );
    this.add( exportManifestEntity );
  }

  private void add( ExportManifestEntity exportManifestEntity ) throws ExportManifestFormatException {
    if ( exportManifestEntity.isValid() ) {
      exportManifestEntities.put( exportManifestEntity.getPath(), exportManifestEntity );
    } else {
      throw new ExportManifestFormatException( "Invalid Manifest Entry" );
    }
  }

  /**
   * Return entire map of export manifest entities
   *
   * @return
   */
  public HashMap<String, ExportManifestEntity> getExportManifestEntities() {
    return exportManifestEntities;
  }

  public ExportManifestEntity getExportManifestEntity( String path ) {
    return exportManifestEntities.get( path );
  }

  /**
   * Marshals the manifest object into xml on the given output stream
   *
   * @param outputStream
   * @throws JAXBException
   * @throws ExportManifestFormatException
   */
  public void toXml( OutputStream outputStream ) throws JAXBException, ExportManifestFormatException {
    if ( !isValid() ) {
      throw new ExportManifestFormatException( "Invalid root Folder for manifest" );
    }
    getMarshaller().marshal( new JAXBElement<ExportManifestDto>( new QName( "http://www.pentaho.com/schema/",
      "ExportManifest" ), ExportManifestDto.class, getExportManifestDto() ), outputStream );
  }

  public String toXmlString() throws JAXBException {
    StringWriter sw = new StringWriter();
    Marshaller marshaller = getMarshaller();
    marshaller.marshal( new JAXBElement<ExportManifestDto>( new QName( "http://www.pentaho.com/schema/",
      "ExportManifest" ), ExportManifestDto.class, getExportManifestDto() ), sw );
    return sw.toString();
  }

  private Marshaller getMarshaller() throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance( ExportManifestDto.class );
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    return marshaller;
  }

  public static ExportManifest fromXml( ByteArrayInputStream input ) throws JAXBException {
    SAXParserFactory secureSAXParserFactory;
    try {
      secureSAXParserFactory = XMLParserFactoryProducer.createSecureSAXParserFactory();
    } catch ( SAXNotSupportedException | SAXNotRecognizedException | ParserConfigurationException ex ) {
      throw new JAXBException( ex );
    }
    XMLReader xmlReader;
    try {
      xmlReader = secureSAXParserFactory.newSAXParser().getXMLReader();
      xmlReader.setFeature( "http://xml.org/sax/features/namespaces", true );
    } catch ( SAXException | ParserConfigurationException ex ) {
      throw new JAXBException( ex );
    }
    Source xmlSource = new SAXSource( xmlReader, new InputSource( input ) );

    JAXBContext jc =
      JAXBContext.newInstance( "org.pentaho.platform.plugin.services.importexport.exportManifest.bindings" );
    Unmarshaller u = jc.createUnmarshaller();

    try {
      JAXBElement<ExportManifestDto> o = (JAXBElement) ( u.unmarshal( xmlSource ) );
      ExportManifestDto exportManifestDto = o.getValue();
      ExportManifest exportManifest = new ExportManifest( exportManifestDto );
      return exportManifest;
    } catch ( Exception e ) {
      System.out.println( e.toString() );
      return null;
    }
  }

  ExportManifestDto getExportManifestDto() {
    ExportManifestDto rawExportManifest = new ExportManifestDto();
    List<ExportManifestEntityDto> rawEntityList = rawExportManifest.getExportManifestEntity();
    rawExportManifest.setExportManifestInformation( manifestInformation );
    TreeSet<String> ts = new TreeSet<>( exportManifestEntities.keySet() );
    for ( String path : ts ) {
      rawEntityList.add( exportManifestEntities.get( path ).getExportManifestEntityDto() );
    }

    rawExportManifest.getExportManifestMetadata().addAll( this.metadataList );
    rawExportManifest.getExportManifestMondrian().addAll( this.mondrianList );
    rawExportManifest.getExportManifestSchedule().addAll( ExportManifestUtil.fromSchedulerToBindingRequest( this.scheduleList ) );
    rawExportManifest.getExportManifestDatasource().addAll( this.datasourceList );
    rawExportManifest.getExportManifestUser().addAll( this.getUserExports() );
    rawExportManifest.getExportManifestRole().addAll( this.getRoleExports() );
    rawExportManifest.setExportManifestMetaStore( this.getMetaStore() );
    rawExportManifest.getGlobalUserSettings().addAll( this.getGlobalUserSettings() );

    return rawExportManifest;
  }

  /**
   * Factory method to deliver one ExportManifestEntity. The Manifest is built by adding one ExportManifestEntity object
   * for each file and folder in the export set.
   *
   * @return
   */
  public ExportManifestEntity createExportManifestEntry() {
    return new ExportManifestEntity();
  }

  public boolean isValid() {
    if ( !this.exportManifestEntities.isEmpty() ) {
      for ( ExportManifestEntity manEntity : exportManifestEntities.values() ) {
        if ( !manEntity.isValid() ) {
          return false;
        }
      }
      if ( StringUtil.isEmpty( manifestInformation.getRootFolder() ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return the manifestInformation
   */
  public ExportManifestDto.ExportManifestInformation getManifestInformation() {
    return manifestInformation;
  }

  /**
   * @param manifestInformation the manifestInformation to set
   */
  public void setManifestInformation( ExportManifestDto.ExportManifestInformation manifestInformation ) {
    this.manifestInformation = manifestInformation;
  }

  public ExportManifestDto.ExportManifestInformation createExportManifestInformation() {
    return new ExportManifestDto.ExportManifestInformation();
  }

  public void addMetadata( ExportManifestMetadata metadata ) {
    this.metadataList.add( metadata );
  }

  public void addMondrian( ExportManifestMondrian mondrian ) {
    this.mondrianList.add( mondrian );
  }

  public void addSchedule( IJobScheduleRequest schedule ) {
    this.scheduleList.add( schedule );
  }

  public void addDatasource( DatabaseConnection connection ) {
    this.datasourceList.add( connection );
  }

  public List<ExportManifestMetadata> getMetadataList() {
    return metadataList;
  }

  public List<ExportManifestMondrian> getMondrianList() {
    return mondrianList;
  }

  public List<IJobScheduleRequest> getScheduleList() {
    return scheduleList;
  }

  public List<DatabaseConnection> getDatasourceList() {
    return datasourceList;
  }

  public List<UserExport> getUserExports() {
    return userExports;
  }

  public void addUserExport( UserExport userExport ) {
    this.userExports.add( userExport );
  }

  public List<RoleExport> getRoleExports() {
    return roleExports;
  }

  public void addRoleExport( RoleExport roleExport ) {
    this.roleExports.add( roleExport );
  }

  public void setMetaStore( ExportManifestMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public ExportManifestMetaStore getMetaStore() {
    return metaStore;
  }

  public List<ExportManifestUserSetting> getGlobalUserSettings() {
    return globalUserSettings;
  }

  public void addGlobalUserSetting( ExportManifestUserSetting globalSetting ) {
    globalUserSettings.add( globalSetting );
  }
}
