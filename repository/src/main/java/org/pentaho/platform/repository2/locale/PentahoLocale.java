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


package org.pentaho.platform.repository2.locale;

import org.pentaho.platform.api.locale.IPentahoLocale;

import java.util.Locale;

/**
 * A wrapper class to the java {@link Locale}, since it is a final class. Only needed for web services
 * 
 * @author krivera
 */
public class PentahoLocale implements IPentahoLocale {

  private Locale locale;

  /**
   * Default empty constructor needed for web services
   */
  public PentahoLocale() {
    this.locale = Locale.getDefault();
  }

  public PentahoLocale( Locale locale ) {
    this.locale = locale;
  }

  @Override
  public Locale getLocale() {
    return this.locale;
  }

  @Override
  public String toString() {
    return locale.getLanguage();
  }
}
