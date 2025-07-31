/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.api.repository2.unified.webservices;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;

@XmlRootElement( name = "repositoryFileDtoes" )
@XmlAccessorType( XmlAccessType.FIELD )
public class RepositoryFileDtoWrapper {

  @XmlElement( name = "repositoryFileDto" )
  private List<RepositoryFileDto> repositoryFileDto;

  public RepositoryFileDtoWrapper() {
  }

  public RepositoryFileDtoWrapper( List<RepositoryFileDto> repositoryFileDto ) {
    setRepositoryFileDto( repositoryFileDto );
  }

  public List<RepositoryFileDto> getRepositoryFileDto() {
    return repositoryFileDto;
  }

  public void setRepositoryFileDto( List<RepositoryFileDto> repositoryFileDto ) {
    // If the list is empty, set it to null to support old API responses
    if ( repositoryFileDto != null && repositoryFileDto.isEmpty() ) {
      repositoryFileDto = null;
    }

    this.repositoryFileDto = repositoryFileDto;
  }
}
