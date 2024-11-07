/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

/**
 * Takes as input the string that defines a <code>UserMap</code>. When Spring instantiates this bean, it outputs a
 * <code>UserRoleListEnhancedUserMap</code>.
 * 
 * <p>
 * This class allows a string that defines a <code>UserMap</code> to be defined once in Spring beans XML, then used by
 * multiple client beans that need access to user to role mappings.
 * </p>
 * 
 * <p>
 * This class is necessary since <code>UserMap</code> does not define a constructor or setter necessary to populate a
 * <code>UserMap</code> bean, nor does it provide any way to extract its mappings once created.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 *              &lt;bean id=&quot;userMap&quot; class=&quot;java.lang.String&quot;&gt;
 *                &lt;constructor-arg type=&quot;java.lang.String&quot;&gt;
 *                  &lt;value&gt;
 *                    &lt;![CDATA[
 *                    admin=password,Admin,ceo,Authenticated
 *                    ...
 *                    ]]&gt;
 *                  &lt;/value&gt;
 *                &lt;/constructor-arg&gt;
 *              &lt;/bean&gt;
 * 
 *             &lt;bean id=&quot;userRoleListEnhancedUserMapFactoryBean&quot;
 *               class=&quot;org.pentaho.security.UserRoleListEnhancedUserMapFactoryBean&quot;&gt;
 *               &lt;property name=&quot;userMap&quot; ref=&quot;userMap&quot; /&gt;
 *             &lt;/bean&gt;
 * 
 *              &lt;bean id=&quot;inMemoryUserRoleListService&quot;
 *                class=&quot;org.pentaho.security.InMemoryDaoUserDetailsRoleListImpl&quot;&gt;
 *                &lt;property name=&quot;userRoleListEnhancedUserMap&quot;
 *                  ref=&quot;userRoleListEnhancedUserMapFactoryBean&quot; /&gt;
 *                &lt;property name=&quot;allAuthorities&quot;&gt;
 *                  &lt;list&gt;
 *                    &lt;bean class=&quot;org.springframework.security.GrantedAuthorityImpl&quot;&gt;
 *                      &lt;constructor-arg value=&quot;Authenticated&quot; /&gt;
 *                      ...
 *                    &lt;/bean&gt;
 *                  &lt;/list&gt;
 *                &lt;/property&gt;
 *              &lt;/bean&gt;
 * </pre>
 * 
 * @author mlowery
 * @see UserMapFactoryBean
 */
public class UserRoleListEnhancedUserMapFactoryBean implements FactoryBean {

  /*
   * The user map which will be processed by property editor.
   */
  private Properties userMap;

  public Object getObject() throws Exception {
    UserRoleListEnhancedUserMap userRoleListEnhanceduserMap = new UserRoleListEnhancedUserMap();
    UserRoleListEnhancedUserMapEditor.addUsersFromProperties( userRoleListEnhanceduserMap, userMap );
    return userRoleListEnhanceduserMap;
  }

  public Class getObjectType() {
    return UserRoleListEnhancedUserMap.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setUserMap( final Properties userMap ) {
    this.userMap = userMap;
  }
}
