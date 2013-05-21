package org.pentaho.platform.engine.core.system.objfac.spring;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring implementation of {@link IPentahoObjectReference}
 *
 * {@inheritDoc}
 *
 * User: nbaker
 * Date: 1/16/13
 */
public class SpringPentahoObjectReference<T> implements IPentahoObjectReference {

  private ConfigurableApplicationContext context;

  private String name;

  private final Class<T> clazz;

  private IPentahoSession session;

  private final SpringBeanAttributes attributes;

  private static String PRIORITY = "priority";

  public SpringPentahoObjectReference(ConfigurableApplicationContext context, String name, Class<T> clazz,
      IPentahoSession session, BeanDefinition beanDef) {
    this.context = context;
    this.name = name;
    this.clazz = clazz;
    this.session = session;
    this.attributes = new SpringBeanAttributes(beanDef);
  }

  @Override
  public Object getObject() {
    SpringScopeSessionHolder.SESSION.set(session);
    Object obj = context.getBeanFactory().getBean(name);
    SpringScopeSessionHolder.SESSION.set(null);

    if (obj instanceof IPentahoInitializer) {
      ((IPentahoInitializer) obj).init(session);
    }
    return obj;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SpringPentahoObjectReference that = (SpringPentahoObjectReference) o;

    if (!clazz.equals(that.clazz))
      return false;
    if (!name.equals(that.name))
      return false;
    if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null)
      return false;
    if (session != null ? !session.equals(that.session) : that.session != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + clazz.hashCode();
    result = 31 * result + (session != null ? session.hashCode() : 0);
    result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(Object o) {
    if (o == null || o instanceof IPentahoObjectReference == false) {
      return -1;
    }
    IPentahoObjectReference ref = (IPentahoObjectReference) o;
    int pri1 = extractPriority(this);
    int pri2 = extractPriority(ref);
    if (pri1 == pri2) {
      return 0;
    } else if (pri1 > pri2) {
      return 1;
    } else {
      return -1;
    }

  }

  private int extractPriority(IPentahoObjectReference ref) {
    if (ref == null || ref.getAttributes() == null || !ref.getAttributes().containsKey(PRIORITY)) {
      // return default
      return IPentahoObjectFactory.DEFAULT_PRIORTIY;
    }

    try {
      return Integer.parseInt(ref.getAttributes().get(PRIORITY).toString());
    } catch (NumberFormatException e) {
      // return default
      return IPentahoObjectFactory.DEFAULT_PRIORTIY;
    }
  }

  /**
   * Hashmap backed by a Spring BeanDefinition
   */
  private static class SpringBeanAttributes extends HashMap<String, Object> {

    /**
     * 
     */
    private static final long serialVersionUID = -5790844158879001752L;

    public SpringBeanAttributes(final BeanDefinition definition) {
      for (String s : definition.attributeNames()) {
        this.put(s, definition.getAttribute(s));
      }
    }

  }

}
