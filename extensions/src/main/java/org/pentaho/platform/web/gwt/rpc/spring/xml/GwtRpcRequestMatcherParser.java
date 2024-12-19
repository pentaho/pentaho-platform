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

import org.pentaho.platform.web.gwt.rpc.matcher.PluginGwtRpcRequestMatcher;
import org.pentaho.platform.web.gwt.rpc.matcher.SystemGwtRpcRequestMatcher;
import org.pentaho.platform.web.gwt.rpc.support.GwtRpcSerializationPolicyCache;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

class GwtRpcRequestMatcherParser extends AbstractBeanDefinitionParser {

  private final boolean isPlugin;

  public GwtRpcRequestMatcherParser( boolean isPlugin ) {
    this.isPlugin = isPlugin;
  }

  @Override
  protected AbstractBeanDefinition parseInternal( Element element, ParserContext parserContext ) {

    String pattern = element.getAttribute( "pattern" );
    String rpcMethods = element.getAttribute( "methods" );

    // Defaults to false, if not present.
    boolean isCaseInsensitive = Boolean.parseBoolean( element.getAttribute( "insensitive" ) );

    if ( pattern.equals( "" ) ) {
      // throws
      parserContext.getReaderContext().fatal(
        "'pattern' attribute is empty or unspecified.",
        element );
    }

    List<String> rpcMethodsList = null;
    if ( !rpcMethods.equals( "" ) ) {
      rpcMethodsList = Arrays.asList( rpcMethods.split( "\\s+" ) );
    }

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( getBeanClass() );
    builder.addConstructorArgValue( pattern );
    builder.addConstructorArgValue( isCaseInsensitive );
    builder.addConstructorArgValue( rpcMethodsList );

    // TODO: Accepts a ref to a shared policy cache? Or, use a single, bounded cache?
    builder.addConstructorArgValue( new GwtRpcSerializationPolicyCache() );

    return builder.getBeanDefinition();
  }

  private Class<?> getBeanClass() {
    return isPlugin ? PluginGwtRpcRequestMatcher.class : SystemGwtRpcRequestMatcher.class;
  }
}
