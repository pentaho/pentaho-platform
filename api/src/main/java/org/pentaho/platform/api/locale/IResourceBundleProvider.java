package org.pentaho.platform.api.locale;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

@FunctionalInterface
public interface IResourceBundleProvider {
  /**
   * Returns the resource bundle for the given locale.
   * If the given locale is {@code null}, the current locale will be used.
   *
   * @param locale the locale to use
   * @return the resource bundle for the given locale, or the current locale if {@code null}
   * @throws java.util.MissingResourceException if no resource bundle for the given locale can be found.
   */
  @NonNull
  ResourceBundle getResourceBundle( @Nullable Locale locale );

  /**
   * Returns the resource bundle for the current locale.
   *
   * @return the resource bundle for the current locale
   * @throws java.util.MissingResourceException if no resource bundle for the current locale can be found.
   */
  @NonNull
  default ResourceBundle getResourceBundle() {
    return getResourceBundle( null );
  }
}
