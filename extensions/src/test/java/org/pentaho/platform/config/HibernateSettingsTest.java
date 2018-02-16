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
