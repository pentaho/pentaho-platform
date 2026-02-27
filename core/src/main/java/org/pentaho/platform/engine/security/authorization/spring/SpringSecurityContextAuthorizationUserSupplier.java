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

package org.pentaho.platform.engine.security.authorization.spring;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.function.Supplier;

public class SpringSecurityContextAuthorizationUserSupplier implements Supplier<IAuthorizationUser> {
  @NonNull
  @Override
  public IAuthorizationUser get() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Assert.notNull( authentication, "No authentication found in the security context" );

    return new SpringAuthenticationAuthorizationUser( authentication );
  }
}
