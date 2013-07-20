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
 */
package org.pentaho.platform.repository2.unified.exportManifest;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestEntityDto;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.repository2.unified.exportManifest.bindings.Schedule;

/**
 * The Primary Object which represents the ExportManifest XML file by the same name 
 * stored in the Repository Export zip file during a repository export.
 * 
 * @author tkafalas
 */
public class ExportManifest {
  private HashMap<String, ExportManifestEntity> exportManifestEntities;

  private ExportManifestDto.ExportManifestInformation manifestInformation;
  private List<ExportManifestMetadata> metadataList = new ArrayList<ExportManifestMetadata>();
  private List<ExportManifestMondrian> mondrianList = new ArrayList<ExportManifestMondrian>();
  private List<Schedule> scheduleList = new ArrayList<Schedule>();

  public ExportManifest() {
    this.exportManifestEntities = new HashMap<String, ExportManifestEntity>();
    this.manifestInformation = new ExportManifestDto.ExportManifestInformation();
  }

  public ExportManifest(ExportManifestDto exportManifestDto) {
    this();
    this.manifestInformation = exportManifestDto.getExportManifestInformation();
    List<ExportManifestEntityDto> exportManifestEntityList = exportManifestDto.getExportManifestEntity();
    for (ExportManifestEntityDto exportManifestEntityDto : exportManifestEntityList) {
      exportManifestEntities.put(exportManifestEntityDto.getPath(), new ExportManifestEntity(exportManifestEntityDto));
    }
    this.mondrianList = exportManifestDto.getExportManifestMondrian();
    this.metadataList = exportManifestDto.getExportManifestMetadata();
    this.scheduleList = exportManifestDto.getExportManifestSchedule();
  }

  /**
   * 
   * @param repositoryFile
   * @param repositoryFileAcl
   * @throws ExportManifestFormatException if the RepositoryFile path does not start with the manifest's rootFolder of manifest is 
   */
  public void add(RepositoryFile repositoryFile, RepositoryFileAcl repositoryFileAcl)
      throws ExportManifestFormatException {
    ExportManifestEntity exportManifestEntity = new ExportManifestEntity(manifestInformation.getRootFolder(),
        repositoryFile, repositoryFileAcl);
    this.add(exportManifestEntity);
  }

  private void add(ExportManifestEntity exportManifestEntity) throws ExportManifestFormatException {
    if (exportManifestEntity.isValid()) {
      exportManifestEntities.put(exportManifestEntity.getPath(), exportManifestEntity);
    } else {
      throw new ExportManifestFormatException("Invalid Manifest Entry");
    }
  }

  public ExportManifestEntity getExportManifestEntity(String path) {
    return exportManifestEntities.get(path);
  }

  /**
   * Marshals the manifest object into xml on the given output stream
   * 
   * @param outputStream
   * @throws JAXBException
   * @throws ExportManifestFormatException 
   */
  public void toXml(OutputStream outputStream) throws JAXBException, ExportManifestFormatException {
    if (!isValid()) {
      throw new ExportManifestFormatException("Invalid root Folder for manifest");
    }
    final JAXBContext jaxbContext = JAXBContext.newInstance(ExportManifestDto.class);
    Marshaller marshaller = getMarshaller();
    marshaller.marshal(
        new JAXBElement<ExportManifestDto>(new QName("http://www.pentaho.com/schema/", "ExportManifest"),
            ExportManifestDto.class, getExportManifestDto()), outputStream);
  }

  public String toXmlString() throws JAXBException {
    StringWriter sw = new StringWriter();
    Marshaller marshaller = getMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(
        new JAXBElement<ExportManifestDto>(new QName("http://www.pentaho.com/schema/", "ExportManifest"),
            ExportManifestDto.class, getExportManifestDto()), sw);
    return sw.toString();
  }

  private Marshaller getMarshaller() throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(ExportManifestDto.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return marshaller;
  }

  public static ExportManifest fromXml(ByteArrayInputStream input) throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("org.pentaho.platform.repository2.unified.exportManifest.bindings");
    Unmarshaller u = jc.createUnmarshaller();

    try {
      JAXBElement<ExportManifestDto> o = (JAXBElement) (u.unmarshal(input));
      ExportManifestDto exportManifestDto = o.getValue();
      ExportManifest exportManifest = new ExportManifest(exportManifestDto);
      return exportManifest;
    } catch (Exception e) {
      System.out.println(e.toString());
      return null;
    }
  }

  ExportManifestDto getExportManifestDto() {
    ExportManifestDto rawExportManifest = new ExportManifestDto();
    List<ExportManifestEntityDto> rawEntityList = rawExportManifest.getExportManifestEntity();
    rawExportManifest.setExportManifestInformation(manifestInformation);
    TreeSet<String> ts = new TreeSet<String>(exportManifestEntities.keySet());
    for (String path : ts) {
      rawEntityList.add(exportManifestEntities.get(path).getExportManifestEntityDto());
    }

    rawExportManifest.getExportManifestMetadata().addAll(this.metadataList);
    rawExportManifest.getExportManifestMondrian().addAll(this.mondrianList);
    rawExportManifest.getExportManifestSchedule().addAll(this.scheduleList);

    return rawExportManifest;
  }

  /**
   * Factory method to deliver one ExportManifestEntity. The Manifest is built
   * by adding one ExportManifestEntity object for each file and folder in the
   * export set.
   * 
   * @return
   */
  public ExportManifestEntity createExportManifestEntry() {
    return new ExportManifestEntity();
  }

  public boolean isValid() {
    if (this.exportManifestEntities.size() > 0) {
      for (ExportManifestEntity manEntity : exportManifestEntities.values()) {
        if (!manEntity.isValid()) {
          return false;
        }
      }
      if (manifestInformation.getRootFolder() == null || manifestInformation.getRootFolder().length() == 0) {
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
   * @param manifestInformation
   *          the manifestInformation to set
   */
  public void setManifestInformation(ExportManifestDto.ExportManifestInformation manifestInformation) {
    this.manifestInformation = manifestInformation;
  }

  public ExportManifestDto.ExportManifestInformation createExportManifestInformation() {
    return new ExportManifestDto.ExportManifestInformation();
  }

  public void addMetadata(ExportManifestMetadata metadata){
    this.metadataList.add(metadata);
  }

  public void addMondrian(ExportManifestMondrian mondrian){
    this.mondrianList.add(mondrian);
  }
  
  public void addSchedule(Schedule schedule){
    this.scheduleList.add(schedule);
  }

  public List<ExportManifestMetadata> getMetadataList() {
    return metadataList;
  }

  public List<ExportManifestMondrian> getMondrianList() {
    return mondrianList;
  }
  
  public List<Schedule> getScheduleList() {
    return scheduleList;
  }
}
