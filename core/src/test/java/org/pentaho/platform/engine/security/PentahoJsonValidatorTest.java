/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2024 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.platform.engine.security;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class PentahoJsonValidatorTest {

    private final String VALID_JSON = "{\"class\":\"org.pentaho.platform.engine.security.TestObject\",\"intValue\":\"5\",\"stringValue\":\"XPTO\"}";
    private final String INVALID_CLASS_JSON = "{\"class\":\"org.pentaho.platform.engine.security.TestObjectW\",\"intValue\":\"5\",\"stringValue\":\"XPTO\"}";
    private final String EMPTY_JSON = "";
    private final String VALID_JSONArray = "{\"class\":\"org.pentaho.platform.engine.security.TestObject\",\"list\":[{\"class\":\"org.pentaho.platform.engine.security.TestObject\",\"intValue\":\"5\"\n}],\"intValue\":\"5\",\"stringValue\":\"XPTO\"}";

    @Test
    public void testInvalidJson() {
        try {
            PentahoJsonValidator.validateJson( EMPTY_JSON, TestObject.class );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Invalid Json", e.getMessage() );
            return ;
        }
        fail();
    }

    @Test
    public void testInvalidClassAttribute() {
        try {
            PentahoJsonValidator.validateJson( INVALID_CLASS_JSON, TestObject.class );
        } catch ( IllegalArgumentException e ) {
            assertEquals( "Invalid Payload", e.getMessage() );
            return ;
        }
        fail();
    }


    @Test
    public void testValidJson() {
        assertTrue( PentahoJsonValidator.isJsonValid( VALID_JSON, TestObject.class ) );
    }

    @Test
    public void testValidJsonArray() {
        assertTrue( PentahoJsonValidator.isJsonValid( VALID_JSONArray, TestObject.class ) );
    }
}

class TestObject {
    int intValue;
    String stringValue;
    List<String> list = new ArrayList<>();

    public TestObject(){
    }

    public TestObject( int intValue, String stringValue, List< String > list ){
        this.intValue = intValue;
        this.stringValue = stringValue;
        this.list = list;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue( int intValue ) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue( String stringValue ) {
        this.stringValue = stringValue;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }
        TestObject that = (TestObject) o;

        return intValue == that.intValue && stringValue.equals( that.stringValue );
    }
}
