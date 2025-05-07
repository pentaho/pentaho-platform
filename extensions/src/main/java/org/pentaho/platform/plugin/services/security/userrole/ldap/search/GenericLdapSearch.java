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


package org.pentaho.platform.plugin.services.security.userrole.ldap.search;

import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.util.Assert;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenericLdapSearch implements LdapSearch, InitializingBean {

  // ~ Static fields/initializers ============================================
  private static final Log logger = LogFactory.getLog( GenericLdapSearch.class );

  // ~ Instance fields =======================================================

  /**
   * Generates disposable instances of search parameters. Factory should be thread-safe (i.e. no mutable instance
   * variables).
   *
   * @see LdapSearchParams
   */
  private LdapSearchParamsFactory paramsFactory;

  /**
   * Transforms LDAP search results into custom objects. Can be chained together. Transformer should be thread-safe
   * (i.e. no mutable instance variables).
   */
  private Transformer resultsTransformer;

  /**
   * Transforms filter arguments before passing to the <code>paramsFactory</code>. On such example is transforming a
   * <code>GrantedAuthority</code> instance into a <code>String</code> instance for searching. Transformer should be
   * thread-safe (i.e. no mutable instance variables).
   * <p>
   * <strong>Note that depending on the matching rules of the directory server, case in strings may or may not
   * matter.</strong>
   * </p>
   */
  private Transformer filterArgsTransformer;

  private ContextSource contextSource;

  // ~ Constructors ==========================================================

  public GenericLdapSearch( final ContextSource contextSource, final LdapSearchParamsFactory paramsFactory ) {
    this( contextSource, paramsFactory, null, null );
  }

  public GenericLdapSearch( final ContextSource contextSource, final LdapSearchParamsFactory paramsFactory,
      final Transformer resultsTransformer ) {
    this( contextSource, paramsFactory, resultsTransformer, null );
  }

  public GenericLdapSearch( final ContextSource contextSource, final LdapSearchParamsFactory paramsFactory,
      final Transformer resultsTransformer, final Transformer filterArgsTransformer ) {
    super();
    this.contextSource = contextSource;
    this.paramsFactory = paramsFactory;
    this.resultsTransformer = resultsTransformer;
    this.filterArgsTransformer = filterArgsTransformer;
  }

  // ~ Methods ===============================================================

  public List search( final Object[] filterArgs ) {
    Object[] transformedArgs = filterArgs;
    // transform the filterArgs
    if ( null != filterArgsTransformer ) {
      transformedArgs = (Object[]) filterArgsTransformer.transform( filterArgs );
    }
    LdapSearchParams params = paramsFactory.createParams( transformedArgs );
    // use a set internally to store intermediate results
    Set results = new HashSet();
    NamingEnumeration matches = null;
    try {
      matches =
          contextSource.getReadOnlyContext().search( params.getBase(), params.getFilter(), params.getFilterArgs(),
              params.getSearchControls() );
    } catch ( NamingException e1 ) {
      if ( GenericLdapSearch.logger.isErrorEnabled() ) {
        // TODO: Throw an exception here
        GenericLdapSearch.logger.error( "Directory search failed", e1 ); //$NON-NLS-1$
      }
      return new ArrayList( results );
    }
    try {
      while ( matches.hasMore() ) {
        SearchResult result = (SearchResult) matches.next();
        if ( null != resultsTransformer ) {
          results.addAll( (Collection) resultsTransformer.transform( result ) );
        } else {
          results.add( result );
        }
      }
    } catch ( NamingException e ) {
      if ( GenericLdapSearch.logger.isErrorEnabled() ) {
        // TODO: Throw an exception here
        GenericLdapSearch.logger.error( "Enumerating directory search results failed", e ); //$NON-NLS-1$
      }
    }
    return new ArrayList( results );
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( contextSource, "ContextSource must not be null" );
    Assert.notNull( paramsFactory, "LdapSearchParamsFactory must not be null" );
  }
}
