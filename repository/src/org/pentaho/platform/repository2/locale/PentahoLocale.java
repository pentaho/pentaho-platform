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
