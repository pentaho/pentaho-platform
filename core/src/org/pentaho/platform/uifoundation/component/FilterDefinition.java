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

package org.pentaho.platform.uifoundation.component;

import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ComponentException;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.uifoundation.component.xml.FilterPanelException;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.XForm;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO sbarkdull, may be worth breaking into 4 separate classes, one for each of the ITEM_SOURCE_*'s of course
 * they would all implement the same interface or abstr class A factory class would look at the xml, and create the
 * appropriate instance/type
 * 
 * @author unknow, probably James Dixon
 * 
 */
public abstract class FilterDefinition {

  private static final String RE_HAS_WHITE_SPACE = ".+\\s+.+"; //$NON-NLS-1$

  /**
   * the name of the title, identified by the element <title> in the filter panel definition file
   */
  private String title;

  private String elementName;

  /**
   * a resultSet containing the text and values for the items to be displayed in the filter panel's control. The
   * result set may be a scoped variable, in which case it is retrieved by the key specified in the
   * <global-attribute> or <session-attribute> element in the filter panel definition file. Or, it is the output of
   * the action sequence, in which case it is retrieved from the action sequence's output specified by the name of
   * the <data-output> element of the filter panel definition file.
   */
  private IPentahoResultSet resultSet;

  /**
   * 
   * the type of the control in the filter panel, identified by the element <type> in the filter panel definition
   * file. Valid values are: radio,list,list-multi,check-multi,check-multi-scroll,check-multi-scroll-2-column,
   * check-multi-scroll-3-column,check-multi-scroll-4-column
   */
  private int type;

  /**
   * name of the column to retrieve the names of the items placed in the filter panel's control related to the
   * member variable nameColumnNo
   */
  protected String descriptionItem;

  /**
   * name of the column to retrieve the values of the items placed in the filter panel's control related to the
   * member variable valueColumnNo
   */
  protected String valueItem;

  private String[] defaultValue;

  protected Element node;

  protected String formName;

  protected ILogger logger;

  protected IPentahoSession session;

  /**
   * index of the column to retrieve the names of the items placed in the filter panel's control related to the
   * member variable descriptionItem
   */
  private int nameColumnNo = -1;

  /**
   * index of the column to retrieve the values of the items placed in the filter panel's control related to the
   * member variable valueItem
   */
  private int valueColumnNo = -1;

  /**
   * Ctor, duh.
   * 
   * @param node
   * @param formName
   * @param logger
   */
  protected FilterDefinition( final Element node, final IPentahoSession session, final ILogger logger ) {

    this.logger = logger;
    this.node = node;
    this.session = session;
  }

  // public void setFormName(String formName) {
  // this.formName = formName;
  // }

  public String getTitle() {
    return title;
  }

  public String getName() {
    return elementName;
  }

  /**
   * order of precedence: session-attribute, global-attribute, data-solution, static-list
   * 
   * @param xMLnode
   * @throws FilterPanelException
   */
  public void fromXml( final Element xMLnode ) throws FilterPanelException {
    title = XmlDom4JHelper.getNodeText( "title", xMLnode ); //$NON-NLS-1$
    elementName = XmlDom4JHelper.getNodeText( "name", xMLnode ); //$NON-NLS-1$
    descriptionItem = XmlDom4JHelper.getNodeText( "data-display", xMLnode ); //$NON-NLS-1$
    valueItem = XmlDom4JHelper.getNodeText( "data-value", xMLnode ); //$NON-NLS-1$
    formName = XmlDom4JHelper.getNodeText( "name", xMLnode ); //$NON-NLS-1$

    String typeStr = XmlDom4JHelper.getNodeText( "type", xMLnode ); //$NON-NLS-1$
    if ( "radio".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_RADIO;
    } else if ( "list".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_LIST;
    } else if ( "list-multi".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_LIST_MULTI;
    } else if ( "check-multi".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_CHECK_MULTI;
    } else if ( "check-multi-scroll".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_CHECK_MULTI_SCROLL;
    } else if ( "check-multi-scroll-2-column".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_CHECK_MULTI_SCROLL_2_COLUMN;
    } else if ( "check-multi-scroll-3-column".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_CHECK_MULTI_SCROLL_3_COLUMN;
    } else if ( "check-multi-scroll-4-column".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_CHECK_MULTI_SCROLL_4_COLUMN;
    } else if ( "text-box".equalsIgnoreCase( typeStr ) ) { //$NON-NLS-1$
      type = XForm.TYPE_TEXT;
    } else {
      type = XForm.TYPE_SELECT;
    }
  }

  public void setDefaultValue( final String[] defaultValue ) {
    this.defaultValue = defaultValue;
  }

  public boolean isValid( final String[] values ) {
    if ( values == null ) {
      return ( false );
    }

    for ( String element : values ) {
      if ( !isValid( element ) ) {
        return ( false );
      }
    }
    return ( true );
  }

  protected boolean isValid( final String value ) {
    // this assume that the list of valid values does not have to be populated

    // This is a new field which let the user specify whether an empty value is a valide value or not
    String empty = XmlDom4JHelper.getNodeText( "empty-accepted", node ); //$NON-NLS-1$
    // This is a new field which let the user specify if the value select has to be in the list or values or not
    String valueInList = XmlDom4JHelper.getNodeText( "value-in-list", node ); //$NON-NLS-1$
    boolean isEmptyAccepted = Boolean.parseBoolean( empty );
    boolean mustExistInList =
        ( ( valueInList != null ) && ( valueInList.length() > 0 ) ) ? Boolean.parseBoolean( valueInList ) : true;

    if ( mustExistInList ) {
      if ( value == null ) {
        return false;
      }
      if ( resultSet == null ) {
        // we cannot determin the values for this filter
        return false;
      }
      Object[] row = resultSet.next();
      String rowValue;
      while ( row != null ) {
        rowValue = row[valueColumnNo].toString();

        if ( value.equals( rowValue ) ) {
          resultSet.close();
          return true;
        }
        row = resultSet.next();
      }
      // close the result set so we can loop through it again later if we need to
      resultSet.close();
      return false;

    } else {
      if ( isEmptyAccepted ) {
        return true;
      } else {
        return ( ( value != null ) && ( value.length() > 0 ) );
      }
    }
  }

  // sbarkdull: doesn't appear to be used anywhere, temporarily removing it from the public interface
  /*
   * private boolean isValid(String[] value, Map parameterProviders) { if (resultSet == null && parameterProviders
   * != null) { populate(parameterProviders, value); } return isValid(value);
   * 
   * }
   */
  protected abstract IPentahoResultSet getResultSet( Map parameterProviders );

  public boolean populate( final Map parameterProviders, final String[] value ) {
    // TODO apply session-based security
    // TODO support static lists of values

    defaultValue = value;
    resultSet = getResultSet( parameterProviders );
    if ( resultSet != null ) {
      // find the column that we have been told to you
      IPentahoMetaData metaData = resultSet.getMetaData();
      nameColumnNo = metaData.getColumnIndex( descriptionItem );
      valueColumnNo = metaData.getColumnIndex( valueItem );
    }
    return ( resultSet != null );
  }

  /**
   * Create the XForm header and XForm body, and place the results in the parameters xformHeader and xformBody.
   * 
   * @param xformHeader
   *          StringBuffer containing the XForm header
   * @param xformBody
   *          StringBuffer containing the XForm body
   * 
   * @throws ComponentException
   *           if this.nameColumnNo is -1, this.valueColumnNo is -1, or the this.resultSet is null. nameColumnNo is
   *           likely to be -1 if this.descriptionItem does not correlate with the value of the <data-display>
   *           element in the filter panel definition file and the name of a column in the resultSet. valueColumnNo
   *           is likely to be -1 if this.valueItem does not correlate with the value of the <data-value> element
   *           in the filter panel definition file and the name of a column in the resultSet. resultSet is likely
   *           to be null if a result-set was not placed in session or global scope under the key identified by the
   *           <global-attribute> or <session-attribute> element in the filter panel definition file, or if the
   *           action sequence identified by <data-action> element in the filter panel definition file failed to
   *           return a result set
   */
  public void getXForm( final StringBuffer xformHeader, final StringBuffer xformBody ) throws ComponentException {

    // iterate thru the values to get the items and the display names
    String value;
    String name;

    HashMap displayNames = null;
    ArrayList items = null;

    // TODO support multiple column headers / row headers
    // TODO support an iteration across columns for a given row
    // If the result is non-empty then we will extract the values from the result set and display to the user
    if ( ( resultSet != null ) && ( resultSet.getRowCount() > 0 ) ) {
      displayNames = new HashMap();
      items = new ArrayList();
      if ( nameColumnNo == -1 ) {
        // we did not find the specified name column
        throw new ComponentException( Messages.getInstance().getErrorString(
            "FilterDefinition.ERROR_0001_NAME_COLUMN_MISSING", descriptionItem ) ); //$NON-NLS-1$
      } else if ( valueColumnNo == -1 ) {
        // we did not find the specified name column
        throw new ComponentException( Messages.getInstance().getErrorString(
            "FilterDefinition.ERROR_0002_VALUE_COLUMN_MISSING", valueItem ) ); //$NON-NLS-1$
      }

      Object[] row = null;
      try {
        row = resultSet.next();
      } catch ( Exception e ) {
        // We will check for null below
      }
      if ( row == null ) {
        logger.warn( Messages.getInstance().getErrorString( "FilterDefinition.ERROR_0004_FILTER_DEFINITION_EMPTY" ) ); //$NON-NLS-1$ 
      } else {
        while ( row != null ) {
          value = row[valueColumnNo].toString();
          items.add( value );
          name = row[nameColumnNo].toString();
          if ( name != null ) {
            displayNames.put( value, name );
          }

          row = resultSet.next();
        }
      }
      // close the result set so we can loop through it again later if we need
      // to
      resultSet.close();

      // now create the XForm for the item
      if ( displayNames.size() == 0 ) {
        displayNames = null;
      }
    } else {
      // ResultSet is null and it is ok only for a filter type of TEXT_BOX
      if ( type != XForm.TYPE_TEXT ) {
        throw new ComponentException( Messages.getInstance().getErrorString(
            "FilterDefinition.ERROR_0003_FILTER_DEFINITION_NULL" ) ); //$NON-NLS-1$
      }
    }

    assert formName != null : Messages.getInstance().getErrorString( "FilterDefinition.ERROR_0005_NAME_ELEMENT_EMPTY" ); //$NON-NLS-1$
    assert !formName.matches( FilterDefinition.RE_HAS_WHITE_SPACE ) : Messages.getInstance().getErrorString(
        "FilterDefinition.ERROR_0006_NAME_ELEMENT_WHITESPACE" ); //$NON-NLS-1$
    XForm.createXFormControl( type, elementName, defaultValue, items, displayNames, formName, xformHeader, xformBody );
  }

  public static void main( final String[] args ) {
    String[] nm = { " xx", "xx ", " xx ", "x x", "xx", "custome rnumber", " x x " }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

    for ( String element : nm ) {
      boolean matches = element.matches( FilterDefinition.RE_HAS_WHITE_SPACE );
      System.out.println( "[" + element + "] matches: " + matches ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
}
