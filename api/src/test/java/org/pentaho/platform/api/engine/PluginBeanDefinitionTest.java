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


package org.pentaho.platform.api.engine;

import org.junit.jupiter.api.Test;

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
