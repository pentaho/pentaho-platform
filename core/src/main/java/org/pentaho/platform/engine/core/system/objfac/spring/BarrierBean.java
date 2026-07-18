/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.core.system.objfac.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tkafalas
 */
public class BarrierBean {
  private String beanName;

  public BarrierBean( String beanName ) {
    this.beanName = beanName;
  }

  public String getBeanName() {
    return beanName;
  }

  public static List<BarrierBean> convertString( String barrierBeansString ) {
    List<BarrierBean> barrierBeans = Collections.synchronizedList( new ArrayList<BarrierBean>() );
    for ( String beanName : barrierBeansString.split( "," ) ) {
      barrierBeans.add( new BarrierBean( beanName.trim() ) );
    }
    return barrierBeans;
  }
}
