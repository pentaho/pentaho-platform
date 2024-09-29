/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.plugin.services.security.userrole;

import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.EhCacheBasedUserCache;

public class PentahoEhCacheBasedUserCache extends EhCacheBasedUserCache {

  private static final Log logger = LogFactory.getLog( PentahoEhCacheBasedUserCache.class );
  private boolean caseSensitive = false;

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public void setCaseSensitive( boolean caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  @Override public UserDetails getUserFromCache( String username ) {
    return super.getUserFromCache( caseSensitive ? username : username.toLowerCase() );
  }

  @Override public void putUserInCache( UserDetails user ) {
    Element element = new Element( caseSensitive ? user.getUsername() : user.getUsername().toLowerCase(), user );
    if ( logger.isDebugEnabled() ) {
      logger.debug( "Cache put: " + element.getKey() );
    }

    this.getCache().put( element );
  }

  @Override public void removeUserFromCache( String username ) {
    super.removeUserFromCache( caseSensitive ? username : username.toLowerCase() );
  }
}
