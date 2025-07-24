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


package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * 
 * @author rmansoor
 * 
 */
public class SimpleMapCacheManager implements ICacheManager {

  protected static final Log logger = LogFactory.getLog( SimpleMapCacheManager.class );
  private final Map simpleMap;
  private static SimpleMapCacheManager _instance = new SimpleMapCacheManager();

  public static SimpleMapCacheManager getInstance() {
    return ( _instance );
  }

  public SimpleMapCacheManager() {
    simpleMap = Collections.synchronizedMap( new HashMap() );
    PentahoSystem.addLogoutListener( this );
  } // ctor

  public void cacheStop() {
  }

  public boolean cacheEnabled( String region ) {
    return true;
  }

  public void onLogout( final IPentahoSession session ) {
    removeRegionCache( session.getName() );
  }

  public boolean addCacheRegion( String region, Properties cacheProperties ) {
    return true;
  }

  public boolean addCacheRegion( String region ) {
    return true;
  }

  public void clearRegionCache( String region ) {
    removeFromMap( region );
  }

  public void removeRegionCache( String region ) {
    removeFromMap( region );
  }

  public void putInRegionCache( String region, Object key, Object value ) {
    simpleMap.put( getCorrectedKey( region, key ), value );
  }

  public Object getFromRegionCache( String region, Object key ) {
    return simpleMap.get( getCorrectedKey( region, key ) );
  }

  public Set getAllEntriesFromRegionCache( String region ) {
    if ( simpleMap != null ) {
      return simpleMap.entrySet();
    } else {
      return null;
    }
  }

  public Set getAllKeysFromRegionCache( String region ) {
    if ( simpleMap != null ) {
      return simpleMap.keySet();
    } else {
      return null;
    }
  }

  public List getAllValuesFromRegionCache( String region ) {
    return getFromMap( region );
  }

  public void removeFromRegionCache( String region, Object key ) {
    simpleMap.remove( getCorrectedKey( region, key ) );
  }

  public boolean cacheEnabled() {
    return true;
  }

  public void clearCache() {
    simpleMap.clear();
  }

  public Object getFromGlobalCache( Object key ) {
    return getFromRegionCache( GLOBAL, key );
  }

  public Object getFromSessionCache( IPentahoSession session, String key ) {
    return getFromRegionCache( SESSION, getCorrectedKey( session, key ) );
  }

  public void killSessionCache( IPentahoSession session ) {
    removeFromMap( session.getId() );
  }

  public void killSessionCaches() {
    removeRegionCache( SESSION );
  }

  public void putInGlobalCache( Object key, Object value ) {
    putInRegionCache( GLOBAL, key, value );
  }

  public void putInSessionCache( IPentahoSession session, String key, Object value ) {
    putInRegionCache( SESSION, getCorrectedKey( session, key ), value );
  }

  public void removeFromGlobalCache( Object key ) {
    removeFromRegionCache( GLOBAL, key );
  }

  public void removeFromSessionCache( IPentahoSession session, String key ) {
    removeFromRegionCache( SESSION, getCorrectedKey( session, key ) );
  }

  private String getCorrectedKey( final IPentahoSession session, final String key ) {
    String sessionId = session.getId();
    String newKey = null;
    if ( sessionId != null ) {
      newKey = sessionId + "\t" + key; //$NON-NLS-1$
    }
    return newKey;
  }

  private String getCorrectedKey( final String region, final Object key ) {
    return region + "\t" + key; //$NON-NLS-1$
  }

  private List getFromMap( String id ) {
    List list = new ArrayList();
    if ( simpleMap != null ) {
      String keyId = id + "\t";
      Iterator it = simpleMap.entrySet().iterator();
      while ( it.hasNext() ) {
        Map.Entry entry = (Map.Entry) it.next();
        String key = ( entry.getKey() != null ) ? entry.getKey().toString() : ""; //$NON-NLS-1$
        if ( key.startsWith( keyId ) ) {
          list.add( entry.getValue() );
        }
      }
    }
    return list;
  }

  private void removeFromMap( String id ) {
    if ( simpleMap != null ) {
      Iterator it = simpleMap.entrySet().iterator();
      String keyId = id + "\t";
      while ( it.hasNext() ) {
        Map.Entry entry = (Map.Entry) it.next();
        String key = entry.getKey() != null ? (String) entry.getKey() : ""; //$NON-NLS-1$
        if ( key.startsWith( keyId ) ) {
          it.remove();
        }
      }
    }
  }
}
