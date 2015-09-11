package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Definition of the export metadata required for a MetaStore.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ExportManifestMetaStore", propOrder = { "name", "description" } )
public class ExportManifestMetaStore {

  @XmlAttribute( name = "file" )
  protected String file;

  @XmlElement( name = "name" )
  protected String name;

  @XmlElement( name = "description" )
  protected String description;

  public ExportManifestMetaStore() {
  }

  public ExportManifestMetaStore( String file, String name, String description ) {
    this.description = description;
    this.file = file;
    this.name = name;
  }

  /**
   * Gets the description for the metastore
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description for the metastore
   * @param description
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * Get the file path (zip) that indicates where in the export zip the metastore content is located
   * @return
   */
  public String getFile() {
    return file;
  }

  /**
   * Set the path to where in the export zip the metastore content is located (path to the zipped metastore)
   * @param file
   */
  public void setFile( String file ) {
    this.file = file;
  }

  /**
   * Get the name of the metastore
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the metastore
   * @param name
   */
  public void setName( String name ) {
    this.name = name;
  }
}
