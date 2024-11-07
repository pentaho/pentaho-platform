/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.engine.services.actionsequence;

import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.actionsequence.dom.actions.IActionParameterMgr;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.IParameterResolver;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ActionSequenceParameterMgr implements IActionParameterMgr {

  IRuntimeContext runtimeContext;

  IPentahoSession pentahoSession;

  IParameterResolver parameterResolver;

  public ActionSequenceParameterMgr( final IRuntimeContext runtimeContext, final IPentahoSession session ) {
    this( runtimeContext, session, null );
  }

  public ActionSequenceParameterMgr( final IRuntimeContext runtimeContext, final IPentahoSession session,
      final IParameterResolver parameterResolver ) {
    this.runtimeContext = runtimeContext;
    pentahoSession = session;
    this.parameterResolver = parameterResolver;
  }

  public Object getInputValue( final IActionInput actionInput ) {
    Object value = null;
    String variableName = actionInput.getName();
    if ( runtimeContext.getInputNames().contains( variableName ) ) {
      value = runtimeContext.getInputParameterValue( variableName );
    }
    if ( value instanceof IContentItem ) {
      value = ( (IContentItem) value ).getInputStream();
    }
    return value;
  }

  public String replaceParameterReferences( final String inputString ) {
    return runtimeContext.applyInputsToFormat( inputString, parameterResolver );
  }

  public IPentahoStreamSource getDataSource( final IActionResource actionResource ) throws FileNotFoundException {
    IPentahoStreamSource dataSrc = null;
    IActionSequenceResource resource = runtimeContext.getResourceDefintion( actionResource.getName() );
    if ( resource != null ) {
      dataSrc = runtimeContext.getResourceDataSource( resource );
    }
    return dataSrc;
  }

  public String getString( final IActionResource actionResource ) throws IOException {
    String resourceString = null;
    IActionSequenceResource resource = runtimeContext.getResourceDefintion( actionResource.getName() );
    if ( resource != null ) {
      resourceString = runtimeContext.getResourceAsString( resource );
    }
    return resourceString;
  }

  public IPentahoStreamSource getDataSource( final IActionInput actionInput ) {
    return runtimeContext.getDataSource( actionInput.getName() );
  }

  public void setOutputValue( final IActionOutput actionOutput, final Object value ) {
    runtimeContext.setOutputValue( actionOutput.getName(), value );
  }

  public InputStream getInputStream( final IActionResource actionResource ) throws FileNotFoundException {
    InputStream inputStream = null;
    IActionSequenceResource resource = runtimeContext.getResourceDefintion( actionResource.getName() );
    if ( resource != null ) {
      inputStream = resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
    }
    return inputStream;
  }
}
