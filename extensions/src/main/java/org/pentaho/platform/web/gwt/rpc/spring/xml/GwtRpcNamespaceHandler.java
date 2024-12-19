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


package org.pentaho.platform.web.gwt.rpc.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class GwtRpcNamespaceHandler extends NamespaceHandlerSupport {
  @Override
  public void init() {
    registerBeanDefinitionParser( "plugin-gwt-rpc-request-matcher", new GwtRpcRequestMatcherParser( true ) );
    registerBeanDefinitionParser( "system-gwt-rpc-request-matcher", new GwtRpcRequestMatcherParser( false ) );
  }
}
