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

package org.pentaho.platform.plugin.action.mondrian.mapper;

/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created December 12, 2009
 * @author Marc Batchelor
 */

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Iterator;

public class MondrianUserSessionUserRoleListMapper extends MondrianAbstractPlatformUserRoleMapper implements
    InitializingBean {

  private String sessionProperty;

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( sessionProperty );
  }

  //
  // This class doesn't necessarily map roles so much as extract rolss from the session,
  // and supply them for downstream use. You must provide the sessionProperty to use
  // for the roles to pass along to the Mondrian engine
  //
  @Override
  @SuppressWarnings( "unchecked" )
  protected String[] mapRoles( String[] mondrianRoles, String[] platformRoles ) {
    //
    // Note - this mapper doesn't need the mondrian catalog for mapping
    // or in fact any of the passed parameters. The roles for this
    // user come out of a session variable.
    //
    IPentahoSession session = PentahoSessionHolder.getSession();
    String[] rtn = null;
    Object sessionObj = session.getAttribute( sessionProperty );
    if ( sessionObj != null ) {
      if ( sessionObj instanceof String[] ) {
        // If it's already a string array, return it
        rtn = ( (String[]) sessionObj );
      } else if ( sessionObj instanceof Collection ) {
        // Any collection, iterate over it and use toString to
        // get roles... This will likely lead to no data cases, but
        // it's success oriented.
        Collection rolesColl = ( (Collection) sessionObj );
        rtn = new String[rolesColl.size()];
        Iterator it = rolesColl.iterator();
        int i = 0;
        while ( it.hasNext() ) {
          rtn[i] = it.next().toString();
          i++;
        }
      } else if ( sessionObj instanceof Object[] ) {
        // An Object array? Make it simple and toString
        // everything
        Object[] roleObjs = (Object[]) sessionObj;
        rtn = new String[roleObjs.length];
        for ( int i = 0; i < roleObjs.length; i++ ) {
          rtn[i] = roleObjs[i].toString();
        }
      } else {
        rtn = new String[] { sessionObj.toString() };
      }
    }

    return rtn;
  }

  public void setSessionProperty( String value ) {
    this.sessionProperty = value;
  }

  public String getSessionProperty() {
    return this.sessionProperty;
  }

}
