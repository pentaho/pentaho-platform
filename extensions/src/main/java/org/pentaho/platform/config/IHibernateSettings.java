/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.config;

public interface IHibernateSettings {

  public String getHibernateConfigFile();

  public void setHibernateConfigFile( String hibernateConfigFile );

  public boolean getHibernateManaged();

  public void setHibernateManaged( boolean hibernateManaged );
}
