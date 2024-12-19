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

/**
 * Datasource Type
 */
public enum PentahoDataSourceType {
    METADATA( "metadata" ),
    DATA_SOURCE_WIZARD( "dsw" );

    private String type;

    private PentahoDataSourceType( String type ) {
        this.type = type;
    }

    @Override
    public  String toString() {
        return type;
    }
}
