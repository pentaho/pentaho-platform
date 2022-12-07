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
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.action.kettle;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp.PoolingDataSource;
import org.pentaho.di.core.database.CachedManagedDataSourceInterface;

import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PoolingManagedDataSource extends PoolingDataSource implements CachedManagedDataSourceInterface {

    private boolean isExpired;
    private String poolConfigHash;
    private List<String> usedBy;

    public PoolingManagedDataSource(){
        usedBy = new ArrayList<>();
    }

    @Override
    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public String calculateDSHash( Object dataSource ) {
        return DigestUtils.md5Hex( dataSource.toString() );
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public void setInUseBy( String ownerName ) {
        if ( !usedBy.contains( ownerName ) ){
            usedBy.add( ownerName );
        }
    }

    @Override
    public void tryInvalidateDataSource( String invalidatedBy ) {
        usedBy.remove( invalidatedBy );
    }

    public void setConfigHash( String poolConfig ) {
        poolConfigHash = calculateDSHash( poolConfig );
    }

    public String getPoolConfigHash() {
        return poolConfigHash;
    }

    public boolean hasSameConfig( String config ) {
        return poolConfigHash.equals( calculateDSHash( config ) );
    }

    @Override
    public boolean isInUse() {
        return !usedBy.isEmpty();
    }

    @Override
    public void expire() {
        isExpired = true;
    }
}
