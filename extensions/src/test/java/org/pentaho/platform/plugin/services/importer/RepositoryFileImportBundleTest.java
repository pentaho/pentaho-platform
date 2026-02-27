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
