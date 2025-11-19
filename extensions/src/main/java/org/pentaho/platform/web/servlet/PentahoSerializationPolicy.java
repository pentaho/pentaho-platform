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
