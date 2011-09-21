package org.pentaho.platform.repository2.unified.webservices;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateAdapter extends XmlAdapter<String, Date> {
  private final static String DF = "MM/dd/yyyy HH:mm:ss";
  private final static Log log = LogFactory.getLog(DateAdapter.class);


  @Override
  public Date unmarshal(String v) throws Exception {
      return new Date(Long.valueOf(v)); 
  }

  @Override
  public String marshal(Date v) throws Exception {
      return String.valueOf(v.getTime());
  }
}