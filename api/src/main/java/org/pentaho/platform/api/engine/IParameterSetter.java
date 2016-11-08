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

package org.pentaho.platform.api.engine;

import java.util.Date;

/**
 * Augments the parameter provider by adding settings for the parameters. Some parameter providers allow setting
 * and updating values.
 * 
 * @author jdixon
 */

public interface IParameterSetter extends IParameterProvider {

  /**
   * Sets a named parameter to a <tt>String</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>String</tt> value to set
   */
  public void setParameter( String name, String value );

  /**
   * Sets a named parameter to a <tt>long</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>long</tt> value to set
   */
  public void setParameter( String name, long value );

  /**
   * Sets a named parameter to a <tt>Date</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>Date</tt> value to set
   */
  public void setParameter( String name, Date value );

  /**
   * Sets a named parameter to a <tt>Object</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>Object</tt> value to set
   */
  public void setParameter( String name, Object value );

}
