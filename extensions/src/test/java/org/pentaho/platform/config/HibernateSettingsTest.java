package org.pentaho.platform.config;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/21/15.
 */
public class HibernateSettingsTest {
  @Test
  public void testHasValidGettersAndSetters() {
    assertThat( HibernateSettings.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testHasValidConstructors() throws Exception {
    assertThat( HibernateSettings.class, hasValidBeanConstructor() );
  }

  @Test
  public void testConstructor() throws Exception {
    IHibernateSettings hibSettings = mock( IHibernateSettings.class );
    when( hibSettings.getHibernateConfigFile() ).thenReturn( "hibernate/config/file.xml" );
    when( hibSettings.getHibernateManaged() ).thenReturn( true );

    HibernateSettings hibernateSettings = new HibernateSettings( hibSettings );
    assertEquals( "hibernate/config/file.xml", hibernateSettings.getHibernateConfigFile() );
    assertEquals( true, hibernateSettings.getHibernateManaged() );
  }
}
