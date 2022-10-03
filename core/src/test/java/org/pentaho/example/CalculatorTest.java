package org.pentaho.example;

import junit.framework.TestCase;

public class CalculatorTest extends TestCase {

    public void testAdd() {

        Calculator calculator = new Calculator();
        assertEquals(5, calculator.add(0,5) );
    }
}