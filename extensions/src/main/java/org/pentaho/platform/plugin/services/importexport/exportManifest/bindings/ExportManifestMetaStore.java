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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
