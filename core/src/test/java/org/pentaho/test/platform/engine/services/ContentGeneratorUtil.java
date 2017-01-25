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

package org.pentaho.test.platform.engine.services;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentGeneratorUtil {
  /**
   * Convenience method for executing a content generator and getting back it's output as a string. Useful for
   * testing.
   * 
   * @param cg
   *          the content generator to execute
   * @return the output of the content generator
   * @throws Exception
   *           if there was a problem creating the content
   */
  public static String getContentAsString( IContentGenerator cg ) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOutputHandler outputHandler = new SimpleOutputHandler( out, false );

    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( "?" ); //$NON-NLS-1$
    List<String> messages = new ArrayList<String>();
    cg.setOutputHandler( outputHandler );
    IMimeTypeListener mimeTypeListener = new IMimeTypeListener() {
      @SuppressWarnings( "unused" )
      public String mimeType = null;

      @SuppressWarnings( "unused" )
      public String name = null;

      public void setMimeType( String mimeType ) {
        this.mimeType = mimeType;
      }

      public void setName( String name ) {
        this.name = name;
      }
    };
    outputHandler.setMimeTypeListener( mimeTypeListener );
    cg.setMessagesList( messages );
    cg.setParameterProviders( parameterProviders );
    cg.setSession( PentahoSessionHolder.getSession() );
    cg.setUrlFactory( urlFactory );
    cg.createContent();
    String content = new String( out.toByteArray() );
    return content;
  }
}
