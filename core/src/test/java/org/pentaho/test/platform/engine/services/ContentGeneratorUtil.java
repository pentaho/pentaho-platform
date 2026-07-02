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
