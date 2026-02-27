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


package org.pentaho.test.platform.plugin.pluginmgr;

import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

import java.util.List;
import java.util.Map;

public class ContentGenerator1 implements IContentGenerator {

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
