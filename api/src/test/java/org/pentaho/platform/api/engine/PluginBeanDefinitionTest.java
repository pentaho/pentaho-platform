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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PluginBeanDefinitionTest {

  public static final String BEAN_ID = "myBeanId";
  public static final String CLASSNAME = "myClassName";
  public static final String NEW_BEAN_ID = "newBeanId";
  public static final String NEW_CLASSNAME = "newClassName";

  @Test
  public void testGetterSetter() {
    PluginBeanDefinition definition = new PluginBeanDefinition( BEAN_ID, CLASSNAME );
    assertEquals( BEAN_ID, definition.getBeanId() );
    assertEquals( CLASSNAME, definition.getClassname() );

    definition.setBeanId( NEW_BEAN_ID );
    assertEquals( NEW_BEAN_ID, definition.getBeanId() );

    definition.setClassname( NEW_CLASSNAME );
    assertEquals( NEW_CLASSNAME, definition.getClassname() );
  }
}
