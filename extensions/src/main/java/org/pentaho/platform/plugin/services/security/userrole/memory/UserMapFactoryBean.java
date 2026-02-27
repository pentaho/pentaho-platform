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


package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.springframework.beans.factory.FactoryBean;

/**
 * Takes as input the string that defines a <code>UserMap</code>. When Spring instantiates this bean, it outputs a
 * <code>UserMap</code>.
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
 *     &lt;bean id=&quot;userMap&quot; class=&quot;java.lang.String&quot;&gt;
 *       &lt;constructor-arg type=&quot;java.lang.String&quot;&gt;
 *         &lt;value&gt;
 *           &lt;![CDATA[
 *           admin=password,Admin,ceo,Authenticated
 *           ...
 *           ]]&gt;
 *         &lt;/value&gt;
 *       &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * 
 *     &lt;bean id=&quot;userMapFactoryBean&quot;
 *       class=&quot;org.pentaho.security.UserMapFactoryBean&quot;&gt;
 *       &lt;property name=&quot;userMap&quot; ref=&quot;userMap&quot; /&gt;
 *     &lt;/bean&gt;
 * 
 *     &lt;bean id=&quot;memoryAuthenticationDao&quot;
 *       class=&quot;org.springframework.security.userdetails.memory.InMemoryDaoImpl&quot;&gt;
 *       &lt;property name=&quot;userMap&quot; ref=&quot;userMapFactoryBean&quot; /&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author mlowery
 * @see UserRoleListEnhancedUserMapFactoryBean
 */
public class UserMapFactoryBean implements FactoryBean {
  /*
   * The user map text which will be processed by property editor.
   */
  private String userMap;

  public Object getObject() throws Exception {
    PentahoUserMapEditor userMapEditor = new PentahoUserMapEditor();
    userMapEditor.setAsText( userMap );
    return userMapEditor.getValue();
  }

  public Class getObjectType() {
    return PentahoUserMap.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setUserMap( final String userMap ) {
    this.userMap = userMap;
  }

}
