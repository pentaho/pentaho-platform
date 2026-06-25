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
