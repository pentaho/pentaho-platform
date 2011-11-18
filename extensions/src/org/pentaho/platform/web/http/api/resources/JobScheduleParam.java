package org.pentaho.platform.web.http.api.resources;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JobScheduleParam implements Serializable {
  private static final SimpleDateFormat isodatetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");  

  String name;
  String type;
  String stringValue;

  public JobScheduleParam() {
  }
  
  public JobScheduleParam(String name, String value) {
    this.name = name;
    this.type = "string";
    this.stringValue = value;
  }
  
  public JobScheduleParam(String name, Number value) {
    this.name = name;
    this.type = "number";
    this.stringValue = (value != null ? value.toString() : null);
  }
  
  public JobScheduleParam(String name, Date value) {
    this.name = name;
    this.type = "date";
    this.stringValue = (value != null ? isodatetime.format(value) : null);
  }
  
  public JobScheduleParam(String name, Boolean value) {
    this.name = name;
    this.type = "boolean";
    this.stringValue = (value != null ? value.toString() : null);
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getStringValue() {
    return stringValue;
  }
  
  public void setStringValue(String value) {
    this.stringValue = value;
  }
  
  public Serializable getValue() {
    Serializable object = null;
    if (type.equals("string")) {
      object = stringValue;
    } else if (type.equals("number")) {
      object = stringValue.indexOf(".") < 0 ? Integer.parseInt(stringValue) : Float.parseFloat(stringValue);
    } else if (type.equals("boolean")) {
      object = new Boolean(stringValue);
    } else if (type.equals("date")) {
      try {
        object =  isodatetime.parse(stringValue);
      } catch (ParseException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return object;
  }
}
