/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
import org.pentaho.platform.servicecoordination.api.IServiceBarrier;
import org.pentaho.platform.servicecoordination.api.IServiceBarrierManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tkafalas
 */
public class BarrierBeanProcessor {
  private Logger logger = LoggerFactory.getLogger( BarrierBeanProcessor.class );
  private static BarrierBeanProcessor barrierBeanProcessor;
  private IServiceBarrierManager serviceBarrierManager = IServiceBarrierManager.LOCATOR.getManager();
  private Map<String, List<BarrierBean>> barrierBeans = new ConcurrentHashMap<String, List<BarrierBean>>();
  private Map<String, Set<String>> beanBarriers = new ConcurrentHashMap<String, Set<String>>();

  private BarrierBeanProcessor() {
  }

  public static BarrierBeanProcessor getInstance() {
    if ( barrierBeanProcessor == null ) {
      barrierBeanProcessor = new BarrierBeanProcessor();
    }
    return barrierBeanProcessor;
  }

  /**
   * This method can be run multiple times for multiple barrierBean files. Only one list of barrierBeans is maintained
   * so that plugins can add their own barrierBeans if necessary.  Registered barrierBeans will be held just prior
   * to bean initialization.  See {@link BarrierBeanPostProcessor}
   *
   * @param barrierBeanFilePath
   * @return
   */
  public void registerBarrierBeans( String barrierBeanFilePath ) {
    Properties barrierBeanProperties = new Properties();
    File barrierBeanFile = new File( barrierBeanFilePath );
    if ( barrierBeanFile.exists() ) {

      FileInputStream fileInput = null;
      try {
        fileInput = new FileInputStream( barrierBeanFile );
        barrierBeanProperties.load( fileInput );
      } catch ( FileNotFoundException e ) {
        e.printStackTrace();
      } catch ( IOException e ) {
        e.printStackTrace();
      } finally {
        if ( fileInput != null ) {
          try {
            fileInput.close();
          } catch ( IOException e ) {
            e.printStackTrace();
          }
        }
      }

      registerBarrierBeans( barrierBeanProperties );

    }
  }

  /**
   * This method can be run multiple times for multiple barrierBean property sets. Only one list of barrierBeans is
   * maintained so that plugins can add their own barrierBeans if necessary.  Registered barrierBeans will be held just
   * prior to bean initialization.  See {@link BarrierBeanPostProcessor}
   *
   * @param barrierBeanFilePath
   * @return
   */
  @SuppressWarnings( "unchecked" )
  public void registerBarrierBeans( Properties barrierBeanProperties ) {
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
  public Map<String, List<BarrierBean>> getBarrierBeans() {
    return barrierBeans;
  }

  /**
   * @return Returns a map where key = Bean Name, value = list of Barrier names
   */
  public Map<String, Set<String>> getBeanBarriers() {
    return beanBarriers;
  }

  public void awaitBarrier( String barrierName ) {
    // Check the service barrier
    try {
      serviceBarrierManager.getServiceBarrier( barrierName ).awaitAvailability();
    } catch ( InterruptedException e1 ) {
      // This should never happen
      logger.error( "ServiceBarrier Interrupted", e1 );
    }
  }

}
