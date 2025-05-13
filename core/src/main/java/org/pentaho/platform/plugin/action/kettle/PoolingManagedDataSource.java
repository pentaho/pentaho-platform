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


package org.pentaho.platform.plugin.action.kettle;


import org.apache.commons.dbcp2.PoolingDataSource;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.database.CachedManagedDataSourceInterface;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceHelper;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PoolingManagedDataSource extends PoolingDataSource implements CachedManagedDataSourceInterface {

    private boolean isExpired;
    private String poolConfigHash;
    private List<String> usedBy;

    public PoolingManagedDataSource( IDatabaseConnection databaseConnection, IDatabaseDialect dialect )
      throws Exception {
        super( PooledDatasourceHelper.createGenericPool( databaseConnection, dialect,
            databaseConnection.getConnectionPoolingProperties() ) );

        isExpired = false;
        poolConfigHash = "";
        usedBy = new ArrayList<>();

        Map<String, String> attributes = databaseConnection.getConnectionPoolingProperties();

        /*
         * All of this is wrapped in a DataSource, which client code should already know how to handle (since it's the
         * same class of object they'd fetch via the container's JNDI tree
         */
        setHash( databaseConnection.calculateHash() );
        if ( attributes.containsKey( IDBDatasourceService.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED ) ) {
            setAccessToUnderlyingConnectionAllowed( Boolean.parseBoolean( attributes
                    .get( IDBDatasourceService.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED ) ) );
        }
    }

    @Override
    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public void tryInvalidateDataSource( String invalidatedBy ) {
        expire();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public void addInUseBy( String ownerName ) {
        if ( !usedBy.contains( ownerName ) ){
            usedBy.add( ownerName );
        }
    }

    @Override
    public void removeInUseBy( String invalidatedBy ) {
        usedBy.remove( invalidatedBy );
    }

    @Override
    public void setHash( String poolConfig ) {
        poolConfigHash = poolConfig;
    }

    @Override
    public String getHash() {
        return poolConfigHash;
    }

    public boolean hasSameConfig( String poolConfigHash ) {
        return this.poolConfigHash.equals( poolConfigHash );
    }

    @Override
    public boolean isInUse() {
        return !usedBy.isEmpty();
    }

    @Override
    public void setInUseBy( List<String> ownerList ) {
        usedBy = ownerList;
    }

    @Override
    public void expire() {
        isExpired = true;
    }
}
