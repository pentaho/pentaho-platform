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


package org.pentaho.mantle.client;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.mantle.client.objects.UserPermission;

public class NothingTest extends TestCase {

  public void testNothing() {

    Assert.assertTrue( true );
    UserPermission userPerm = new UserPermission();
    Assert.assertNotNull( userPerm.toString() );
  }

}
