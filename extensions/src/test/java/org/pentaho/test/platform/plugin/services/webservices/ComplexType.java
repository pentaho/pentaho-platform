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


package org.pentaho.test.platform.plugin.services.webservices;

public class ComplexType {

  private String name;
  private String address;
  private int age;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress( String address ) {
    this.address = address;
  }

  public int getAge() {
    return age;
  }

  public void setAge( int age ) {
    this.age = age;
  }

}
