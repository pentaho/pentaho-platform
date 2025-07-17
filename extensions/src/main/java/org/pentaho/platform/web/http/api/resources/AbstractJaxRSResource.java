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

import org.glassfish.jersey.message.internal.QualitySourceMediaType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML_TYPE;

public abstract class AbstractJaxRSResource {

  /**
   * Quality of service set high to overcome bad Accept header precedence in Webkit and IE browsers. This will make
   *
   * @Produces annotations work properly in jersey
   */
  public static final String TEXT_HTML = "text/html;qs=2"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( AbstractJaxRSResource.class );

  protected List<MediaType> acceptableMediaTypes;

  @Context
  protected HttpServletRequest httpServletRequest;

  @Context
  protected HttpServletResponse httpServletResponse;

  // @Context
  // protected Request jaxRsRequest;

  @Context
  public void setHttpHeaders( HttpHeaders httpHeaders ) {
    List<MediaType> mediaTypes = httpHeaders.getAcceptableMediaTypes();
    int htmlPos = -1;
    int xmlPos = -1;

    for ( int i = 0; i < mediaTypes.size(); i++ ) {
      MediaType t = mediaTypes.get( i );
      if ( t.getSubtype().equals( APPLICATION_XML_TYPE.getSubtype() ) ) {
        xmlPos = i;
      }
      if ( t.getSubtype().equals( TEXT_HTML_TYPE.getSubtype() ) ) {
        htmlPos = i;
      }
    }

    //
    // If both html and xml media types are requested, make sure html
    // takes precedence over xml. This is to correct Webkit (Safari and Chrome),
    // and IE browsers faulty default HTTP Accept header.
    //
    if ( htmlPos > -1 && xmlPos > -1 && xmlPos < htmlPos ) {

      MediaType origHtmlMediaType = mediaTypes.remove( htmlPos );

      Map<String, String> params = new HashMap<String, String>( origHtmlMediaType.getParameters() );
      params.put( "q", "2.0" ); //$NON-NLS-1$ //$NON-NLS-2$

      QualitySourceMediaType newhtmlMediaType =
        new QualitySourceMediaType( origHtmlMediaType.getType(), origHtmlMediaType.getSubtype(), 2000, params );

      // reinsert ahead of xml and with increased qos value
      mediaTypes.add( xmlPos, newhtmlMediaType );

      // VariantListBuilder vlb = Variant.VariantListBuilder.newInstance();
      // for(MediaType m : mediaTypes) {
      // vlb.mediaTypes(m).add();
      // }
      //
      // Variant variant = jaxRsRequest.selectVariant(vlb.build());

      logger.info(
        "Bad Accept header detected by browser, mime type order rewritten as " + mediaTypes.toString() ); //$NON-NLS-1$
    }
    acceptableMediaTypes = mediaTypes;
  }

}
