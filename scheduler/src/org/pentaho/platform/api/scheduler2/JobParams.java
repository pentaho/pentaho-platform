package org.pentaho.platform.api.scheduler2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobParams {
  @XmlElement
  JobParam[] jobParams;
}
