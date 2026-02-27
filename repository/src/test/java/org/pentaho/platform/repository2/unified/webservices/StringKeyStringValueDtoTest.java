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


package org.pentaho.platform.repository2.unified.webservices;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.webservices.StringKeyStringValueDto;

/**
 * Test class for the KeyStringKeyValueDto class
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class StringKeyStringValueDtoTest extends TestCase {
  @Test
  public void testInitialization() {
    final StringKeyStringValueDto empty = new StringKeyStringValueDto();
    assertNull( empty.getKey() );
    assertNull( empty.getValue() );
    assertTrue( empty.equals( new StringKeyStringValueDto() ) );
    assertEquals( empty.hashCode(), new StringKeyStringValueDto().hashCode() );
    assertFalse( StringUtils.isEmpty( empty.toString() ) );

    final StringKeyStringValueDto sample1 = new StringKeyStringValueDto();
    sample1.setKey( "test key" );
    sample1.setValue( "test value" );
    final StringKeyStringValueDto sample2 = new StringKeyStringValueDto( "test key", "test value" );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( null );
    sample2.setKey( null );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( sample1.getValue() );
    sample1.setValue( null );
    sample2.setKey( sample2.getValue() );
    sample2.setValue( null );
    assertTrue( sample1.equals( sample2 ) );
    assertEquals( sample1.hashCode(), sample2.hashCode() );

    sample1.setKey( null );
    assertTrue( empty.equals( sample1 ) );
    assertEquals( empty.hashCode(), sample1.hashCode() );

    sample1.setValue( "test" );
    assertFalse( empty.equals( sample1 ) );
    assertFalse( empty.equals( sample2 ) );
    assertFalse( empty.hashCode() == sample1.hashCode() );
    assertFalse( empty.hashCode() == sample2.hashCode() );

    assertFalse( empty.equals( null ) );
    assertFalse( empty.equals( sample1.toString() ) );
  }
}
