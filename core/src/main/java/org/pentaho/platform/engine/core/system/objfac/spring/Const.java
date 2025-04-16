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

/**
 * Constants used by Hitachi Vantara Spring extensions
 *
 * User: nbaker Date: 3/6/13
 */
public interface Const {
  String ATTRIBUTES = "attributes";
  String ATTR = "attr";
  String KEY = "key";
  String VALUE = "value";
  String SCHEMA_TAG_ATTRIBUTES = "attributes";
  String SCHEMA_TAG_LIST = "list";
  String SCHEMA_TAG_BEAN = "bean";
  String SCHEMA_PUBLISH = "publish";
  String FACTORY_MARKER = "PublishedBeanRegistryMarker";

  /**
   * The bean identifier of the owner plugin marker registered with Spring bean factories belonging to a Pentaho plugin.
   * <p>
   * A bean of class {@link String} is defined in the Spring Bean Factory having as value the owner plugin's id. This is
   * done by the plugin manager when creating a plugin's Spring Bean Factory.
   * <p>
   * This bean id can be used as bean reference in a Spring configuration file to obtain the id of the owner plugin of a
   * Spring Bean Factory.
   * <p>
   * Additionally, this bean id is used to pass the plugin id information to an object reference attribute, named
   * {@link #PUBLISHER_PLUGIN_ID_ATTRIBUTE} to every bean published to the Pentaho system, via {@code <pen:publish>}.
   * This attribute can later be used to determine the source plugin of a bean, especially for supporting management and
   * administration functionality (e.g. determine the plugin that registered a certain authorization action).
   */
  String OWNER_PLUGIN_ID_BEAN = "OwnerPluginId";

  /**
   * The name of the object reference attribute that contains the identifier of the plugin that published the bean to
   * the Pentaho system.
   */
  String PUBLISHER_PLUGIN_ID_ATTRIBUTE = "publisher-plugin-id";
}
