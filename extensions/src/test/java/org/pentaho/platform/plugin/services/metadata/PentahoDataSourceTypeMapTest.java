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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PentahoDataSourceTypeMapTest {

    PentahoDataSourceTypeMap testInstance = new PentahoDataSourceTypeMap();

    private static final String TYPE_1 = "TYPE-1";

    private static final String TYPE_2 = "TYPE-2";

    @Before
    public void setup() {
        testInstance = createInstanceWithTestData();
    }

    @Test
    public void testReset() {

        // sanity check
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).isEmpty() );
        assertFalse( testInstance.getDatasourceType( TYPE_2 ).isEmpty() );

        // execute
        testInstance.reset();

        // verify
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).isEmpty() );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).isEmpty() );
    }

    @Test
    public void testAddDatasourceType() {

        // execute
        testInstance.addDatasourceType( TYPE_2, "domain-XY" );
        testInstance.addDatasourceType( TYPE_1, "domain-555" );
        testInstance.addDatasourceType( TYPE_1, "domain-001" );
        testInstance.addDatasourceType( TYPE_2, "domain-HE" );

        //verify
        assertEquals( 4, testInstance.getDatasourceType( TYPE_1 ).size() );
        assertEquals( 5, testInstance.getDatasourceType( TYPE_2 ).size() );

        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-555" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-001" ) );

        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-V" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-F" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-XY" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-HE" ) );

    }

    @Test
    public void testDeleteDomainId_InvalidInputs() {

        // testing deleting domain Id that should not exist
        testInstance.deleteDomainId( null );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // testing deleting domain Id that should not exist
        testInstance.deleteDomainId( "       " );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // testing deleting domain Id that should not exist
        testInstance.deleteDomainId( "" );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // testing deleting domain Id that should not exist
        testInstance.deleteDomainId( "DOES-NOT-EXIST" );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

    }

    @Test
    public void testDeleteDomainId_ValidInput() {

        // delete known domain Id
        testInstance.deleteDomainId( "domain-7" );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // second delete on domain Id should not raise errors
        testInstance.deleteDomainId( "domain-7" );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // delete known domain Id
        // second delete should not raise errors
        testInstance.deleteDomainId( "domain-34" );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // second delete on domain Id should not raise errors
        // map holding this type should be empty, still should not raise an error
        testInstance.deleteDomainId( "domain-34" );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );

        // delete known domain Id
        testInstance.deleteDomainId( "domain-V" );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-V" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-F" ) );

        // second delete on domain Id should not raise errors
        testInstance.deleteDomainId( "domain-V" );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-V" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-F" ) );

        // final checks
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-V" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-F" ) );

        assertEquals( 0, testInstance.getDatasourceType( TYPE_1 ).size() );
        assertEquals( 2, testInstance.getDatasourceType( TYPE_2 ).size() );

    }

    @Test
    public void testGetDatasourceType() {

        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-34" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-V" ) );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-F" ) );

        // sanity check
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "DOES-NOT-EXIST" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "domain-7777777" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( null ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "" ) );
        assertFalse( testInstance.getDatasourceType( TYPE_1 ).contains( "      " ) );

        assertNotNull( testInstance.getDatasourceType( null ) );
        assertNotNull( testInstance.getDatasourceType( "" ) );
        assertNotNull( testInstance.getDatasourceType( "    " ) );
        assertNotNull( testInstance.getDatasourceType( "DOES-NOT-EXIST" ) );
    }

    /**
     * For detailed test for class
     */
    @Test
    public void testScenario1() {

        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertEquals( 3, testInstance.getDatasourceType( TYPE_2 ).size() );

        testInstance.deleteDomainId( "domain-W" );

        assertFalse( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertEquals( 2, testInstance.getDatasourceType( TYPE_2 ).size() );

        testInstance.reset();
        testInstance.reset(); // trying second reset

        assertEquals( 0, testInstance.getDatasourceType( TYPE_2 ).size() );

        testInstance.addDatasourceType( TYPE_2, "domain-W" );
        assertTrue( testInstance.getDatasourceType( TYPE_2 ).contains( "domain-W" ) );
        assertEquals( 1, testInstance.getDatasourceType( TYPE_2 ).size() );

    }


    private PentahoDataSourceTypeMap createInstanceWithTestData() {

        PentahoDataSourceTypeMap pdstm = new PentahoDataSourceTypeMap();

        pdstm.addDatasourceType( TYPE_1, "domain-7" );
        pdstm.addDatasourceType( TYPE_1, "domain-34" );
        pdstm.addDatasourceType( TYPE_2, "domain-W" );
        pdstm.addDatasourceType( TYPE_2, "domain-V" );
        pdstm.addDatasourceType( TYPE_2, "domain-F" );
        return pdstm;
    }
}
