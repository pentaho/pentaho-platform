package org.pentaho.platform.engine.core.system.objfac.spring;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * User: nbaker
 * Date: 1/16/13
 */
public class BeanAttributeHandler implements BeanDefinitionDecorator {
  private static String ATTR = "attr";

  @Override
  public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder beanDefinitionHolder, ParserContext parserContext) {
    NodeList nodes = node.getChildNodes();

    beanDefinitionHolder.getBeanDefinition().setAttribute("id", beanDefinitionHolder.getBeanName());
    for(int i=0; i < nodes.getLength(); i++){
      Node n = nodes.item(i);
      if(stripNamespace(n.getNodeName()).equals(ATTR)){
        beanDefinitionHolder.getBeanDefinition().setAttribute(
            n.getAttributes().getNamedItem(Const.KEY).getNodeValue(),
            n.getAttributes().getNamedItem(Const.VALUE).getNodeValue());

      }
    }
    return beanDefinitionHolder;
  }

  private static String stripNamespace(String s){
    if(s.indexOf(':') > 0){
      return s.substring(s.indexOf(':') + 1);
    }
    return s;
  }
}
