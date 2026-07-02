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


package org.pentaho.platform.security.userroledao;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

public class DefaultTenantedPrincipleNameResolver implements ITenantedPrincipleNameResolver {

  public static final String DEFAULT_DELIMETER = "-";
  public static final String ALTERNATE_DELIMETER = "_";

  public boolean userNameNaturallyContainsEmbeddedTenantName = false;
  private String delimeter = DEFAULT_DELIMETER;
  private boolean principalNameFollowsTenantName = false;

  public DefaultTenantedPrincipleNameResolver() {
  }

  public DefaultTenantedPrincipleNameResolver( String delimiter ) {
    setDelimeter( delimiter );
  }

  public ITenant getTenant( String principalId ) {
    String tenantName = null;
    int delimiterIndex = principalId.lastIndexOf( getDelimeter() + "/" );
    if ( delimiterIndex >= 0 ) {
      tenantName =
          ( getUserNameFollowsTenantName() ? principalId.substring( 0, delimiterIndex - 1 ) : principalId
              .substring( delimiterIndex + 1 ) );
      if ( !isTenantValid( tenantName ) ) {
        tenantName = null;
      }
    }
    return new Tenant( tenantName, true );
  }

  public String getPrincipleName( String principalId ) {
    String userName = principalId;
    int delimiterIndex = principalId.lastIndexOf( getDelimeter() + "/" );
    if ( delimiterIndex >= 0 ) {
      if ( getUserNameNaturallyContainsEmbeddedTenantName() ) {
        userName = principalId;
      } else {
        userName =
            ( getUserNameFollowsTenantName() ? principalId.substring( delimiterIndex + 1 ) : principalId.substring( 0,
                delimiterIndex ) );
        if ( getTenant( principalId ).getId() == null ) {
          userName = principalId;
        }
      }
    }
    return userName;
  }

  public String getPrincipleId( ITenant tenant, String principleName ) {
    String id = getDelimeter();
    if ( ( tenant == null ) || ( tenant.getId() == null ) ) {
      id = principleName;
    } else {
      id =
          principalNameFollowsTenantName ? tenant.getId() + getDelimeter() + principleName : principleName
              + getDelimeter() + tenant.getId();
    }
    return id;
  }

  public boolean getUserNameNaturallyContainsEmbeddedTenantName() {
    return userNameNaturallyContainsEmbeddedTenantName;
  }

  public void setUserNameNaturallyContainsEmbeddedTenantName( boolean userNameNaturallyContainsEmbeddedTenantName ) {
    this.userNameNaturallyContainsEmbeddedTenantName = userNameNaturallyContainsEmbeddedTenantName;
  }

  public String getDelimeter() {
    return delimeter;
  }

  public void setDelimeter( String delimeter ) {
    this.delimeter = delimeter;
  }

  public boolean getUserNameFollowsTenantName() {
    return principalNameFollowsTenantName;
  }

  public void setUserNameFollowsTenantName( boolean userNameFollowsTenantName ) {
    this.principalNameFollowsTenantName = userNameFollowsTenantName;
  }

  @Override
  public boolean isValid( String principleId ) {
    if ( principleId.contains( getDelimeter() ) ) {
      int pentahoRootFolderNameIndex = principleId.lastIndexOf( ServerRepositoryPaths.getPentahoRootFolderName() );
      if ( pentahoRootFolderNameIndex <= 1 ) {
        return false;
      }
      String delim = principleId.substring( pentahoRootFolderNameIndex-2, pentahoRootFolderNameIndex-1 );
      if ( delim.equals( getDelimeter() ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean isTenantValid( String tenantName ) {
    return tenantName != null && tenantName.contains( ServerRepositoryPaths.getPentahoRootFolderName() );
  }
}
