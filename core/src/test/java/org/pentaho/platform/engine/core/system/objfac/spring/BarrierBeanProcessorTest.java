/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.system.objfac.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;

/**
 * @author tkafalas
 */
public class BarrierBeanProcessorTest {
  public static final String BARRIER_NAME1 = "KarafFeatureWatcherBarrier";
  public static final String BARRIER_NAME2 = "testBarrier2";
  public static final String BARRIER_NAME3 = "testBarrier3";

  @Test
  public void testRegisterBarrierBean() {
    BarrierBeanProcessor barrierBeanProcessor = BarrierBeanProcessor.getInstance();
    barrierBeanProcessor.getBarrierBeans().clear();// Need to make sure the collection is clean before testing.

    barrierBeanProcessor.registerBarrierBeans( "src/test/resources/solution/system/barrierBean.properties" );

    assertEquals( 3, barrierBeanProcessor.getBarrierBeans().size() );
    // Check barrierBean map
    Map<String, List<BarrierBean>> barrierBeanMap = barrierBeanProcessor.getBarrierBeans();
    assertEquals( 1, barrierBeanMap.get( BARRIER_NAME1 ).size() );
    assertEquals( 4, barrierBeanMap.get( BARRIER_NAME2 ).size() );
    assertEquals( 1, barrierBeanMap.get( BARRIER_NAME3 ).size() );

    // Check beanBarrier map
    Map<String, Set<String>> beanBarrierMap = barrierBeanProcessor.getBeanBarriers();
    assertEquals( 2, beanBarrierMap.get( "fo" ).size() );
    assertTrue( beanBarrierMap.get( "fo" ).contains( BARRIER_NAME2 ) );
    assertTrue( beanBarrierMap.get( "fo" ).contains( BARRIER_NAME3 ) );

    // Test ability to add another bean to existing barrier
    Properties testProperties = new Properties();
    testProperties.put( BARRIER_NAME2, "foo" );
    barrierBeanProcessor.registerBarrierBeans( testProperties );
    assertEquals( 5, barrierBeanMap.get( BARRIER_NAME2 ).size() );
    assertTrue( isBeanInList( barrierBeanMap.get( BARRIER_NAME2 ), "foo" ) );
    assertTrue( beanBarrierMap.get( "foo" ).contains( BARRIER_NAME2 ) );

    barrierBeanProcessor.registerBarrierBeans( testProperties );

    // System should forgive a bad file
    barrierBeanProcessor.registerBarrierBeans( "fooFile" );
    assertEquals( 3, barrierBeanProcessor.getBarrierBeans().size() );

    // Check for availablility
    IServiceBarrierManager serviceBarrierManager = IServiceBarrierManager.LOCATOR.getManager();
    IServiceBarrier serviceBarrier = serviceBarrierManager.getServiceBarrier( BARRIER_NAME1 );
    assertTrue( serviceBarrier.isAvailable() );

    // The following line should not block since we did not put a hold on the barrier
    barrierBeanProcessor.awaitBarrier( BARRIER_NAME1 );
  }

  private static boolean isBeanInList( List<BarrierBean> list, String beanName ) {
    for ( BarrierBean barrierBean : list ) {
      if ( beanName.equals( barrierBean.getBeanName() ) ) {
        return true;
      }
    }
    return false;
  }

}
