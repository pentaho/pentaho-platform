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

package org.pentaho.platform.plugin.services.security.userrole.ldap.transform;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts the value of the token <code>tokenName</code> from the attribute <code>attributeName</code>. Ignores
 * attribute value if value is not of type <code>String</code>. Returns the entire attribute value if
 * <code>tokenName</code> is not specified.
 * <p>
 * <strong>Note: This transformer can produce more than one output (in the form of a collection) per single input. Any
 * client of this class should merge the results (e.g. <code>collection.addAll(collection)</code>) into an existing
 * collection.</strong>
 * </p>
 * 
 * <p>
 * Transformer input: <code>SearchResult</code> instance
 * </p>
 * <p>
 * Transformer output: <code>Collection</code> of <code>String</code> instances
 * </p>
 * TODO refactor into searchresulttoattributelist, attributelisttosingleattribute, attributestringtoattributesubstring
 * 
 * @author mlowery
 */
public class SearchResultToAttrValueList implements Transformer, InitializingBean {

  // ~ Static fields/initializers ============================================
  private static final Log logger = LogFactory.getLog( SearchResultToAttrValueList.class );

  // ~ Instance fields =======================================================

  private String attributeName;

  private String tokenName;

  // ~ Constructors ==========================================================

  public SearchResultToAttrValueList( final String attributeName ) {
    this( attributeName, null );
  }

  public SearchResultToAttrValueList( final String attributeName, final String tokenName ) {
    super();
    this.attributeName = attributeName;
    this.tokenName = tokenName;
  }

  // ~ Methods ===============================================================

  /**
   * Assumes that <code>src</code> consists of name-value pairs linked via <code>'='</code> and each pair separated by
   * <code>','</code>.
   * 
   * @param src
   *          the source string
   * @param inTokenName
   *          the name part of the name-value pair whose value will be returned
   * @return the value part of the name-value pair whose name part is <code>tokenName</code>
   */
  protected String extract( final String src, final String inTokenName ) {
    if ( SearchResultToAttrValueList.logger.isDebugEnabled() ) {
      SearchResultToAttrValueList.logger.debug( Messages.getInstance().getString(
          "SearchResultToAttrValueList.DEBUG_LOOKING_FOR_SUBSTRING", inTokenName, src ) ); //$NON-NLS-1$
    }
    String[] tokens = src.split( "," ); //$NON-NLS-1$
    for ( String rdnString : tokens ) {
      String[] rdnTokens = rdnString.split( "=" ); //$NON-NLS-1$
      if ( rdnTokens[0].trim().equals( inTokenName ) ) {
        if ( SearchResultToAttrValueList.logger.isDebugEnabled() ) {
          SearchResultToAttrValueList.logger.debug( Messages.getInstance().getString(
              "SearchResultToAttrValueList.DEBUG_EXTRACTED_TOKEN", rdnTokens[1].trim() ) ); //$NON-NLS-1$
        }
        return rdnTokens[1].trim();
      }
    }
    if ( SearchResultToAttrValueList.logger.isDebugEnabled() ) {
      SearchResultToAttrValueList.logger.debug( Messages.getInstance().getString(
          "SearchResultToAttrValueList.DEBUG_TOKEN_NOT_FOUND", inTokenName, src ) ); //$NON-NLS-1$
    }
    return null;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.hasLength( attributeName );
  }

  public Object transform( final Object obj ) {
    Object transformed = obj;
    if ( obj instanceof SearchResult ) {
      transformed = new HashSet();
      Set valueSet = (Set) transformed;
      SearchResult res = (SearchResult) obj;
      if ( SearchResultToAttrValueList.logger.isDebugEnabled() ) {
        SearchResultToAttrValueList.logger
            .debug( Messages
              .getInstance()
              .getString(
                "SearchResultToAttrValueList.DEBUG_ATTRIBUTES_FROM_SEARCHRESULT",
                ( null != res.getAttributes() ) ? res.getAttributes().toString() : "null" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      Attribute attr = res.getAttributes().get( attributeName );
      if ( SearchResultToAttrValueList.logger.isDebugEnabled() ) {
        SearchResultToAttrValueList.logger
            .debug( Messages
              .getInstance()
              .getString(
                "SearchResultToAttrValueList.DEBUG_ATTRIBUTE_VALUE", attributeName,
                ( null != attr ) ? attr.toString() : "null" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      if ( attr != null ) { // check for null as node might not have attribute we're looking for
        try {
          NamingEnumeration values = attr.getAll();
          while ( values.hasMore() ) {
            // if tokenName was specified, extract from value; otherwise
            // store value unchanged
            Object value = values.next();
            if ( StringUtils.hasLength( tokenName ) ) {
              if ( ( null != value ) && ( value instanceof String ) ) {
                String tokenValue = extract( (String) value, tokenName );
                if ( null != tokenValue ) {
                  valueSet.add( tokenValue );
                }
              } else {
                if ( SearchResultToAttrValueList.logger.isWarnEnabled() ) {
                  SearchResultToAttrValueList.logger.warn( Messages.getInstance().getString(
                    "SearchResultToAttrValueList.WARN_ATTRIBUTE_NOT_A_STRING" ) ); //$NON-NLS-1$
                }
              }
            } else {
              if ( null != value ) {
                valueSet.add( value.toString() );
              }
            }
          }
        } catch ( NamingException e ) {
          if ( SearchResultToAttrValueList.logger.isErrorEnabled() ) {
            SearchResultToAttrValueList.logger.error( Messages.getInstance().getErrorString(
              "SearchResultToAttrValueList.ERROR_0001_NAMING_EXCEPTION" ), e ); //$NON-NLS-1$
          }
        }
      }
      return transformed;

    }
    return transformed;

  }
}
