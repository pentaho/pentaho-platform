/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SecurityParameterProvider implements IParameterProvider {

  public static final List SecurityNames = new ArrayList( 4 );

  public static final List SecurityTypes = new ArrayList( 4 );

  private static final int PRINCIPAL_NAME = 0;

  private static final int PRINCIPAL_ROLES = 1;

  private static final int PRINCIPAL_AUTHENTICATED = 2;

  private static final int PRINCIPAL_IS_ADMINISTRATOR = 3;

  private static final int SYSTEM_ROLE_NAMES = 4;

  private static final int SYSTEM_USER_NAMES = 5;

  public static final String SCOPE_SECURITY = "security"; //$NON-NLS-1$

  private String listSeparator = ","; //$NON-NLS-1$

  private IPentahoSession session;

  static {
    SecurityParameterProvider.SecurityNames.add( "principalName" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityNames.add( "principalRoles" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityNames.add( "principalAuthenticated" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityNames.add( "principalAdministrator" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityNames.add( "systemRoleNames" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityNames.add( "systemUserNames" ); //$NON-NLS-1$

    SecurityParameterProvider.SecurityTypes.add( "string" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityTypes.add( "string-list" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityTypes.add( "string" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityTypes.add( "string" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityTypes.add( "string-list" ); //$NON-NLS-1$
    SecurityParameterProvider.SecurityTypes.add( "string-list" ); //$NON-NLS-1$
  }

  public SecurityParameterProvider( final IPentahoSession session ) {
    super();
    this.session = session;
  }

  public void setListSeparator( final String value ) {
    this.listSeparator = value;
  }

  public String getListSeparator() {
    return this.listSeparator;
  }

  public String getStringParameter( final String name, final String defaultValue ) {
    Object obj = getParameter( name );
    if ( obj != null ) {
      if ( obj instanceof List ) {
        return listToString( (List) obj );
      } else if ( obj instanceof String[] ) {
        return arrayToString( (String[]) obj );
      } else if ( obj instanceof GrantedAuthority[] ) {
        return arrayToString( (GrantedAuthority[]) obj );
      } else {
        return obj.toString();
      }
    }
    return defaultValue;
  }

  public String listToString( final List aList ) {
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < aList.size(); i++ ) {
      if ( aList.get( i ) != null ) {
        Object listObj = aList.get( i );
        if ( listObj instanceof GrantedAuthority ) {
          sb.append( i > 0 ? this.listSeparator : "" ).append( ( (GrantedAuthority) listObj ).getAuthority() ); //$NON-NLS-1$
        } else {
          sb.append( i > 0 ? this.listSeparator : "" ).append( listObj.toString() ); //$NON-NLS-1$
        }
      }
    }
    return sb.toString();
  }

  public String arrayToString( final String[] anArray ) {
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < anArray.length; i++ ) {
      if ( anArray[i] != null ) {
        sb.append( i > 0 ? this.listSeparator : "" ).append( anArray[i] ); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }

  public String arrayToString( final GrantedAuthority[] anArray ) {
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < anArray.length; i++ ) {
      if ( anArray[i] != null ) {
        sb.append( i > 0 ? this.listSeparator : "" ).append( anArray[i].getAuthority() ); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }

  public long getLongParameter( final String name, final long defaultValue ) {
    // No integer parameters supported
    return defaultValue;
  }

  public Date getDateParameter( final String name, final Date defaultValue ) {
    // No Date parameters supported
    return defaultValue;
  }

  public BigDecimal getDecimalParameter( final String name, final BigDecimal defaultValue ) {
    // No decimal parameters supported
    return defaultValue;
  }

  public Object[] getArrayParameter( final String name, final Object[] defaultValue ) {
    // No decimal parameters supported
    return defaultValue;
  }

  public String[] getStringArrayParameter( final String name, final String[] defaultValue ) {
    // No decimal parameters supported
    return defaultValue;
  }

  public Iterator getParameterNames() {
    return SecurityParameterProvider.SecurityNames.iterator();
  }

  public String getParameterType( final String name ) {
    int idx = SecurityParameterProvider.SecurityNames.indexOf( name );
    if ( idx >= 0 ) {
      return (String) SecurityParameterProvider.SecurityTypes.get( idx );
    }
    return null;
  }

  public Object getParameter( final String name ) {
    if ( name.startsWith( "principal" ) ) { //$NON-NLS-1$
      if ( name.equals( SecurityParameterProvider.SecurityNames.get( SecurityParameterProvider.PRINCIPAL_NAME ) ) ) {
        return getPrincipalName();
      } else if ( name
          .equals( SecurityParameterProvider.SecurityNames.get( SecurityParameterProvider.PRINCIPAL_ROLES ) ) ) {
        return getPrincipalRoles();
      } else if ( name.equals( SecurityParameterProvider.SecurityNames
          .get( SecurityParameterProvider.PRINCIPAL_AUTHENTICATED ) ) ) {
        return getPrincipalAuthenticated();
      } else if ( name.equals( SecurityParameterProvider.SecurityNames
          .get( SecurityParameterProvider.PRINCIPAL_IS_ADMINISTRATOR ) ) ) {
        return getPrincipalIsAdministrator();
      }
    } else {
      if ( name.equals( SecurityParameterProvider.SecurityNames.get( SecurityParameterProvider.SYSTEM_ROLE_NAMES ) ) ) {
        return getSystemRoleNames();
      } else if ( name.equals( SecurityParameterProvider.SecurityNames
          .get( SecurityParameterProvider.SYSTEM_USER_NAMES ) ) ) {
        return getSystemUserNames();
      }
    }
    return null;
  }

  private Authentication getAuthentication() {
    return SecurityHelper.getInstance().getAuthentication();
  }

  protected String getPrincipalName() {
    Authentication auth = getAuthentication();
    if ( auth != null ) {
      return auth.getName();
    }
    return null;
  }

  protected String getPrincipalAuthenticated() {
    Authentication auth = getAuthentication();
    if ( auth != null ) {
      return auth.isAuthenticated() ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    return "false"; //$NON-NLS-1$
  }

  protected String getPrincipalIsAdministrator() {
    return SecurityHelper.getInstance().isPentahoAdministrator( this.session ) ? "true" : "false"; //$NON-NLS-1$
    // //$NON-NLS-2$
  }

  protected Object getPrincipalRoles() {
    Authentication auth = getAuthentication();
    if ( auth != null ) {
      GrantedAuthority[] auths = auth.getAuthorities();
      if ( auths != null ) {
        List rtn = new ArrayList( auths.length );
        for ( GrantedAuthority element : auths ) {
          rtn.add( element.getAuthority() );
        }
        return rtn;
      } else {
        return new ArrayList();
      }
    }
    return null;
  }

  protected Object getSystemRoleNames() {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    if ( service != null ) {
      return service.getAllRoles();
    }
    return null;
  }

  protected Object getSystemUserNames() {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    if ( service != null ) {
      return service.getAllUsers();
    }
    return null;
  }

  public boolean hasParameter( String name ) {
    return this.getParameter( name ) != null;
  }
}
