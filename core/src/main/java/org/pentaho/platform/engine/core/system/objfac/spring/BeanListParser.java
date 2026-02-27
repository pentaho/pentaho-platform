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

package org.pentaho.platform.engine.core.system.objfac.spring;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Map<String, String> propMap = new HashMap<>();
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
