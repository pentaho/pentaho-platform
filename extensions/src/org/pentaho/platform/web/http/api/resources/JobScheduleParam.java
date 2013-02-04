package org.pentaho.platform.web.http.api.resources;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JobScheduleParam implements Serializable {
  
  private static final long serialVersionUID = -4214459740606299083L;

  private static final SimpleDateFormat isodatetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");  

  String name;
  String type;
  ArrayList<String> stringValue = new ArrayList<String>();

  public JobScheduleParam() {
  }
  
  public JobScheduleParam(String name, String value) {
    this.name = name;
    this.type = "string";
    stringValue.add(value);
  }
  
  public JobScheduleParam(String name, Number value) {
    this.name = name;
    this.type = "number";
    stringValue.add(value != null ? value.toString() : null);
  }
  
  public JobScheduleParam(String name, Date value) {
    this.name = name;
    this.type = "date";
    stringValue.add(value != null ? isodatetime.format(value) : null);
  }
  
  public JobScheduleParam(String name, Boolean value) {
    this.name = name;
    this.type = "boolean";
    stringValue.add(value != null ? value.toString() : null);
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
  
  public ArrayList<String> getStringValue() {
    return stringValue;
  }
  
  public void setStringValue(ArrayList<String> value) {
    this.stringValue = value;
  }
  
  public Serializable getValue() {
    Serializable object = null;
    if (type.equals("string")) {
      if (stringValue.size() > 0) {
        object = stringValue.get(0);
      }
    } else if (type.equals("number")) {
      if (stringValue.size() > 0) {
        object = stringValue.get(0).indexOf(".") < 0 ? Integer.parseInt(stringValue.get(0)) : Float.parseFloat(stringValue.get(0));
      }
    } else if (type.equals("boolean")) {
      if (stringValue.size() > 0) {
        object = Boolean.valueOf(stringValue.get(0));
      }
    } else if (type.equals("date")) {
      if (stringValue.size() > 0) {
        try {
          object =  isodatetime.parse(stringValue.get(0));
        } catch (ParseException e) {
          throw new IllegalArgumentException(e);
        }
      }
    } else if (type.equals("string[]")){
      object = new String[stringValue.size()];
      int i = 0;
      for (String string : stringValue) {
        ((String[])object)[i++] = string;
      }     
    } else if (type.equals("number[]")){
      object = new Number[stringValue.size()];
      int i = 0;
      for (String string : stringValue) {
        ((Number[])object)[i++] = string.indexOf(".") < 0 ? Integer.parseInt(string) : Float.parseFloat(string);
      }     
    } else if (type.equals("boolean[]")){
      object = new Boolean[stringValue.size()];
      int i = 0;
      for (String string : stringValue) {
        ((Boolean[])object)[i++] = Boolean.valueOf(string);;
      }     
    } else if (type.equals("date[]")){
      object = new Date[stringValue.size()];
      int i = 0;
      for (String string : stringValue) {
        try {
          ((Date[])object)[i++] = isodatetime.parse(string);
        } catch (ParseException e) {
          throw new IllegalArgumentException(e);
        }
      }     
    }
    return object;
  }
}
