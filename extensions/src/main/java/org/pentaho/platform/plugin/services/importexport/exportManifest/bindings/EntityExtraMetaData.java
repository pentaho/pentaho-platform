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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "EntityExtraMetaData" )
public class EntityExtraMetaData {

  @XmlElement( name = "metadata" )
  List<EntityExtraMetaDataEntry> metadata = new ArrayList<>();

  public void addMetadata( EntityExtraMetaDataEntry entry ) {
    this.metadata.add( entry );
  }

  public List<EntityExtraMetaDataEntry> getMetadata() {
    return metadata;
  }

  public void setMetadata( List<EntityExtraMetaDataEntry> metadata ) {
    this.metadata = metadata;
  }

}
