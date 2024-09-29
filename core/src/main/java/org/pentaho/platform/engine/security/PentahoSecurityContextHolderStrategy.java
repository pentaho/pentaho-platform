/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Used by Spring Security's {@link org.springframework.security.core.context.SecurityContextHolder} to govern the creation
 * and scope of a {@link SecurityContext}. This implementation is, with respect scope, the same as
 * org.springframework.security.context.InheritableThreadLocalSecurityContextHolderStrategy. The SecurityContext
 * implementations factoried by this class are of our own type {@link PentahoSecurityContextImpl} which manages
 * Authentication in it's own InheritableThreadLocal
 *
 *
 * Created by nbaker on 6/6/14.
 */
public class PentahoSecurityContextHolderStrategy implements SecurityContextHolderStrategy {


  private static InheritableThreadLocal<SecurityContext> context = new InheritableThreadLocal<>();

  public SecurityContext getContext() {
    if ( context.get() == null ) {
      context.set( new PentahoSecurityContextImpl() );
    }

    return context.get();
  }

  public void setContext( SecurityContext sContext ) {
    context.set( sContext );
  }

  @Override public void clearContext() {
    context.remove();
  }

  public static final class PentahoSecurityContextImpl extends SecurityContextImpl {
    InheritableThreadLocal<Authentication> authentication = new InheritableThreadLocal<>();

    @Override public Authentication getAuthentication() {
      return authentication.get();
    }

    @Override public void setAuthentication( Authentication a ) {
      authentication.set( a );
    }

    public int hashCode() {
      if ( getAuthentication() == null ) {
        return -1;
      } else {
        return getAuthentication().hashCode();
      }
    }


    public String toString() {
      return "Authentication: " + this.getAuthentication();
    }
  }

  @Override
  public SecurityContext createEmptyContext() {
    return new PentahoSecurityContextImpl();
  }

}
