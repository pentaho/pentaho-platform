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
package org.pentaho.platform.engine.core.system.objfac.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.ListUtils;
import org.pentaho.platform.api.engine.IServiceBarrier;
import org.pentaho.platform.api.engine.IServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tkafalas
 */
public class BarrierBeanProcessor {
  private static Logger logger = LoggerFactory.getLogger( BarrierBeanProcessor.class );
  
  private static IServiceBarrierManager serviceBarrierManager = IServiceBarrierManager.LOCATOR.getManager();
  private static Map<String, List<BarrierBean>> barrierBeans = new ConcurrentHashMap<String, List<BarrierBean>>();
  private static Map<String, Set<String>> beanBarriers = new ConcurrentHashMap<String, Set<String>>();

  /**
   * This method can be run multiple times for multiple barrierBean files. Only one list of barrierBeans is maintained
   * so that plugins can add their own barrierBeans if necessary.
   * 
   * @param barrierBeanFilePath
   * @return
   */
  public static void registerBarrierBeans( String barrierBeanFilePath ) {
    Properties barrierBeanProperties = new Properties();
    File barrierBeanFile = new File( barrierBeanFilePath );
    if ( barrierBeanFile.exists() ) {

      try {
        FileInputStream fileInput = new FileInputStream( barrierBeanFile );
        barrierBeanProperties.load( fileInput );
        fileInput.close();
      } catch ( FileNotFoundException e ) {
        e.printStackTrace();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      
      registerBarrierBeans( barrierBeanProperties );

    }
  }
  
  /**
   * This method can be run multiple times for multiple barrierBean property sets. Only one list of barrierBeans is maintained
   * so that plugins can add their own barrierBeans if necessary.
   * 
   * @param barrierBeanFilePath
   * @return
   */
  @SuppressWarnings( "unchecked" )
  public static void registerBarrierBeans( Properties barrierBeanProperties ){
    Enumeration<Object> enuKeys = barrierBeanProperties.keys();
    while ( enuKeys.hasMoreElements() ) {
      String barrierName = (String) enuKeys.nextElement();
      IServiceBarrier barrier = serviceBarrierManager.getServiceBarrier( barrierName );
      List<BarrierBean> theseBarrierBeans =
          BarrierBean.convertString( barrierBeanProperties.getProperty( barrierName ) );
      if ( theseBarrierBeans.size() > 0 ) {
        for ( BarrierBean barrierBean : theseBarrierBeans ) {
          //Add the beans/barriers to the maps
          if ( beanBarriers.containsKey( barrierBean.getBeanName() ) ) {
            beanBarriers.get( barrierBean.getBeanName() ).add( barrierName );
          } else {
            Set<String> newSet = new HashSet<String>();
            newSet.add( barrierName );
            beanBarriers.put( barrierBean.getBeanName(), newSet );
          }

          //put one hold on the barrier for each bean defined for it.
          barrier.hold();
        }

        List<BarrierBean> finalBarrierBeans = (List<BarrierBean>) barrierBeans.get( barrierName );
        finalBarrierBeans =
            finalBarrierBeans == null ? theseBarrierBeans : ListUtils.union( finalBarrierBeans, theseBarrierBeans );
        barrierBeans.put( barrierName, finalBarrierBeans );
      }
    }
  }
  
  /**
   * @return Returns a map where key = barrier name, value = list of {@link BarrierBean}s
   */
  public static Map<String, List<BarrierBean>> getBarrierBeans() {
    return barrierBeans;
  }

  /**
   * @return Returns a map where key = Bean Name, value = list of Barrier names
   */
  public static Map<String, Set<String>> getBeanBarriers() {
    return beanBarriers;
  }
  
  public static void awaitBarrier( String barrierName ){
    // Check the service barrier
    try {
      serviceBarrierManager.getServiceBarrier( barrierName ).awaitAvailability();
    } catch ( InterruptedException e1 ) {
      // This should never happen
      logger.error( "ServiceBarrier Interrupted", e1 );
    }
  }  
  
}
