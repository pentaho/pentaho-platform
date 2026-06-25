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



package com.pentaho.pdi.ws;

import jakarta.jws.WebService;

@WebService
public interface IRepositorySyncWebService {
  public RepositorySyncStatus sync( String repositoryId, String repositoryUrl ) throws RepositorySyncException;

  public default void logout() { }
}
