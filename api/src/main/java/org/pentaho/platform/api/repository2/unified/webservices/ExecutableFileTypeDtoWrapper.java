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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement( name = "executableFileTypeDtoes" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ExecutableFileTypeDtoWrapper {

  @XmlElement( name = "executableFileTypeDto" )
  private ArrayList<ExecutableFileTypeDto> executableFileTypeDtoes;

  public ExecutableFileTypeDtoWrapper() {

  }

  public ExecutableFileTypeDtoWrapper( ArrayList<ExecutableFileTypeDto> executableFileTypeDtoes ) {
    this.executableFileTypeDtoes = executableFileTypeDtoes;
  }

  public ArrayList<ExecutableFileTypeDto> getExecutableFileTypeDtoes() {
    return executableFileTypeDtoes;
  }

  public void setExecutableFileTypeDtoes( ArrayList<ExecutableFileTypeDto> executableFileTypeDtoes ) {
    this.executableFileTypeDtoes = executableFileTypeDtoes;
  }
}
