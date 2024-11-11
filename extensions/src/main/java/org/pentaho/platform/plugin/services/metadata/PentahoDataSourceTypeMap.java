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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holder of information relating to the mappings between Pentaho Metadata Domain datasource types and
 * Pentaho Metadata Domain IDs.
 * <p></p>
 * Map :{
 *    "TYPE-1 ": ["domain-7", "domain-34"]
 *    "TYPE-2" : ["domain-W", "domain-V", "domain-"F"]
 *    }
 *
 */
class PentahoDataSourceTypeMap {
  private static final Log log = LogFactory.getLog( PentahoDataSourceTypeMap.class );

  /**
   * Store mappings for datasource type -> collections of domain Ids
   */
  private Map<String, Set<String>> mapDataSourceTypeToDomainIds = new HashMap<>();

  /**
   * Remove all domain Ids from datasource types.
   */
  public void reset() {
    log.debug( "reset()" );
    mapDataSourceTypeToDomainIds.clear();
  }

  /**
   * Add domain Id to a datasource type.
   * @param datasourceType
   * @param domainId
   */
  public void addDatasourceType( String datasourceType, final String domainId ) {
    log.debug( String.format( "addDatasourceType( datasourceType: %s, domainId: %s )", datasourceType, domainId ) );
    Set<String> setDomainIds = mapDataSourceTypeToDomainIds.get( datasourceType );
    if ( setDomainIds == null ) {
      setDomainIds = new HashSet<>();
    }
    setDomainIds.add( domainId );
    mapDataSourceTypeToDomainIds.put( datasourceType, setDomainIds );
  }

  /**
   * Remove a single domain Id.
   * @param domainId
   */
  public void deleteDomainId( final String domainId ) {
    log.debug( String.format( "deleteDomainId( domainId: %s )", domainId ) );
    mapDataSourceTypeToDomainIds.values().stream().forEach( setDomainIds -> setDomainIds.remove( domainId ) );
  }

  /**
   * Retrieve all domain Ids for a specific datasource type
   * @param datasourceType
   * @return
   */
  public Set<String> getDatasourceType( String datasourceType ) {
    log.debug( String.format( "getDatasourceType( datasourceType: %s )", datasourceType ) );
    return mapDataSourceTypeToDomainIds.containsKey( datasourceType )
            ? mapDataSourceTypeToDomainIds.get( datasourceType )
            : Collections.emptySet();
  }

}
