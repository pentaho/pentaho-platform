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

package org.pentaho.platform.web.servlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.util.ArrayList;
import java.util.List;

public class PentahoSerializationPolicy extends SerializationPolicy {
  private List<String> whiteList = new ArrayList<String>();

  @Override
  public boolean shouldDeserializeFields( Class<?> clazz ) {
    return whiteList.contains( clazz.getName() );
  }

  @Override
  public boolean shouldSerializeFields( Class<?> clazz ) {
    return whiteList.contains( clazz.getName() );
  }

  @Override
  public void validateDeserialize( Class<?> arg0 ) throws SerializationException {

  }

  @Override
  public void validateSerialize( Class<?> arg0 ) throws SerializationException {

  }

  public List<String> getWhiteList() {
    return whiteList;
  }

  public void setWhiteList( List<String> whiteList ) {
    this.whiteList = whiteList;
  }
}
