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
