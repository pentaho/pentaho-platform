/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

import java.util.List;
import java.util.Map;

public class TestObjectFactory implements IPentahoObjectFactory {

  public <T> T get( Class<T> arg0, IPentahoSession arg1 ) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> T get( Class<T> arg0, String arg1, IPentahoSession arg2 ) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public Class<?> getImplementingClass( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  public void init( String arg0, Object arg1 ) {
    // TODO Auto-generated method stub

  }

  public boolean objectDefined( String arg0 ) {
    // TODO Auto-generated method stub
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
