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

package org.pentaho.platform.engine.services.actionsequence;

import org.pentaho.platform.api.engine.IOutputDef;

import java.io.OutputStream;
import java.util.List;

public class OutputDef implements IOutputDef {

  private String type;

  private String name;

  private boolean isList;

  private Object value;

  public OutputDef( final String name, final OutputStream outputStream ) {
    this.name = name;
    isList = false;
    type = "content"; //$NON-NLS-1$
    value = outputStream;
  }

  public OutputDef( final String name, final List list ) {
    this.name = name;
    isList = true;
    type = "list"; //$NON-NLS-1$
    value = list;
  }

  public OutputDef( final String name, final String type ) {
    this.name = name;
    this.type = type;
    isList = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputDef#getType()
   */
  public String getType() {
    return type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputDef#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputDef#isList()
   */
  public boolean isList() {
    // TODO Auto-generated method stub
    return isList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputDef#setValue(java.lang.Object)
   */
  public void setValue( final Object value ) {
    if ( !"content".equals( type ) && !"list".equals( type ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      this.value = value;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IOutputDef#getOutputStream()
   */
  public OutputStream getOutputStream() {
    // TODO Auto-generated method stub
    if ( "content".equals( type ) ) { //$NON-NLS-1$
      return (OutputStream) value;
    }
    return null;
  }

  public void addToList( final Object listItem ) {
    if ( "list".equals( type ) ) { //$NON-NLS-1$
      ( (List) value ).add( listItem );
    }
  }

}
