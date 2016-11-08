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

package org.pentaho.platform.uifoundation.component.xml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.uifoundation.component.FilterDefinition;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.xml.XForm;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class stores the defintion of filters that are used by FilterComponent to generate content for user
 * interfaces.
 * 
 * This object supports mulitple filters. It stores information about the sources of the values for each filters.
 * It generates an XForm snippet to represent the selections.
 * 
 * @author James Dixon
 * 
 */
public class FilterPanel {

  public static final boolean debug = PentahoSystem.debug;

  // TODO, move to Java 5, generic as ArrayList<FilterDefinition>
  ArrayList filterList;

  String error;

  String name;

  ILogger logger;

  // TODO sbarkdull, this may be worth reimplementing, and not using a DOM Document
  // but using SAX to parse the document. Note that the document is parsed in this
  // constructor, and then thrown away. This is usually a good sign that SAX
  // is a more optimal solution. On the other hand, these documents are small, and there
  // are few of them, so maybe it is no big deal.
  public FilterPanel( final IPentahoSession session, final Document document, final ILogger logger )
    throws FilterPanelException {
    this.logger = logger;
    filterList = new ArrayList();
    if ( document != null ) {
      // create the filter definitions
      List filterNodes = document.selectNodes( "filters/filter" ); //$NON-NLS-1$
      Iterator filtersIterator = filterNodes.iterator();
      while ( filtersIterator.hasNext() ) {
        Element filterNode = (Element) filtersIterator.next();
        FilterDefinition filterDefinition = FilterDefinitionFactory.create( filterNode, session, logger );
        filterList.add( filterDefinition );
      }
      if ( filterList.size() == 0 ) {
        error = Messages.getInstance().getString( "FilterPanel.ERROR_0001_NO_FILTERS" ); //$NON-NLS-1$
        return;
      }
    }
  }

  public List getFilters() {
    return filterList;
  }

  public boolean populate( final Map parameterProviders, final Map defaultValues ) {
    int fail = 0;
    if ( filterList != null ) {
      Iterator filtersIterator = filterList.iterator();
      while ( filtersIterator.hasNext() ) {
        FilterDefinition filterDefinition = (FilterDefinition) filtersIterator.next();
        if ( filterDefinition.populate( parameterProviders, (String[]) defaultValues
          .get( filterDefinition.getName() ) ) ) {
          boolean ignore = true;
        } else {
          fail++;
        }
      }
    }
    return ( fail == 0 );
  }

  public Document getXForm( final String actionUrl ) {

    StringBuffer content = new StringBuffer();
    Document document = null;

    // String strUuid = UUID.randomUUID().toString().replaceAll( "-", "_");
    String strUuid = UUIDUtil.getUUIDAsString().replaceAll( "-", "_" ); //$NON-NLS-1$ //$NON-NLS-2$

    content.append( "<filters xmlns:xf=\"http://www.w3.org/2002/xforms\"><id>" + strUuid + "</id><title><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
        Messages.getInstance().getEncodedString( name ) + "]]></title><description></description><help></help>" + //$NON-NLS-1$
        "<action><![CDATA[" + actionUrl + "]]></action>" ); //$NON-NLS-1$ //$NON-NLS-2$

    if ( error != null ) {
      content.append( "<error>" ); //$NON-NLS-1$
      content.append( error );
      content.append( "</error>" ); //$NON-NLS-1$
    } else if ( filterList == null ) {
      content.append( "<error>" ); //$NON-NLS-1$
      content.append( Messages.getInstance().getString( "FilterPanel.ERROR_0003_NO_FILTER_VALUES" ) ); //$NON-NLS-1$
      content.append( "</error>" ); //$NON-NLS-1$
    } else {
      try {
        Iterator filtersIterator = filterList.iterator();
        StringBuffer xformHeader = new StringBuffer();
        String formName = null;
        while ( filtersIterator.hasNext() ) {
          FilterDefinition filterDefinition = (FilterDefinition) filtersIterator.next();
          String filterName = filterDefinition.getName();
          if ( formName == null ) {
            formName = filterName;
          }
          String title = filterDefinition.getTitle();
          StringBuffer xformBody = new StringBuffer();
          // XForm.createXFormHeader(name, xformHeader);
          filterDefinition.getXForm( xformHeader, xformBody );
          // XForm.completeXForm(XForm.OUTPUT_HTML, filterName, new StringBuffer(), xformBody);
          // content.append( "<filter name=\""+filterName+"\">"
          // ).append(filterContent).append( "</filter>" );
          // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

          content.append( "<filter><name><![CDATA[" + filterName + "]]></name>" ) //$NON-NLS-1$ //$NON-NLS-2$
              .append( "<title><![CDATA[" + title + "]]></title><control>" ) //$NON-NLS-1$ //$NON-NLS-2$
              .append( xformBody ).append( "</control></filter>" ); //$NON-NLS-1$
        }
        XForm.completeXFormHeader( formName, xformHeader );
        content.append( xformHeader );
      } catch ( Exception e ) {
        logger.error( Messages.getInstance().getErrorString( "FilterPanel.ERROR_0004_COULD_NOT_CREATE_CONTENT" ), e ); //$NON-NLS-1$
      }

    }
    content.append( "</filters>" ); //$NON-NLS-1$
    if ( FilterPanel.debug ) {
      logger.debug( content.toString() );
    }
    try {
      document = XmlDom4JHelper.getDocFromString( content.toString(), new PentahoEntityResolver() );
    } catch ( XmlParseException e ) {
      logger.error( Messages.getInstance().getErrorString( "FilterPanel.ERROR_0004_COULD_NOT_CREATE_CONTENT" ), e ); //$NON-NLS-1$
    }
    return document;
  }

}
