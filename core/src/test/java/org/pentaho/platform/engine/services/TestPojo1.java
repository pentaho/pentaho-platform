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


package org.pentaho.platform.engine.services;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import org.pentaho.platform.api.engine.IConfiguredPojo;
//import org.pentaho.platform.api.engine.ISimplePojoComponent;

public class TestPojo1 {
  // public class TestPojo1 implements ISimplePojoComponent, IConfiguredPojo {

  public static String input1;
  protected String output1;

  public static int int1;
  public static Integer int2;
  public static boolean bool1;
  public static Boolean bool2;
  public static long long1;
  public static Long long2;
  public static double double1;
  public static Double double2;
  public static float float1;
  public static Float float2;
  public static BigDecimal bigDecimal;
  public static String setting1;
  public static String setting2;
  public static String setting3;

  public boolean execute() throws Exception {

    // this will generate a null pointer if input1 is null
    output1 = input1 + input1;

    return true;
  }

  public void setLogger( Log log ) {
    PojoComponentTest.setLoggerCalled = true;
  }

  public void setSession( IPentahoSession session ) {
    PojoComponentTest.setSessionCalled = true;
  }

  public String getOutput1() {
    return output1;
  }

  public void setInput1( String input1 ) {
    TestPojo1.input1 = input1;
  }

  public boolean validate() throws Exception {
    return true;
  }

  public boolean done() {
    PojoComponentTest.doneCalled = true;
    return true;
  }

  public void setInt1( int anInt ) {
    TestPojo1.int1 = anInt;
  }

  public void setInt2( Integer anInt ) {
    TestPojo1.int2 = anInt;
  }

  public boolean isBool1() {
    return bool1;
  }

  public void setBool1( boolean bool1 ) {
    TestPojo1.bool1 = bool1;
  }

  public Boolean getBool2() {
    return bool2;
  }

  public void setBool2( Boolean bool2 ) {
    TestPojo1.bool2 = bool2;
  }

  public long getLong1() {
    return long1;
  }

  public void setLong1( long long1 ) {
    TestPojo1.long1 = long1;
  }

  public Long getLong2() {
    return long2;
  }

  public void setLong2( Long long2 ) {
    TestPojo1.long2 = long2;
  }

  public double getDouble1() {
    return double1;
  }

  public void setDouble1( double double1 ) {
    TestPojo1.double1 = double1;
  }

  public Double getDouble2() {
    return double2;
  }

  public void setDouble2( Double double2 ) {
    TestPojo1.double2 = double2;
  }

  public float getFloat1() {
    return float1;
  }

  public void setFloat1( float float1 ) {
    TestPojo1.float1 = float1;
  }

  public Float getFloat2() {
    return float2;
  }

  public void setFloat2( Float float2 ) {
    TestPojo1.float2 = float2;
  }

  public BigDecimal getBigDecimal() {
    return bigDecimal;
  }

  public void setBigDecimal( BigDecimal bigDecimal ) {
    TestPojo1.bigDecimal = bigDecimal;
  }

  public int getInt1() {
    return int1;
  }

  public Integer getInt2() {
    return int2;
  }

  public boolean configure( Map<String, String> settings ) {

    TestPojo1.setting1 = settings.get( "pojosettings/settings.xml{settings-root/setting1-name}" ); //$NON-NLS-1$
    TestPojo1.setting2 = settings.get( "pojosettings/settings.xml{settings-root/setting2-name}" ); //$NON-NLS-1$
    TestPojo1.setting3 = settings.get( "pojosettings/settings.xml{settings-root/bogus}" ); //$NON-NLS-1$
    return true;
  }

  public Set<String> getConfigSettingsPaths() {
    Set<String> paths = new HashSet<String>();
    paths.add( "pojosettings/settings.xml{settings-root/setting1-name}" ); //$NON-NLS-1$
    paths.add( "pojosettings/settings.xml{settings-root/setting2-name}" ); //$NON-NLS-1$
    paths.add( "pojosettings/settings.xml{settings-root/bogus}" ); //$NON-NLS-1$
    return paths;
  }

}
