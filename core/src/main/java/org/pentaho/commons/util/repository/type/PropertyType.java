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

package org.pentaho.commons.util.repository.type;

public class PropertyType {

  public static final String BOOLEAN = "boolean"; //$NON-NLS-1$
  public static final String ID = "id"; //$NON-NLS-1$
  public static final String INTEGER = "integer"; //$NON-NLS-1$
  public static final String DATETIME = "datetime"; //$NON-NLS-1$
  public static final String DECIMAL = "decimal"; //$NON-NLS-1$
  public static final String HTML = "html"; //$NON-NLS-1$
  public static final String STRING = "string"; //$NON-NLS-1$
  public static final String URI = "uri"; //$NON-NLS-1$
  public static final String XML = "xml"; //$NON-NLS-1$

  private String type;

  public PropertyType( String type ) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

}
