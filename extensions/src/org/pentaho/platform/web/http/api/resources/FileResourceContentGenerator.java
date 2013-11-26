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
