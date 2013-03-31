package org.pentaho.platform.engine.core.system.objfac.spring;

import org.apache.commons.lang.ClassUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: nbaker
 * Date: 3/27/13
 */
public class BeanPublishParser implements BeanDefinitionDecorator {
  private static String ATTR = "as-type";
  private static enum specialPublishTypes{
    INTERFACES,
    CLASSES,
    ALL
  }


  @Override
  public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder beanDefinitionHolder, ParserContext parserContext) {


    String publishType = null;
    String beanClassName = beanDefinitionHolder.getBeanDefinition().getBeanClassName();
    if(node.getAttributes().getNamedItem(ATTR) != null){                          // publish as type if specified
      publishType = node.getAttributes().getNamedItem(ATTR).getNodeValue();
    } else {                                                                      // fallback to publish as itself
      publishType = beanClassName;
    }


    NodeList nodes = node.getChildNodes();

    for(int i=0; i < nodes.getLength(); i++){
      Node n = nodes.item(i);
      if(stripNamespace(n.getNodeName()).equals(Const.ATTRIBUTES)){
        NodeList attrnodes = n.getChildNodes();

        for(int y=0; y < attrnodes.getLength(); y++){
          Node an = attrnodes.item(y);
          if(stripNamespace(an.getNodeName()).equals(Const.ATTR)){
            beanDefinitionHolder.getBeanDefinition().setAttribute(
            an.getAttributes().getNamedItem(Const.KEY).getNodeValue(),
            an.getAttributes().getNamedItem(Const.VALUE).getNodeValue());
          }
        }
      }
    }


    try {
      List<Class<?>> classesToPublish = new ArrayList<Class<?>>();

      Class<?> clazz = getClass().getClassLoader().loadClass(beanClassName);

      if(specialPublishTypes.INTERFACES.name().equals(publishType)) {
        classesToPublish.addAll(ClassUtils.getAllInterfaces(clazz));
      } else if(specialPublishTypes.CLASSES.name().equals(publishType)) {

        classesToPublish.addAll(ClassUtils.getAllSuperclasses(clazz));
        classesToPublish.add(clazz);
      } else if(specialPublishTypes.ALL.name().equals(publishType)) {

        classesToPublish.addAll(ClassUtils.getAllInterfaces(clazz));
        classesToPublish.addAll(ClassUtils.getAllSuperclasses(clazz));
        classesToPublish.add(clazz);
      } else {
        classesToPublish.add(getClass().getClassLoader().loadClass(publishType));
      }

      String id = null;

      if(parserContext.getRegistry().containsBeanDefinition(Const.FACTORY_MARKER) == false){
        id = UUID.randomUUID().toString();
        parserContext.getRegistry().registerBeanDefinition(Const.FACTORY_MARKER, BeanDefinitionBuilder.genericBeanDefinition(Marker.class).setScope(
            BeanDefinition.SCOPE_PROTOTYPE).addConstructorArgValue(id).getBeanDefinition());
      } else {
        id = (String) parserContext.getRegistry().getBeanDefinition(Const.FACTORY_MARKER).getConstructorArgumentValues().getArgumentValue(0, String.class).getValue();
      }

      for(Class cls : classesToPublish){
        PublishedBeanRegistry.registerBean(beanDefinitionHolder.getBeanName(), cls, id);
      }

    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find class for publish type: "+publishType+" specified on publish of bean id: "+beanDefinitionHolder.getBeanName(), e);
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
