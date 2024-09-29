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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importer;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.EntityExtraMetaDataEntry;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.mockito.Mockito.mock;

public class RepositoryFileImportBundleTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    final String[] excludes = new String[] { "childBundles", "folder", "hidden", "schedulable"};
    Assert.assertThat( RepositoryFileImportBundle.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testBuilder() throws Exception {
    RepositoryFileExtraMetaData repositoryFileExtraMetaData = mock( RepositoryFileExtraMetaData.class );
    RepositoryFileImportBundle.Builder builder = new RepositoryFileImportBundle.Builder();
    builder.extraMetaData( repositoryFileExtraMetaData );
    builder.mime( "text/directory" );

    RepositoryFileImportBundle repositoryFileImportBundle = builder.build();
    Assert.assertEquals( repositoryFileExtraMetaData, repositoryFileImportBundle.getExtraMetaData() );
  }
}
