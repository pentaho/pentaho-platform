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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.config;

/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 */

import java.io.Serializable;

public class HibernateSettings implements IHibernateSettings, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  String hibernateConfigFile;
  boolean hibernateManaged;

  public HibernateSettings() {

  }

  public HibernateSettings( IHibernateSettings hibernateSettings ) {
    setHibernateConfigFile( hibernateSettings.getHibernateConfigFile() );
    setHibernateManaged( hibernateSettings.getHibernateManaged() );
  }

  public String getHibernateConfigFile() {
    return hibernateConfigFile;
  }

  public void setHibernateConfigFile( String hibernateConfigFile ) {
    this.hibernateConfigFile = hibernateConfigFile;
  }

  public boolean getHibernateManaged() {
    return hibernateManaged;
  }

  public void setHibernateManaged( boolean hibernateManaged ) {
    this.hibernateManaged = hibernateManaged;
  }

}
