package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;

public interface IJobScheduleParam {
  String getName();

  String getType();
  Serializable getValue();
}
