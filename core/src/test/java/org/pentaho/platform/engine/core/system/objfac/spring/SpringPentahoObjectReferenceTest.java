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

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringPentahoObjectReferenceTest {
  @Test
  public void testOwnerPluginIdBeanIsPassedToPublisherPluginIdAttribute() {
    // Mark the context as owned by a plugin.
    ConfigurableApplicationContext context = mock( ConfigurableApplicationContext.class );

    String testOwnerPluginId = "test-plugin-id";
    when( context.getBean( Const.OWNER_PLUGIN_ID_BEAN, String.class ) ).thenReturn( testOwnerPluginId );

    String testBeanName = "test-bean";

    // Any class will do.
    Class<UUID> clazz = UUID.class;
    UUID testObject = UUID.randomUUID();

    IPentahoSession session = mock( IPentahoSession.class );

    BeanDefinition beanDef = mock( BeanDefinition.class );
    when( beanDef.attributeNames() ).thenReturn( new String[0] );

    SpringPentahoObjectReference<UUID> objectReference =
      new SpringPentahoObjectReference<>( context, testBeanName, clazz, session, beanDef );

    assertNotNull( objectReference.getAttributes() );
    assertEquals( testOwnerPluginId, objectReference.getAttributes().get( Const.PUBLISHER_PLUGIN_ID_ATTRIBUTE ) );
  }
}
