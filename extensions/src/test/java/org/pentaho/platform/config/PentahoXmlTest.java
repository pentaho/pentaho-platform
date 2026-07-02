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


package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class PentahoXmlTest {

  PentahoXml pentahoXml;

  @Mock Document document;

  @Before
  public void setUp() throws Exception {
    pentahoXml = new PentahoXml( document );
  }

  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludeProperties = new String[] {
      "document",
      "defaultAcls"
    };
    assertThat( PentahoXml.class, hasValidGettersAndSettersExcluding( excludeProperties ) );
  }

  @Test
  public void testGetDefaultAcls() throws Exception {
    List<Node> nodes = new ArrayList<>();
    DefaultElement e1 = mock( DefaultElement.class );
    DefaultElement e2 = mock( DefaultElement.class );
    when( e1.attributeValue( "role" ) ).thenReturn( "scrum master" );
    when( e2.attributeValue( "role" ) ).thenReturn( "developer" );
    when( e1.attributeValue( "acl" ) ).thenReturn( "read" );
    when( e2.attributeValue( "acl" ) ).thenReturn( "execute" );
    when( e1.getNodeType() ).thenReturn( Node.ELEMENT_NODE );
    when( e2.getNodeType() ).thenReturn( Node.ELEMENT_NODE );

    nodes.add( e1 );
    nodes.add( e2 );

    when( document.selectNodes( "pentaho-system/acl-publisher/default-acls/acl-entry" ) ).thenReturn( nodes );

    List<AclEntry> defaultAcls = pentahoXml.getDefaultAcls();
    assertNotNull( defaultAcls );
    assertEquals( nodes.size(), defaultAcls.size() );
    assertEquals( e1.attributeValue( "role" ), defaultAcls.get( 0 ).getPrincipalName() );
    assertEquals( e2.attributeValue( "role" ), defaultAcls.get( 1 ).getPrincipalName() );
    assertEquals( e1.attributeValue( "acl" ), defaultAcls.get( 0 ).getPermission() );
    assertEquals( e2.attributeValue( "acl" ), defaultAcls.get( 1 ).getPermission() );
  }
}
