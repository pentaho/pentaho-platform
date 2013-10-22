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

package org.pentaho.platform.util.xml;

import org.dom4j.Node;
import org.pentaho.platform.api.engine.IDocumentResourceLoader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XForm {

  // TODO sbarkdull, make this an enumerated type
  public static final int TYPE_RADIO = 1;

  public static final int TYPE_SELECT = 2;

  public static final int TYPE_LIST = 3;

  public static final int TYPE_LIST_MULTI = 4;

  public static final int TYPE_CHECK_MULTI = 5;

  public static final int TYPE_CHECK_MULTI_SCROLL = 6;

  public static final int TYPE_CHECK_MULTI_SCROLL_2_COLUMN = 7;

  public static final int TYPE_CHECK_MULTI_SCROLL_3_COLUMN = 8;

  public static final int TYPE_CHECK_MULTI_SCROLL_4_COLUMN = 9;

  public static final int TYPE_TEXT = 10;

  // TODO sbarkdull make this an enumerated type
  public static final int OUTPUT_XFORM = 1;

  public static final int OUTPUT_HTML = 2;

  public static final int OUTPUT_HTML_PAGE = 3;

  public static String transformSnippet( final Node xForm, final IPentahoSession session,
      final IDocumentResourceLoader loader ) throws TransformerException {

    return XForm.transformSnippet( xForm.asXML(), null, session, loader );

  }

  /**
   * Using html4.xsl, wrap the XForm String in xFormSnippet in a &lt;pho:snippet&gt;, and transform the wrapped XSL
   * into an HTML form snippet.
   * 
   * @param xFormSnippet
   *          The xml snippet being transformed.
   * @param method
   *          HTML form method, generally either "post" or "get".
   * @param session
   * @return HTML form snippet, null on error.
   * @throws TransformerException
   *           if the transform fails.
   */
  public static String transformSnippet( final String xFormSnippet, final String method, final IPentahoSession session,
      final IDocumentResourceLoader loader ) throws TransformerException {

    StringBuffer xForm = null;

    xForm =
        new StringBuffer().append( "<?xml version=\"1.0\" encoding=\"" + LocaleHelper.getSystemEncoding() + "\" ?>" ) //$NON-NLS-1$ //$NON-NLS-2$
            .append(
                "<pho:snippet xmlns:xf=\"http://www.w3.org/2002/xforms\"  xmlns:pho=\"http://www.w3.org/1999/homl\">" ) //$NON-NLS-1$
            .append( xFormSnippet ).append( "</pho:snippet>" ); //$NON-NLS-1$

    String html = XForm.transform( xForm.toString(), method, session, loader );

    // This is required so that we don't get a new form for each control
    // TODO fix the transform so that this code is not required...
    int pos1 = html.indexOf( "<form name=\"pentaho-form\"" ); //$NON-NLS-1$
    int pos2 = html.indexOf( "enctype=\"application/x-www-form-urlencoded\">", pos1 ); //$NON-NLS-1$
    if ( pos2 > 0 ) {
      html = html.substring( pos2 + 44 );
      pos2 = html.indexOf( "</form>" ); //$NON-NLS-1$
      if ( pos2 > 0 ) {
        html = html.substring( 0, pos2 );
      }
    }

    return html;
  }

  /**
   * Using html4.xsl, transform the xml in the xForm string to an HTML form snippet.
   * 
   * @param xForm
   *          The xml string being transformed.
   * @param method
   *          HTML form method, generally either "post" or "get".
   * @param session
   * @return HTML form snippet, null on error.
   * @throws TransformerException
   *           if the transform fails.
   */
  public static String transform( final String xForm, final String method, final IPentahoSession session,
      final IDocumentResourceLoader loader ) throws TransformerException {

    HashMap<String, String> parameters = new HashMap<String, String>();
    if ( method != null ) {
      parameters.put( "form-method", method ); //$NON-NLS-1$
    } else {
      parameters.put( "form-method", "GET" ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // TODO: Why isn't the parameters map above being passed in as the 4th parameter?

    return XmlHelper.transformXml( "html4.xsl", null, xForm, null, loader ).toString(); //$NON-NLS-1$
  }

  public static void createXFormHeader( final String formName, final StringBuffer xformHeader ) {

    xformHeader.append( "<head>" ) //$NON-NLS-1$
        .append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/pentaho-style/active/default.css\" />" ) //$NON-NLS-1$
        .append( "<xf:model id=\"" ).append( formName ).append( "\">" ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "<xf:instance>" ); //$NON-NLS-1$

  }

  public static void completeXFormHeader( final String formName, final StringBuffer xformHeader ) {

    xformHeader
        .append( "</xf:instance>" ) //$NON-NLS-1$
        .append( "<xf:submission action=\"\" separator=\"&amp;\" method=\"urlencoded-get\" id=\"" ).append( formName ).append( "\" />" ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "</xf:model>" ) //$NON-NLS-1$
        .append( "</head>" ); //$NON-NLS-1$

  }

  public static void createXFormSubmit( final String formName, final StringBuffer xformBody, final String buttonText ) {

    xformBody
        .append( "<xf:submit id=\"" ).append( formName ).append( "\" submission=\"" ).append( formName ).append( "\">" ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        .append( "<xf:label>" ).append( buttonText ).append( "</xf:label>" ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "</xf:submit>" ); //$NON-NLS-1$

  }

  public static String completeXForm( final int outputType, final String formName, final StringBuffer xformHeader,
      final StringBuffer xformBody, final IPentahoSession session, final IDocumentResourceLoader loader )
    throws TransformerException {
    xformHeader
        .append( "</xf:instance>" ) //$NON-NLS-1$
        .append( "<xf:submission action=\"\" separator=\"&amp;\" method=\"urlencoded-get\" id=\"" ).append( formName ).append( "\" />" ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "</xf:model>" ) //$NON-NLS-1$
        .append( "</head>" ); //$NON-NLS-1$

    if ( outputType == XForm.OUTPUT_XFORM ) {
      xformHeader.append( xformBody );
      return xformHeader.toString();
    } else if ( outputType == XForm.OUTPUT_HTML ) {
      xformHeader.append( "<body dir=\"" ).append( LocaleHelper.getTextDirection() ).append( "\">" ); //$NON-NLS-1$ //$NON-NLS-2$
      xformHeader.append( xformBody );
      xformHeader.append( "</body>" ); //$NON-NLS-1$
      return XForm.transformSnippet( xformHeader.toString(), "GET", session, loader ); //$NON-NLS-1$
    } else if ( outputType == XForm.OUTPUT_HTML_PAGE ) {
      StringBuffer xForm = new StringBuffer();
      xForm
          .append( "<html xmlns=\"http://www.w3.org/2002/06/xhtml2\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:pho=\"http://www.w3.org/2002/xhoml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" ); //$NON-NLS-1$
      if ( xformBody.indexOf( "{xform-header}" ) > -1 ) { //$NON-NLS-1$
        String header = xformHeader.toString();
        header = header.replaceFirst( "<head>", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        header = header.replaceFirst( "</head>", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        String tmp = xformBody.toString();
        tmp = tmp.replaceFirst( "\\{xform-header\\}", header ); //$NON-NLS-1$
        String html = XForm.transform( tmp, "GET", session, loader ); //$NON-NLS-1$
        return html;
      } else {
        xForm.append( xformHeader );
        xForm.append( xformBody );
        xForm.append( "</html>" ); //$NON-NLS-1$

        String html = XForm.transform( xForm.toString(), "GET", session, loader ); //$NON-NLS-1$ 
        return "<html><body dir=\"" + LocaleHelper.getTextDirection() + "\">" + html + "</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }

    } else {
      return null;
    }

  }

  public static void createXFormControl( final String fieldName, final Object defaultValues, final String formName,
      final StringBuffer xformHeader, final StringBuffer xformBody, final boolean visible ) {

    // create some xform to represent this parameter...

    if ( xformHeader.length() == 0 ) {
      // this is the first parameter, need to create the header...
      XForm.createXFormHeader( formName, xformHeader );
    }

    XForm.setDefaultValues( fieldName, defaultValues, xformHeader );

    if ( visible ) {
      xformBody
          .append( "<xf:input model=\"" ).append( formName ).append( "\" id=\"" ).append( fieldName ).append( "\" ref=\"" ).append( fieldName ).append( "\"></xf:input>" ); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    }

  }

  private static void setDefaultValues( final String fieldName, final Object defaultValues,
      final StringBuffer xformHeader ) {

    xformHeader.append( "<data xmlns=\"\">" ); //$NON-NLS-1$
    if ( defaultValues instanceof String ) {
      xformHeader
          .append( "<" ).append( fieldName ).append( ">" ).append( XmlHelper.encode( (String) defaultValues ) ).append( "</" ).append( fieldName ).append( ">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    } else if ( defaultValues instanceof String[] ) {
      String[] values = (String[]) defaultValues;
      for ( String element : values ) {
        xformHeader
            .append( "<" ).append( fieldName ).append( ">" ).append( XmlHelper.encode( element ) ).append( "</" ).append( fieldName ).append( ">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
    } else if ( defaultValues instanceof Object[] ) {
      Object[] values = (String[]) defaultValues;
      for ( Object element : values ) {
        xformHeader
            .append( "<" ).append( fieldName ).append( ">" ).append( XmlHelper.encode( element.toString() ) ).append( "</" ).append( fieldName ).append( ">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
    } else if ( defaultValues == null ) {
      xformHeader.append( "<" ).append( fieldName ).append( "></" ).append( fieldName ).append( ">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else {
      xformHeader
          .append( "<" ).append( fieldName ).append( ">" ).append( XmlHelper.encode( defaultValues.toString() ) ).append( "</" ).append( fieldName ).append( ">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    xformHeader.append( "</data>" ); //$NON-NLS-1$

  }

  public static void createXFormControl( final int type, final String fieldName, Object defaultValues,
      final List values, final Map displayNames, final String formName, final StringBuffer xformHeader,
      final StringBuffer xformBody ) {

    // If displaying a text box value might be null and we still want to display the text box
    if ( ( type != XForm.TYPE_TEXT ) && ( values == null ) ) {
      return;
    }
    // create some xform to represent this parameter...

    if ( xformHeader.length() == 0 ) {
      // this is the first parameter, need to create the header...
      XForm.createXFormHeader( formName, xformHeader );
    }

    // If the values is not null and if there is only one item in the list make it the default
    if ( ( values != null ) && ( values.size() == 1 ) ) {
      defaultValues = XmlHelper.encode( values.get( 0 ).toString() );
    }

    XForm.setDefaultValues( fieldName, defaultValues, xformHeader );

    String appearance = ""; //$NON-NLS-1$
    if ( type == XForm.TYPE_TEXT ) {
      xformBody
          .append( "<xf:input model=\"" ).append( formName ).append( "\" id=\"" ).append( fieldName ).append( "\" ref=\"" ).append( fieldName ).append( "\"></xf:input>" ); //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    } else if ( ( type == XForm.TYPE_RADIO ) || ( type == XForm.TYPE_SELECT ) || ( type == XForm.TYPE_LIST ) ) {
      switch ( type ) {
        case TYPE_RADIO:
          appearance = "appearance=\"full\"";
          break; //$NON-NLS-1$
        case TYPE_SELECT:
          appearance = "";
          break; //$NON-NLS-1$
        case TYPE_LIST:
          appearance = "appearance=\"compact\"";
          break; //$NON-NLS-1$
      }
      xformBody
          .append( "<xf:select1 " ).append( appearance ).append( " model=\"" ).append( formName ).append( "\" id=\"" ).append( fieldName ).append( "\" ref=\"" ).append( fieldName ).append( "\">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      if ( values != null ) {
        Iterator listIterator = values.iterator();
        while ( listIterator.hasNext() ) {
          String value = XmlHelper.encode( listIterator.next().toString() );
          String displayValue =
              ( displayNames != null ) ? XmlHelper.encode( (String) displayNames.get( value ) ) : value;
          if ( displayValue == null ) {
            displayValue = value;
          }
          xformBody
              .append( "<xf:item><xf:label>" ).append( displayValue ).append( "</xf:label><xf:value>" ).append( value ).append( "</xf:value></xf:item>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        }
      }
      xformBody.append( "</xf:select1>" ); //$NON-NLS-1$
    } else if ( ( type == XForm.TYPE_LIST_MULTI ) || ( type == XForm.TYPE_CHECK_MULTI )
        || ( type == XForm.TYPE_CHECK_MULTI_SCROLL ) || ( type == XForm.TYPE_CHECK_MULTI_SCROLL_2_COLUMN )
        || ( type == XForm.TYPE_CHECK_MULTI_SCROLL_3_COLUMN ) || ( type == XForm.TYPE_CHECK_MULTI_SCROLL_4_COLUMN ) ) {
      switch ( type ) {
        case TYPE_CHECK_MULTI:
          appearance = "appearance=\"full\"";
          break; //$NON-NLS-1$
        case TYPE_LIST_MULTI:
          appearance = "appearance=\"compact\"";
          break; //$NON-NLS-1$
        case TYPE_CHECK_MULTI_SCROLL:
          appearance = "appearance=\"full-scroll\" columns=\"1\"";
          break; //$NON-NLS-1$
        case TYPE_CHECK_MULTI_SCROLL_2_COLUMN:
          appearance = "appearance=\"full-scroll\" columns=\"2\"";
          break; //$NON-NLS-1$
        case TYPE_CHECK_MULTI_SCROLL_3_COLUMN:
          appearance = "appearance=\"full-scroll\" columns=\"3\"";
          break; //$NON-NLS-1$
        case TYPE_CHECK_MULTI_SCROLL_4_COLUMN:
          appearance = "appearance=\"full-scroll\" columns=\"4\"";
          break; //$NON-NLS-1$
      }
      xformBody
          .append( "<xf:select " ).append( appearance ).append( " model=\"" ).append( formName ).append( "\" id=\"" ).append( fieldName ).append( "\" ref=\"" ).append( fieldName ).append( "\">" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      if ( values != null ) {
        Iterator listIterator = values.iterator();
        while ( listIterator.hasNext() ) {
          String value = XmlHelper.encode( listIterator.next().toString() );
          String displayValue =
              ( displayNames != null ) ? XmlHelper.encode( (String) displayNames.get( value ) ) : value;
          if ( displayValue == null ) {
            displayValue = value;
          }
          xformBody
              .append( "<xf:item><xf:label>" ).append( displayValue ).append( "</xf:label><xf:value>" ).append( value ).append( "</xf:value></xf:item>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
        }
      }
      xformBody.append( "</xf:select>" ); //$NON-NLS-1$
    }

  }

}
