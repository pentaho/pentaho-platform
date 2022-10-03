package org.pentaho.example;

import junit.framework.TestCase;

public class CalculatorTest extends TestCase {

    public void testAdd() { // should get to less than 80 % code coverage

        Calculator calculator = new Calculator();
        assertEquals( 5, calculator.add( 0, 5 ) );
    }

    public void testMultiply() { // should get to 100 % code coverage

        Calculator calculator = new Calculator();
        assertEquals( 14, calculator.multiply( 2, 7 ) );
    }
}