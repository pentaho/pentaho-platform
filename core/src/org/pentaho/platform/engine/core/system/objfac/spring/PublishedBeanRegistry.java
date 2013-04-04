package org.pentaho.platform.engine.core.system.objfac.spring;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.*;

/**
 * A Registry for published Spring beans. Also allows for looking up the names of all beans registered for a given type in
 * the given Spring bean factory
 *
 * User: nbaker
 * Date: 3/27/13
 */
public class PublishedBeanRegistry {

  private static Map<Object, Map<Class<?>, List<String>>> classToBeanMap = Collections.synchronizedMap(
      new WeakHashMap<Object, Map<Class<?>, List<String>>>()
  );

  /**
   * Register a bean for the given class. The factoryMarker is a UUID associated with the originating BeanFactory
   *
   * @param beanName
   * @param clazz
   * @param factoryMarker
   */
  public static void registerBean(String beanName, Class<?> clazz, Marker factoryMarker) {
    if(beanName == null){
      throw new IllegalArgumentException("Bean name cannot be null");
    }
    if(clazz == null){
      throw new IllegalArgumentException("Class cannot be null");
    }
    if(factoryMarker == null){
      throw new IllegalArgumentException("factoryMarker cannot be null");
    }

    Map<Class<?>, List<String>> registryMap = classToBeanMap.get(factoryMarker);
    if(registryMap == null){
      registryMap = new WeakHashMap<Class<?>, List<String>>();
      classToBeanMap.put(factoryMarker, registryMap);
    }
    List<String> beansImplementingType = registryMap.get(clazz);
    if(beansImplementingType == null){
      beansImplementingType = Collections.synchronizedList(new ArrayList<String>());
      registryMap.put(clazz, beansImplementingType);
    }
    beansImplementingType.add(beanName);
  }

  /**
   * Returns the names of beans registered in the given factory for the specified type
   *
   * @param registry
   * @param type
   * @return
   */
  public static String[] getBeanNamesForType(ListableBeanFactory registry, Class<?> type) {

    if(registry == null){
      throw new IllegalArgumentException("Registry cannot be null");
    }
    if(type == null){
      throw new IllegalArgumentException("Type cannot be null");
    }
    if(registry.containsBean(Const.FACTORY_MARKER) == false){
      return new String[]{};
    }

    Map<Class<?>, List<String>> map = classToBeanMap.get(registry.getBean(Const.FACTORY_MARKER));
    if(map == null){
      return new String[]{};
    }

    List<String> beansImplementingType = map.get(type);
    if(beansImplementingType == null){
      return new String[]{};
    }
    return beansImplementingType.toArray(new String[beansImplementingType.size()]);
  }

}
