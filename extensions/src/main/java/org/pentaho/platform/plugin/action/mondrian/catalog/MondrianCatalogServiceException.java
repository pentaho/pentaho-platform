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
