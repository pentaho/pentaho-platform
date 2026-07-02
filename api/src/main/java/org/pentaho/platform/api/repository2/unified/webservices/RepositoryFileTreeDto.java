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


package org.pentaho.platform.api.repository2.unified.webservices;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
@XmlRootElement
public class RepositoryFileTreeDto implements Serializable {
  private static final long serialVersionUID = -4222089807149018286L;

  private RepositoryFileDto file;

  private List<RepositoryFileTreeDto> children;

  public RepositoryFileTreeDto() {
  }

  public RepositoryFileDto getFile() {
    return file;
  }

  public void setFile( RepositoryFileDto file ) {
    this.file = file;
  }

  public List<RepositoryFileTreeDto> getChildren() {
    return children;
  }

  public void setChildren( List<RepositoryFileTreeDto> children ) {
    this.children = children;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "RepositoryFileTreeDto [file=" + file + ", children=" + children + "]";
  }

  public void afterUnmarshal( Unmarshaller unmarshaller, Object parent ) {
    if ( children == null ) {
      children = Collections.<RepositoryFileTreeDto>emptyList();
    }
  }
}
