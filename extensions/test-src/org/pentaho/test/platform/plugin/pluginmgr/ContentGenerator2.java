/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.pluginmgr;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

import java.util.List;
import java.util.Map;

public class ContentGenerator2 implements IContentGenerator {

  public String getItemName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setItemName( String itemName ) {
    // TODO Auto-generated method stub
  }

  public void createContent() throws Exception {
    // TODO Auto-generated method stub

  }

  public void setCallbacks( List<Object> callbacks ) {
    // TODO Auto-generated method stub

  }

  public void setInstanceId( String instanceId ) {
    // TODO Auto-generated method stub

  }

  public void setMessagesList( List<String> messages ) {
    // TODO Auto-generated method stub

  }

  public void setOutputHandler( IOutputHandler outputHandler ) {
    // TODO Auto-generated method stub

  }

  public void setParameterProviders( Map<String, IParameterProvider> parameterProviders ) {
    // TODO Auto-generated method stub

  }

  public void setSession( IPentahoSession userSession ) {
    // TODO Auto-generated method stub

  }

  public void setUrlFactory( IPentahoUrlFactory urlFactory ) {
    // TODO Auto-generated method stub

  }

  public void debug( String message ) {
    // TODO Auto-generated method stub

  }

  public void debug( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void error( String message ) {
    // TODO Auto-generated method stub

  }

  public void error( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void fatal( String message ) {
    // TODO Auto-generated method stub

  }

  public void fatal( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public int getLoggingLevel() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void info( String message ) {
    // TODO Auto-generated method stub

  }

  public void info( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void setLoggingLevel( int loggingLevel ) {
    // TODO Auto-generated method stub

  }

  public void trace( String message ) {
    // TODO Auto-generated method stub

  }

  public void trace( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void warn( String message ) {
    // TODO Auto-generated method stub

  }

  public void warn( String message, Throwable error ) {
    // TODO Auto-generated method stub

  }

  public void setInput( IPentahoStreamSource item ) {

  }
}
