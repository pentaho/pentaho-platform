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


package org.pentaho.platform.config;

public interface IPdiConfig {
  public String getRepositoryType();

  public String getRepositoryName();

  public String getRepositoryUserId();

  public String getRepositoryPassword();

  public String getRepositoryXmlFile();

  public void setRepositoryType( String type );

  public void setRepositoryName( String name );

  public void setRepositoryUserId( String userId );

  public void setRepositoryPassword( String password );

  public void setRepositoryXmlFile( String xmlFile );
}
