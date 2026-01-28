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


package org.pentaho.platform.repository.webservices;

import jakarta.jws.WebService;
import java.util.List;

@WebService
public interface IDatasourceMgmtWebService {

  public String createDatasource( DatabaseConnectionDto databaseConnection );

  public void deleteDatasourceByName( String name );

  public DatabaseConnectionDto getDatasourceByName( String name );

  public List<DatabaseConnectionDto> getDatasources();

  public String updateDatasourceByName( String name, DatabaseConnectionDto databaseConnection );

  public void deleteDatasourceById( String id );

  public DatabaseConnectionDto getDatasourceById( String id );

  public List<String> getDatasourceIds();

  public String updateDatasourceById( String id, DatabaseConnectionDto databaseConnection );

  public default void logout() { }

}
