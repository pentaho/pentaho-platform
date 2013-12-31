/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.plugin.action.olap;

//This bean holds the information needed to create an olap4j connection
//see Olap4jSystemListener
public class Olap4jConnectionBean {
  private String name;
  private String user;
  private String password;
  private String connectString;
  private String className;

  public void setName( String name ) {

    this.name = name;
  }
  public void setUser( String user ) {
    this.user = user;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public void setConnectString( String connectString ) {
    this.connectString = connectString;
  }

  public void setClassName( String className ) {
    this.className = className;
  }

  public String getName() {
    return name;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getConnectString() {
    return connectString;
  }

  public String getClassName() {
    return className;
  }
}
