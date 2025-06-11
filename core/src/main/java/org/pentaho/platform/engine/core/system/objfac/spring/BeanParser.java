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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a bean from Spring XML
 *
 * {@code} <pen:bean class="com.foo.Clazz"/> {@code}
 *
 * User: nbaker Date: 3/2/13
 */
public class BeanParser extends AbstractBeanDefinitionParser {

  @Override
  protected AbstractBeanDefinition parseInternal( Element element, ParserContext parserContext ) {
    String originalClassName = element.getAttribute( "class" );
    element.setAttribute( "class", BeanBuilder.class.getName() );

    BeanDefinitionHolder holder = parserContext.getDelegate().parseBeanDefinitionElement( element );
    BeanDefinition definition = holder.getBeanDefinition();
    definition.setAttribute( "originalClassName", originalClassName );

    parserContext.getDelegate().decorateBeanDefinitionIfRequired( element, holder );

    definition.setBeanClassName( BeanBuilder.class.getName() );

    // BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BeanBuilder.class.getName());
    definition.getPropertyValues().addPropertyValue( "type", originalClassName );

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

    definition.getPropertyValues().addPropertyValue( Const.ATTRIBUTES, propMap );

    // AbstractBeanDefinition definition = builder.getRawBeanDefinition();
    // definition.setSource(parserContext.extractSource(element));

    return new GenericBeanDefinition( definition );
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
