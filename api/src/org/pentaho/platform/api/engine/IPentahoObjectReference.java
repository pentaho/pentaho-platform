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
 * Copyright 2012-2013 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.api.engine;

import java.util.Map;

/**
 * User: nbaker
 * Date: 1/16/13
 *
 * IPentahoObjectReference objects represent a pointer to an available object in the IPentahoObjectFactory
 * and contains all attributes associated with that object (priority, adhoc attributes).
 *
 * Implementations of this class should be lazy where possible, deferring retrieval of the Object
 * until the getObject() method is called
 *
 */
public interface IPentahoObjectReference<T> extends Comparable{
  Map<String, Object> getAttributes();
  T getObject();
}
