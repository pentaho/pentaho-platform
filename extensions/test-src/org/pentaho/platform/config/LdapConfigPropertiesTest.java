package org.pentaho.platform.config;

import org.junit.Test;

import java.util.Properties;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/21/15.
 */
public class LdapConfigPropertiesTest {
  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludeProperties = new String[] {
      "properties"
    };
    assertThat( LdapConfigProperties.class, hasValidGettersAndSettersExcluding( excludeProperties ) );
  }

  @Test
  public void testConstructor() throws Exception {
    Properties properties = new Properties();
    LdapConfigProperties props = new LdapConfigProperties( properties );
    assertEquals( properties, props.getProperties() );
  }
}
