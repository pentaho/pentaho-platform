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

import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.File;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/21/15.
 */
public class PdiConfigXmlTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludeProperties = new String[] {
      "properties"
    };
    assertThat( PdiConfigXml.class, hasValidGettersAndSettersExcluding( excludeProperties ) );
  }

  @Test
  public void testConstructor_IPdiConfig() throws Exception {
    IPdiConfig mockConfig = mock( IPdiConfig.class );

    when( mockConfig.getRepositoryName() ).thenReturn( "repo name" );
    when( mockConfig.getRepositoryPassword() ).thenReturn( "p@$$w0rd" );
    when( mockConfig.getRepositoryType() ).thenReturn( "carte" );
    when( mockConfig.getRepositoryUserId() ).thenReturn( "admin" );
    when( mockConfig.getRepositoryXmlFile() ).thenReturn( "file.xml" );

    PdiConfigXml pdiConfigXml = new PdiConfigXml( mockConfig );

    assertNotNull( pdiConfigXml );
    assertEquals( mockConfig.getRepositoryName(), pdiConfigXml.getRepositoryName() );
    assertEquals( mockConfig.getRepositoryPassword(), pdiConfigXml.getRepositoryPassword() );
    assertEquals( mockConfig.getRepositoryType(), pdiConfigXml.getRepositoryType() );
    assertEquals( mockConfig.getRepositoryUserId(), pdiConfigXml.getRepositoryUserId() );
    assertEquals( mockConfig.getRepositoryXmlFile(), pdiConfigXml.getRepositoryXmlFile() );
  }

  @Test ( expected = DocumentException.class )
  public void testConstructor_String() throws Exception {
    PdiConfigXml pdiConfigXml = new PdiConfigXml( "<xml></xml>" );
  }

  @Test ( expected = DocumentException.class )
  public void testConstructor_File() throws Exception {
    PdiConfigXml pdiConfigXml = new PdiConfigXml( new File( "config.xml" ) );
  }
}
