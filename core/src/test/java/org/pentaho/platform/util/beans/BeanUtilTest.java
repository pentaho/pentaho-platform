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


package org.pentaho.platform.util.beans;

import org.junit.Test;
import org.pentaho.di.core.util.Assert;

public class BeanUtilTest {

  private BeanUtil beanUtil;
  private Object bean;

  public BeanUtilTest() {
    bean = new Object();
    beanUtil = new BeanUtil( bean );
  }

  @Test
  public void testIsWriteable() {
    Assert.assertFalse( beanUtil.isWriteable( "[~!@#$%^&*(){}|.,]-=_+|;'\"?<>~`:" ) );
  }

}
