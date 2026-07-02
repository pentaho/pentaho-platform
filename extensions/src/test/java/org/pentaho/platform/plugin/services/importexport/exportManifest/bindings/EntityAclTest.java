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


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/26/15.
 */
public class EntityAclTest {

  @Test
  public void testGetttersAndSetter() throws Exception {
    String[] excludes = new String[] {
      "aces"
    };
    assertThat( EntityAcl.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testGetAces() throws Exception {
    EntityAcl entityAcl = new EntityAcl();
    assertNotNull( entityAcl.getAces() );
  }

  @Test
  public void testInnerClass() throws Exception {
    String[] excludes = new String[] {
      "permissions"
    };
    assertThat( EntityAcl.Aces.class, hasValidGettersAndSettersExcluding( excludes ) );
    EntityAcl.Aces aces = new EntityAcl.Aces();
    assertNotNull( aces.getPermissions() );
  }
}
