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

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by bgroves on 10/29/15.
 */
public class PluginServiceDefinitionTest {
  private static final String ID = "ID";
  private static final String TITLE = "Title";
  private static final String DESCRIPTION = "Description";
  private static final String SERVICE_BEAN = "ServiceBean";
  private static final String SERVICE_CLASS = "SERVICE_CLASS";
  private static final String TYPE_ONE = "typeOne";
  private static final String TYPE_TWO = "typeTwo";
  private static final String[] TYPES = { TYPE_ONE, TYPE_TWO };
  private static final Collection<String> EXTRA_CLASSES = new ArrayList<String>();

  @Test
  public void testGettersSetters() {
    PluginServiceDefinition def = new PluginServiceDefinition();

    assertNull( def.getId() );
    def.setId( ID );
    assertEquals( ID, def.getId() );

    assertNull( def.getTitle() );
    def.setTitle( TITLE );
    assertEquals( TITLE, def.getTitle() );

    assertNull( def.getDescription() );
    def.setDescription( DESCRIPTION );
    assertEquals( DESCRIPTION, def.getDescription() );

    assertNull( def.getTypes() );
    def.setTypes( TYPES );
    assertEquals( 2, def.getTypes().length );
    assertEquals( TYPE_ONE, def.getTypes()[0] );
    assertEquals( TYPE_TWO, def.getTypes()[1] );

    assertNull( def.getServiceBeanId() );
    def.setServiceBeanId( SERVICE_BEAN );
    assertEquals( SERVICE_BEAN, def.getServiceBeanId() );

    assertNull( def.getServiceClass() );
    def.setServiceClass( SERVICE_CLASS );
    assertEquals( SERVICE_CLASS, def.getServiceClass() );

    assertNull( def.getExtraClasses() );
    def.setExtraClasses( EXTRA_CLASSES );
    assertEquals( EXTRA_CLASSES, def.getExtraClasses() );
  }
}
