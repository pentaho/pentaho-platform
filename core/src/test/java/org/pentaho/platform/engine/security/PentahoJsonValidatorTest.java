package org.pentaho.platform.engine.security;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PentahoJsonValidatorTest {

    private final String VALID_JSON = "{\"class\":\"org.pentaho.platform.engine.security.TestObject\",\"intValue\":\"5\",\"stringValue\":\"XPTO\"}";
    private final String INVALID_CLASS_JSON = "{\"class\":\"org.pentaho.platform.engine.security.TestObjectW\",\"intValue\":\"5\",\"stringValue\":\"XPTO\"}";
    private final String EMPTY_JSON = "";

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
}

class TestObject {
    int intValue;
    String stringValue;

    public TestObject(){
    }

    public TestObject( int intValue, String stringValue ){
        this.intValue = intValue;
        this.stringValue = stringValue;
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
