/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.*;

/**
 * Created by rfellows on 11/10/15.
 */
public class SettingTest {

  @Test
  public void testGettersAndSetters() throws Exception {
    assertThat( Setting.class, hasValidGettersAndSetters() );
  }

  @Test
  public void testConstructors() throws Exception {
    assertThat( Setting.class, hasValidBeanConstructor() );

    Setting setting = new Setting( "user", "password" );
    assertEquals( "user", setting.getName() );
    assertEquals( "password", setting.getValue() );
  }

}
