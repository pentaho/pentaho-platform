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


package org.pentaho.platform.plugin.services.metadata;

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
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This is the platform implementation which implements security. NOTE: this class will be moved after integration
 * testing
 *
 * @author David Kincade
 */
public class SecurityAwarePentahoMetadataDomainRepository extends PentahoMetadataDomainRepository {
  private static final Log logger = LogFactory.getLog( SecurityAwarePentahoMetadataDomainRepository.class );
  public static final int[] ACCESS_TYPE_MAP = new int[]{ IAclHolder.ACCESS_TYPE_READ, IAclHolder.ACCESS_TYPE_WRITE,
    IAclHolder.ACCESS_TYPE_UPDATE, IAclHolder.ACCESS_TYPE_DELETE, IAclHolder.ACCESS_TYPE_ADMIN,
    IAclHolder.ACCESS_TYPE_ADMIN };

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
    IPentahoSession auth = PentahoSessionHolder.getSession();
    if ( auth == null ) {
      logger.info( Messages.getInstance().getString( "SecurityAwareCwmSchemaFactory.INFO_AUTH_NULL_CONTINUE" ) ); //$NON-NLS-1$
      return "FALSE()"; //$NON-NLS-1$
    }
    String username = auth.getName();
    HashSet<String> roles = null;
    roles = new HashSet<String>(  );
    for ( GrantedAuthority role : (List<GrantedAuthority>) auth.getAttribute( "roles" ) ) {
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
