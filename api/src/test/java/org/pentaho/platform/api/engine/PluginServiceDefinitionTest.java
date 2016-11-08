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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
