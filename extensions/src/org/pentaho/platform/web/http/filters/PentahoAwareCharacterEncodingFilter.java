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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

// Filters container instead web.xml changes
public class PentahoAwareCharacterEncodingFilter implements Filter {

  private List<Filter> filters;

  @Override
  public void init( FilterConfig config ) throws ServletException {
    this.filters = new ArrayList<Filter>( 2 );

    addFilter( new PentahoAwareCharacterEncodingFilterImpl(), config );
    addFilter( new SystemInitStatusFilter(), config );
  }

  private void addFilter( Filter filter, FilterConfig config ) throws ServletException {
    filter.init( config );
    this.filters.add( filter );
  }

  @Override
  public void doFilter( ServletRequest rq, ServletResponse rs, FilterChain chain ) throws IOException, ServletException {
    FilterChainWrap chainWrap = new FilterChainWrap( this.filters );
    chainWrap.setFilterChain( chain );
    chainWrap.doFilter( rq, rs );
  }

  @Override
  public void destroy() {
    for ( Filter filter : filters ) {
      filter.destroy();
    }
  }

  class FilterChainWrap implements FilterChain {

    Iterator<Filter> filterIterator;
    FilterChain chain;

    public FilterChainWrap( List<Filter> filters ) {
      this.filterIterator = filters.iterator();
    }

    @Override
    public void doFilter( ServletRequest rq, ServletResponse rs ) throws IOException, ServletException {
      if ( this.filterIterator.hasNext() ) {
        this.filterIterator.next().doFilter( rq, rs, this );
      } else {
        this.chain.doFilter( rq, rs );
      }
    }

    public void setFilterChain( FilterChain chain ) {
      this.chain = chain;
    }
  }

}
