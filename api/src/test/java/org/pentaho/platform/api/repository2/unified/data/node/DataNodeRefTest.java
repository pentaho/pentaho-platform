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


package org.pentaho.platform.api.repository2.unified.data.node;

import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataNodeRefTest {

  public static final String REF_ID = "refId";

  @Test
  public void testRef() {
    DataNodeRef ref = new DataNodeRef( REF_ID );
    assertEquals( REF_ID, ref.getId() );
    assertEquals( REF_ID, ref.toString() );

    System.out.println( ref.hashCode() );

    assertTrue( ref.equals( new DataNodeRef( REF_ID ) ) );
    assertTrue( ref.equals( ref ) );
    assertFalse( ref.equals( null ) );
    assertFalse( ref.equals( new DataNodeRef( "blah" ) ) );
    assertFalse( ref.equals( new String() ) );
  }
}
