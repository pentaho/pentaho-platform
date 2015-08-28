package org.pentaho.platform.plugin.services.exporter;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.wrappers.DayOfMonthWrapper;
import org.pentaho.platform.scheduler2.recur.QualifiedDayOfMonth;

import static org.junit.Assert.*;

public class ComplexJobTriggerConverterTest {

  ComplexJobTriggerConverter converter;

  @Before
  public void setUp() throws Exception {
    converter = new ComplexJobTriggerConverter();
  }

  @Test
  public void testUnwrap_DayOfMonthWrapper() throws Exception {
    DayOfMonthWrapper wrapper = new DayOfMonthWrapper();
    QualifiedDayOfMonth dom = new QualifiedDayOfMonth();

//    wrapper.getRecurrences().add(  )
  }
}