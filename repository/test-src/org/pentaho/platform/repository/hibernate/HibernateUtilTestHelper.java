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
