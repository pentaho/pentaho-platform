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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectSerializationUtilTest {

  private long currentTime;
  private Date currentDate;
  private Map map;
  private Company company;

  @Before
  public void setup() {
    currentTime = System.currentTimeMillis();
    currentDate = new Date( currentTime );
    company = new Company( "Pentaho" );

    map = new HashMap();
    map.put( "mydate", currentDate );
    map.put( "mytime", currentTime );
    map.put( "mycompany", company );
  }

  @Test
  public void testGetFieldValueByReflectionDateNegative() {

    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( null, null ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( currentDate, null ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( null, "time" ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( "", "" ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( currentDate, "" ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( currentDate, "foo" ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( "", "foo" ) );

    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( company, "foo" ) );
    assertNull( ObjectSerializationUtil.getPropertyValueByReflection( company, "employees.foe" ) );
  }

  @Test
  public void testGetFieldValueByReflectionPositive() {
    // public getTime() method
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValueByReflection( currentDate, "getTime" ) );
    // private  fastTime field
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValueByReflection( currentDate, "fastTime" ) );

    final Employee john = company.employees.get( "john" );
    final Employee jane = company.employees.get( "jane" );

    // public isManager() method
    assertEquals( "false", "" + ObjectSerializationUtil.getPropertyValueByReflection( john, "isManager" ) );
    assertEquals( "true", "" + ObjectSerializationUtil.getPropertyValueByReflection( jane, "isManager" ) );
    // private imAManager field
    assertEquals( "false", "" + ObjectSerializationUtil.getPropertyValueByReflection( john, "imAManager" ) );
    assertEquals( "true", "" + ObjectSerializationUtil.getPropertyValueByReflection( jane, "imAManager" ) );
  }

  @Test
  public void testGetFieldValueNegative() {

    assertNull( ObjectSerializationUtil.getPropertyValue( null, null ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( currentDate, null ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( null, "time" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( "", "" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( currentDate, "" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( currentDate, "foo" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( "", "foo" ) );

    assertNull( ObjectSerializationUtil.getPropertyValue( company, "foo" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( company, "employees.foe" ) );

    assertNull( ObjectSerializationUtil.getPropertyValue( map, "mycompany.foo" ) );
    assertNull( ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees.foe" ) );
  }

  @Test
  public void testGetFieldValuePositive() {

    // public getTime()
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( currentDate, "time" ) );
    // private  fastTime
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( currentDate, "fastTime" ) );

    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( map, "mytime" ) );
    assertEquals( currentDate, ObjectSerializationUtil.getPropertyValue( map, "mydate" ) );

    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( map, "mydate.time" ) );
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( map, "mydate.fastTime" ) );

    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( map, "mydate.time" ) );
    assertEquals( currentTime, ObjectSerializationUtil.getPropertyValue( map, "mydate.fastTime" ) );
    assertEquals( company, ObjectSerializationUtil.getPropertyValue( map, "mycompany" ) );
    assertEquals( company, ObjectSerializationUtil.getPropertyValue( map, "mycompany" ) );


    assertEquals( "Pentaho", ObjectSerializationUtil.getPropertyValue( map, "mycompany.name" ) );

    assertTrue( ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees" ) instanceof Map );
    assertEquals( "Smith", ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees.john.lastName" ) );
    assertFalse( (boolean) ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees.john.manager" ) );
    assertTrue( (boolean) ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees.jane.manager" ) );

    assertEquals( 2, ( (Map) ObjectSerializationUtil.getPropertyValue( map, "mycompany.employees" )).size() );

    final Object normalizedCompany = ObjectSerializationUtil.normalize( company );
    assertEquals( "Smith", ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john.lastName" ) );
    assertFalse( (boolean) ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.john.manager" ) );
    assertTrue( (boolean) ObjectSerializationUtil.getPropertyValue( normalizedCompany, "employees.jane.manager" ) );
  }

  @Test
  public void testNormalizeNegative() {
    assertNull( ObjectSerializationUtil.normalize( null ) );
  }

  @Test
  public void testNormalizePositive() {

    final Object normalizedCompany = ObjectSerializationUtil.normalize( company, 10 );
    assertNotNull( normalizedCompany );
    assertTrue( normalizedCompany instanceof Map );
    final Map normalizedCompanyMap = (Map) normalizedCompany;
    assertEquals( "Pentaho", normalizedCompanyMap.get( "name" ) );
    assertTrue( normalizedCompanyMap.get( "employees" ) instanceof Map );
    final Map employees = (Map) normalizedCompanyMap.get( "employees" );
    final Map employeeJohn = (Map) employees.get( "john" );
    assertEquals( "John", employeeJohn.get( "firstName" ) );
    assertEquals( "Smith", employeeJohn.get( "lastName" ) );
    assertEquals( false, employeeJohn.get( "imAManager" ) );
    assertEquals( false, employeeJohn.get( "isManager" ) );
    assertTrue( employeeJohn.get( "age" ) instanceof Map );
    final Map johnAge = (Map) employeeJohn.get( "age" );
    assertEquals( 30, johnAge.get( "value" ) );

    final Map employeeJane = (Map) employees.get( "jane" );
    assertEquals( "Jane", employeeJane.get( "firstName" ) );
    assertEquals( "Doe", employeeJane.get( "lastName" ) );
    assertEquals( true, employeeJane.get( "imAManager" ) );
    assertEquals( true, employeeJane.get( "isManager" ) );
    assertTrue( employeeJane.get( "age" ) instanceof Map );
    final Map janeAge = (Map) employeeJane.get( "age" );
    assertEquals( 40, janeAge.get( "value" ) );
  }

  public class Company {
    private String name;
    private Map<String, Employee> employees = new HashMap<String, Employee>();
    private Map<String, Department> departments = new HashMap<String, Department>();

    Company( final String name ) {
      this.name = name;
      employees.put( "john", new Employee( this, "John", "Smith", 30, false ) );
      employees.put( "jane", new Employee( this, "Jane", "Doe", 40, true ) );
      departments.put( "Finance", new Department( "Finance Department" ) );
      departments.put( "IT", new Department( "IT Department" ) );
      departments.put( "Accounting", new Department( "Accounting Department" ) );
    }
    public void addEmployee( final String firstName, final String lastName, final boolean isManager ) {
      employees.put( firstName, new Employee ( this, firstName, lastName, 30, isManager ) );
    }
  }

  public class Department {
    private String name;

    Department( final String name ) {
      this.name = name;
    }
  }

  public class Employee {
    private boolean imAManager;
    private Company company;
    private String firstName;
    private String lastName;
    private Age age;

    Employee( final Company company, final String firstName, final String lastName, final int ageValue, final boolean
      imAManager ) {
      this.company = company;
      this.firstName = firstName;
      this.lastName = lastName;
      this.age = new Age( this, ageValue );
      this.imAManager = imAManager;
    }

    public boolean isManager() {
      return this.imAManager;
    }
  }

  public class Age {
    private Employee employee;
    private int value;

    Age( final Employee employee, final int value ) {
      this.employee = employee;
      this.value = value;
    }
  }
}
