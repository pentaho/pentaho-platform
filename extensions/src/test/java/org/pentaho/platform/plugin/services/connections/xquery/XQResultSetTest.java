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

package org.pentaho.platform.plugin.services.connections.xquery;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.StringValue;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XQResultSetTest {

  @Test
  public void testConvertToJavaPreservesNodeInfo() throws Exception {
    NodeInfo node = mock( NodeInfo.class );

    assertSame( node, XQResultSet.convertToJava( node ) );
  }

  @Test
  public void testConvertToJavaUnwrapsVirtualNode() throws Exception {
    VirtualNode node = mock( VirtualNode.class );
    Object underlyingNode = new Object();
    when( node.getUnderlyingNode() ).thenReturn( underlyingNode );

    assertSame( underlyingNode, XQResultSet.convertToJava( node ) );
  }

  @Test
  public void testConvertToJavaUnwrapsObjectValue() throws Exception {
    Object value = new Object();

    assertSame( value, XQResultSet.convertToJava( new ObjectValue<Object>( value ) ) );
  }

  @Test
  public void testConvertToJavaConvertsNumericValues() throws Exception {
    assertEquals( new BigDecimal( "12.34" ), XQResultSet.convertToJava( new BigDecimalValue( new BigDecimal( "12.34" ) ) ) );
    assertEquals( Long.valueOf( 42 ), XQResultSet.convertToJava( new Int64Value( 42 ) ) );
    assertEquals( Double.valueOf( 1.5D ), XQResultSet.convertToJava( new DoubleValue( 1.5D ) ) );
    assertEquals( Float.valueOf( 1.5F ), XQResultSet.convertToJava( new FloatValue( 1.5F ) ) );
  }

  @Test
  public void testConvertToJavaConvertsBooleanValue() throws Exception {
    assertEquals( Boolean.TRUE, XQResultSet.convertToJava( BooleanValue.TRUE ) );
  }

  @Test
  public void testConvertToJavaConvertsOtherAtomicValuesToStrings() throws Exception {
    assertEquals( "value", XQResultSet.convertToJava( new StringValue( "value" ) ) );
  }
}
