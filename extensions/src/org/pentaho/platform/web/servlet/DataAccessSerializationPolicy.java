package org.pentaho.platform.web.servlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;

public class DataAccessSerializationPolicy extends SerializationPolicy {

  @Override
  public boolean shouldDeserializeFields(Class<?> arg0) {
    return arg0.equals(Object.class) ? false : true;
  }

  @Override
  public boolean shouldSerializeFields(Class<?> arg0) {
    return arg0.equals(Object.class) ? false : true;
  }

  @Override
  public void validateDeserialize(Class<?> arg0) throws SerializationException {
    
  }

  @Override
  public void validateSerialize(Class<?> arg0) throws SerializationException {

  }

}
