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
