/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

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
