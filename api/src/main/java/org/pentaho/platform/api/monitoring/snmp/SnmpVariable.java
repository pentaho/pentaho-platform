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


package org.pentaho.platform.api.monitoring.snmp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be converted to a SNMP VariableBinding for a Trap event.
 * <p/>
 * Created by nbaker on 9/3/14.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.METHOD } )
public @interface SnmpVariable {
  enum TYPE {
    INTEGER, STRING
  }

  String oid() default ""; //So can coexist with old

  TYPE type();

  int ordinal() default -1;

  Class<? extends IVariableSerializer> serializer() default IVariableSerializer.BasicSerializer.class;
}
