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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * 
 * Parses a list bean from Spring XML
 * 
 * {@code} <pen:list class="com.foo.Clazz"/> {@code}
 * 
 * User: nbaker Date: 3/2/13
 */
public class BeanListParser extends AbstractBeanDefinitionParser {

  @Override
  protected AbstractBeanDefinition parseInternal( Element element, ParserContext parserContext ) {
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition( BeanListBuilder.class.getName() );
    builder.addPropertyValue( "type", element.getAttribute( "class" ) );

    Map<String, String> propMap = new HashMap<String, String>();
    Element objectproperties = DomUtils.getChildElementByTagName( element, Const.ATTRIBUTES );
    if ( objectproperties != null ) {
      List props = DomUtils.getChildElementsByTagName( objectproperties, Const.ATTR );
      if ( props != null ) {
        for ( Object o : props ) {
          Element prop = (Element) o;
          String key = prop.getAttribute( Const.KEY );
          String value = prop.getAttribute( Const.VALUE );
          propMap.put( key, value );
        }
      }
    }
    builder.addPropertyValue( Const.ATTRIBUTES, propMap );

    AbstractBeanDefinition definition = builder.getRawBeanDefinition();
    definition.setSource( parserContext.extractSource( element ) );

    return definition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#shouldGenerateIdAsFallback()
   */
  @Override
  protected boolean shouldGenerateIdAsFallback() {
    return true;
  }
}
