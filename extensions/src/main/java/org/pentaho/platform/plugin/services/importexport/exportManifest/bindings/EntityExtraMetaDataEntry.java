/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "EntityExtraMetaDataEntry", propOrder = { "name", "value" } )
public class EntityExtraMetaDataEntry {

  @XmlElement( name = "name" )
  String name;
  @XmlElement( name = "value" )
  String value;

  public EntityExtraMetaDataEntry() {
  }

  public EntityExtraMetaDataEntry( String name, String value ) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }
}
