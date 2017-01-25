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

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reads the attributes added to a bean and sets them on the BeanDefinition's Attributes collection.
 * 
 * {@code} <pen:attributes> <pen:attr key="key" value="value"/> </pen:attributes> {@code}
 * 
 * User: nbaker Date: 1/16/13
 */
public class BeanAttributeHandler implements BeanDefinitionDecorator {
  private static String ATTR = "attr";

  @Override
  public BeanDefinitionHolder decorate( Node node, BeanDefinitionHolder beanDefinitionHolder,
      ParserContext parserContext ) {
    NodeList nodes = node.getChildNodes();

    beanDefinitionHolder.getBeanDefinition().setAttribute( "id", beanDefinitionHolder.getBeanName() );
    for ( int i = 0; i < nodes.getLength(); i++ ) {
      Node n = nodes.item( i );
      if ( stripNamespace( n.getNodeName() ).equals( ATTR ) ) {
        beanDefinitionHolder.getBeanDefinition().setAttribute(
            n.getAttributes().getNamedItem( Const.KEY ).getNodeValue(),
            n.getAttributes().getNamedItem( Const.VALUE ).getNodeValue() );

      }
    }
    return beanDefinitionHolder;
  }

  private static String stripNamespace( String s ) {
    if ( s.indexOf( ':' ) > 0 ) {
      return s.substring( s.indexOf( ':' ) + 1 );
    }
    return s;
  }
}
