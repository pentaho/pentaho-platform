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


package org.pentaho.platform.plugin.services.importexport.legacy;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository.RepositoryException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER;

public class MondrianCatalogRepositoryHelperTest {

  MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;
  IUnifiedRepository repository;

  @Before
  public void setUp() {
    repository = mock( IUnifiedRepository.class );
    RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repository.getFile( nullable( String.class ) ) ).thenReturn( repositoryFile );
    mondrianCatalogRepositoryHelper = new MondrianCatalogRepositoryHelper( repository );
  }

  @Test( expected = RepositoryException.class )
  public void testGetModrianSchemaFilesInvalidCatalogName() {
    String testCatalogName = "testCatalogName";
    when( repository.getFile( ETC_MONDRIAN_JCR_FOLDER + RepositoryFile.SEPARATOR + testCatalogName ) ).thenReturn( null );
    mondrianCatalogRepositoryHelper.getModrianSchemaFiles( testCatalogName );
  }
}
