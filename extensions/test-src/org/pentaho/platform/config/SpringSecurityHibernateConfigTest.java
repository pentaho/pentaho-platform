package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.springframework.security.providers.encoding.PasswordEncoder;

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
