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


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.util.beans.ActionHarness;

import java.io.OutputStream;
import java.util.Iterator;

/**
 * Abstract class that enables a content generator to act like a Java Bean through the {@link IFileResourceRenderer} api
 * 
 * @author aaron
 */
public abstract class FileResourceContentGenerator extends SimpleContentGenerator implements IFileResourceRenderer {

  private static final long serialVersionUID = -2788935372334892908L;
  private static final Log logger = LogFactory.getLog( FileResourceContentGenerator.class );

  @Override
  public void createContent( OutputStream out ) throws Exception {
    IParameterProvider pathParams = parameterProviders.get( "path" ); //$NON-NLS-1$
    IParameterProvider requestParams = parameterProviders.get( IParameterProvider.SCOPE_REQUEST );

    RepositoryFile file = (RepositoryFile) pathParams.getParameter( "file" ); //$NON-NLS-1$

    ActionHarness harness = new ActionHarness( this );

    Iterator<?> iter = requestParams.getParameterNames();

    while ( iter.hasNext() ) {
      String paramName = (String) iter.next();
      harness.setValue( paramName, requestParams.getParameter( paramName ) );
    }

    this.setOutputStream( out );
    this.setRepositoryFile( file );
    this.execute();
  }

  @Override
  public String getMimeType() {
    return getMimeType( null );
  }

  @Override
  public Log getLogger() {
    return logger;
  }
}
