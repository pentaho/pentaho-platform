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

package org.pentaho.platform.plugin.action.mondrian.catalog;

public class MondrianCatalogServiceException extends RuntimeException {

  private static final long serialVersionUID = 1852374894433624504L;

  public static enum Reason {
    GENERAL, ACCESS_DENIED, ALREADY_EXISTS, XMLA_SCHEMA_NAME_EXISTS
  };

  private Reason reason;

  public MondrianCatalogServiceException() {
    super();
  }

  public MondrianCatalogServiceException( final String msg, final Throwable throwable ) {
    this( msg, throwable, Reason.GENERAL );
  }

  public MondrianCatalogServiceException( final String msg, final Throwable throwable, final Reason reason ) {
    super( msg, throwable );
    this.reason = reason;
  }

  public MondrianCatalogServiceException( final String msg ) {
    this( msg, Reason.GENERAL );
  }

  public MondrianCatalogServiceException( final String msg, final Reason reason ) {
    super( msg );
    this.reason = reason;
  }

  public MondrianCatalogServiceException( final Throwable throwable ) {
    this( throwable, Reason.GENERAL );
  }

  public MondrianCatalogServiceException( final Throwable throwable, final Reason reason ) {
    super( throwable );
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }

}
