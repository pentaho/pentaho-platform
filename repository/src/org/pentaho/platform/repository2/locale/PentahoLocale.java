package org.pentaho.platform.repository2.locale;

import java.util.Locale;

import org.pentaho.platform.api.locale.IPentahoLocale;

/**
 * A wrapper class to the java {@link Locale}, since it is a final class. Only needed for web services  
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

  public PentahoLocale(Locale locale) {
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
