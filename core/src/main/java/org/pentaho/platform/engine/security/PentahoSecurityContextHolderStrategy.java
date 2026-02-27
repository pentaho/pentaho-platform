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
