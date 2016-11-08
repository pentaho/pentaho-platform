/*!
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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/22/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class SpringSecurityHibernateConfigTest {

  SpringSecurityHibernateConfig config;

  @Mock Document document;

  @Test
  public void testGetPasswordEncoder() throws Exception {
    config = new SpringSecurityHibernateConfig( document );
    Node node = mock( Node.class );
    when( node.getText() ).thenReturn( DefaultPentahoPasswordEncoder.class.getName() );
    when( document.selectSingleNode( anyString() ) ).thenReturn( node );

    PasswordEncoder passwordEncoder = config.getPasswordEncoder();
    assertTrue( passwordEncoder instanceof DefaultPentahoPasswordEncoder );
  }

  @Test
  public void testGetPasswordEncoder_ClassNotFound() throws Exception {
    config = new SpringSecurityHibernateConfig( document );
    Node node = mock( Node.class );
    when( node.getText() ).thenReturn( "org.pentaho.ClassNotFoundEncoder" );
    when( document.selectSingleNode( anyString() ) ).thenReturn( node );

    PasswordEncoder passwordEncoder = config.getPasswordEncoder();
    assertNull( passwordEncoder );
  }

  @Test
  public void testGetDocument() throws Exception {
    config = new SpringSecurityHibernateConfig( document );
    assertEquals( document, config.getDocument() );
  }

  @Test ( expected = NullPointerException.class )
  public void testInvalidConstructor_nullFile() throws Exception {
    File file = null;
    config = new SpringSecurityHibernateConfig( file );
  }

  @Test
  public void testConstructor_xmlString() throws Exception {
    config = new SpringSecurityHibernateConfig( "<beans>no beans</beans>" );
    assertNotNull( config );
    assertNotNull( config.getDocument() );
  }
}
