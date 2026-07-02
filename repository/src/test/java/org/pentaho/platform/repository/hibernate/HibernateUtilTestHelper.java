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


package org.pentaho.platform.repository.hibernate;

/**
 * Solely for the purpose of getting access to the protected
 * {@link org.pentaho.platform.repository.hibernate.HibernateUtil#initialize()} method.
 * 
 * @author mlowery
 */
public class HibernateUtilTestHelper {
  public static void initialize() {
    HibernateUtil.initialize();
  }
}
