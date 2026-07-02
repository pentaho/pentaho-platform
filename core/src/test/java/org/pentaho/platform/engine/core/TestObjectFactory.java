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


package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

import java.util.List;
import java.util.Map;

public class TestObjectFactory implements IPentahoObjectFactory {

  public <T> T get( Class<T> arg0, IPentahoSession arg1 ) throws ObjectFactoryException {
    // Auto-generated method stub
    return null;
  }

  public <T> T get( Class<T> arg0, String arg1, IPentahoSession arg2 ) throws ObjectFactoryException {
    // Auto-generated method stub
    return null;
  }

  public Class<?> getImplementingClass( String arg0 ) {
    // Auto-generated method stub
    return null;
  }

  public void init( String arg0, Object arg1 ) {
    // Auto-generated method stub

  }

  public boolean objectDefined( String arg0 ) {
    // Auto-generated method stub
    return false;
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession ) throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> clazz, IPentahoSession curSession ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> T get( Class<T> interfaceClass, IPentahoSession session, Map<String, String> properties )
    throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean objectDefined( Class<?> clazz ) {
    return false; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference( Class<T> interfaceClass, IPentahoSession curSession,
      Map<String, String> properties ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass,
                                                                   IPentahoSession curSession ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences( Class<T> interfaceClass, IPentahoSession curSession,
      Map<String, String> properties ) {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public <T> List<T> getAll( Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties )
    throws ObjectFactoryException {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getName() {
    return null; // To change body of implemented methods use File | Settings | File Templates.
  }
}
