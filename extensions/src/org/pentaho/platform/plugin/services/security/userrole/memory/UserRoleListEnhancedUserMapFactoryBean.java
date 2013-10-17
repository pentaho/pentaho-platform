/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.springframework.beans.factory.FactoryBean;

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
   * The user map text which will be processed by property editor.
   */
  private String userMap;

  public Object getObject() throws Exception {
    UserRoleListEnhancedUserMapEditor userRoleListEnhancedUserMapEditor = new UserRoleListEnhancedUserMapEditor();
    userRoleListEnhancedUserMapEditor.setAsText( userMap );
    return userRoleListEnhancedUserMapEditor.getValue();
  }

  public Class getObjectType() {
    return UserRoleListEnhancedUserMap.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setUserMap( final String userMap ) {
    this.userMap = userMap;
  }
}
