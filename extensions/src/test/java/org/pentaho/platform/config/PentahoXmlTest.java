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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.*;
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
    List<Element> elements = new ArrayList<>();
    Element e1 = mock( Element.class );
    Element e2 = mock( Element.class );
    when( e1.attributeValue( "role" ) ).thenReturn( "scrum master" );
    when( e2.attributeValue( "role" ) ).thenReturn( "developer" );
    when( e1.attributeValue( "acl" ) ).thenReturn( "read" );
    when( e2.attributeValue( "acl" ) ).thenReturn( "execute" );
    elements.add( e1 );
    elements.add( e2 );

    when( document.selectNodes( "pentaho-system/acl-publisher/default-acls/acl-entry" ) ).thenReturn( elements );

    List<AclEntry> defaultAcls = pentahoXml.getDefaultAcls();
    assertNotNull( defaultAcls );
    assertEquals( elements.size(), defaultAcls.size() );
    assertEquals( e1.attributeValue( "role" ), defaultAcls.get( 0 ).getPrincipalName() );
    assertEquals( e2.attributeValue( "role" ), defaultAcls.get( 1 ).getPrincipalName() );
    assertEquals( e1.attributeValue( "acl" ), defaultAcls.get( 0 ).getPermission() );
    assertEquals( e2.attributeValue( "acl" ), defaultAcls.get( 1 ).getPermission() );
  }
}
