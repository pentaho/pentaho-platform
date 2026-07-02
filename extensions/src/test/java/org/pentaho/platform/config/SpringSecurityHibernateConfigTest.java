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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
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
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

    PasswordEncoder passwordEncoder = config.getPasswordEncoder();
    assertTrue( passwordEncoder instanceof DefaultPentahoPasswordEncoder );
  }

  @Test
  public void testGetPasswordEncoder_ClassNotFound() throws Exception {
    config = new SpringSecurityHibernateConfig( document );
    Node node = mock( Node.class );
    when( node.getText() ).thenReturn( "org.pentaho.ClassNotFoundEncoder" );
    when( document.selectSingleNode( nullable( String.class ) ) ).thenReturn( node );

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
