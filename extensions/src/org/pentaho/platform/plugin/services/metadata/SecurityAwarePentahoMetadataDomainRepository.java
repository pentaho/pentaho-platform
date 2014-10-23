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

package org.pentaho.platform.plugin.services.metadata;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.RowLevelSecurity;
import org.pentaho.metadata.util.RowLevelSecurityHelper;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

/**
 * This is the platform implementation which implements security. NOTE: this class will be moved after integration
 * testing
 *
 * @author David Kincade
 */
public class SecurityAwarePentahoMetadataDomainRepository extends PentahoMetadataDomainRepository {
  private static final Log logger = LogFactory.getLog( SecurityAwarePentahoMetadataDomainRepository.class );
  public static final int[] ACCESS_TYPE_MAP = new int[]{IAclHolder.ACCESS_TYPE_READ, IAclHolder.ACCESS_TYPE_WRITE,
    IAclHolder.ACCESS_TYPE_UPDATE, IAclHolder.ACCESS_TYPE_DELETE, IAclHolder.ACCESS_TYPE_ADMIN,
    IAclHolder.ACCESS_TYPE_ADMIN};

  public SecurityAwarePentahoMetadataDomainRepository( final IUnifiedRepository repository ) {
    super( repository );
  }

  public IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  @Override
  public String generateRowLevelSecurityConstraint( LogicalModel model ) {
    RowLevelSecurity rls = model.getRowLevelSecurity();
    if ( rls == null || rls.getType() == RowLevelSecurity.Type.NONE ) {
      return null;
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if ( auth == null ) {
      logger.info( Messages.getInstance().getString( "SecurityAwareCwmSchemaFactory.INFO_AUTH_NULL_CONTINUE" ) ); //$NON-NLS-1$
      return "FALSE()"; //$NON-NLS-1$
    }
    String username = auth.getName();
    HashSet<String> roles = null;
    roles = new HashSet<String>();
    for ( GrantedAuthority role : auth.getAuthorities() ) {
      roles.add( role.getAuthority() );
    }

    RowLevelSecurityHelper helper = new SessionAwareRowLevelSecurityHelper();
    return helper.getOpenFormulaSecurityConstraint( rls, username, new ArrayList<String>( roles ) );
  }

  @Override
  public boolean hasAccess( final int accessType, final IConcept aclHolder ) {
    boolean result = true;
    if ( aclHolder != null ) {
      PentahoMetadataAclHolder newHolder = new PentahoMetadataAclHolder( aclHolder );
      int mappedActionOperation = ACCESS_TYPE_MAP[accessType];
      result = SecurityHelper.getInstance().hasAccess( newHolder, mappedActionOperation, getSession() );
    } else if ( accessType == ACCESS_TYPE_SCHEMA_ADMIN ) {
      result = SecurityHelper.getInstance().isPentahoAdministrator( getSession() );
    }
    return result;
  }
}
