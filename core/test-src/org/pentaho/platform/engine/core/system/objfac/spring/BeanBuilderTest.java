/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class BeanBuilderTest {
  @Test
  public void testGetObjectWithoutDampeningTimeout() throws Exception {
    IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    ISystemConfig systemConfig = mock( ISystemConfig.class );
    doReturn( systemConfig ).when( pentahoObjectFactory ).get( eq( ISystemConfig.class ), any( IPentahoSession.class ) );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    final IPentahoObjectReference objectReference = mock( IPentahoObjectReference.class );
    when( pentahoObjectFactory.objectDefined( eq( BeanTestInterface.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.getObjectReferences( eq( BeanTestInterface.class ), any( IPentahoSession.class ), any( Map.class ) ) )
        .thenReturn( null, new ArrayList<IPentahoObjectReference>() { { add( objectReference ); } } );
    final int testValue = 5;
    doReturn( new BeanTestInterface() {
      @Override public int testMethod() {
        return testValue;
      }
    } ).when( objectReference ).getObject();

    BeanBuilder beanBuilder = new BeanBuilder();
    beanBuilder.setType( BeanTestInterface.class.getName() );
    beanBuilder.setAttributes( new HashMap<String, String>() );

    Object object = beanBuilder.getObject();

    assertNotNull( object );
    assertTrue( object instanceof BeanTestInterface );
    assertEquals( testValue, ( (BeanTestInterface) object ).testMethod() );
  }

  private interface BeanTestInterface {
    int testMethod();
  }
}
