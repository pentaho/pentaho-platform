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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

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
