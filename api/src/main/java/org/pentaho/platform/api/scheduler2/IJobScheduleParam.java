package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.List;

public interface IJobScheduleParam {
  String getName();

  void setName( String name );

  String getType();
  void setType( String type );

  Serializable getValue();

  void setStringValue( List<String> value );

  List<String> getStringValue();
}
