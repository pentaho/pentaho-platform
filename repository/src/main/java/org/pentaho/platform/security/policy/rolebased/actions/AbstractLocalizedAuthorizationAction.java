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

package org.pentaho.platform.security.policy.rolebased.actions;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.engine.security.authorization.core.AbstractAuthorizationAction;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The {@code AbstractAuthorizationAction} class provides a base implementation for authorization actions that builds
 * upon the {@link AbstractAuthorizationAction} class to add basic support for localization and resource bundles.
 * <p>
 * The action's localized display name and description are retrieved from a resource bundle, returned by
 * {@link #getResourceBundle(Locale)}, using resource string keys based on the action's name, {@link #getName()}.
 * See {@link org.pentaho.platform.api.engine.IAuthorizationAction#getLocalizedDisplayName(String)} and {@link #getLocalizedDescription(String)} for details.
 * <p>
 * Methods accepting locale strings parse them using the {@link #parseLocale(String)} method.
 * <p>
 * When a given locale string is {@code null} or empty, the default locale is obtained from the
 * {@link #getDefaultLocale()} method.
 */
public abstract class AbstractLocalizedAuthorizationAction extends AbstractAuthorizationAction {

  /**
   * Gets the resource bundle for a specific locale.
   * <p>
   * This implementation parses the specified locale string using {@link #parseLocale(String)} and
   * delegates to {@link #getResourceBundle(Locale)}.
   *
   * @param localeString The locale string to use for localization.
   * @return The resource bundle for the specified locale.
   */
  @NonNull
  protected ResourceBundle getResourceBundle( @Nullable String localeString ) {
    return getResourceBundle( parseLocale( localeString ) );
  }

  /**
   * Gets the resource bundle for a specific locale.
   * <p>
   * If the locale is not specified, the locale obtained by {@link #getDefaultLocale()} is used.
   * <p>
   * The resource bundle for that locale is then retrieved from {@link Messages#getInstance()}.
   *
   * @param locale The locale to use for localization.
   * @return The resource bundle for the specified locale.
   */
  @NonNull
  protected ResourceBundle getResourceBundle( @Nullable Locale locale ) {
    return Messages.getInstance().getBundle( Objects.requireNonNullElseGet( locale, this::getDefaultLocale ) );
  }

  /**
   * Gets the default locale.
   * <p>
   * The default implementation returns the Pentaho System's current thread's locale, as determined by
   * {@link LocaleHelper#getLocale()}.
   *
   * @return The default locale.
   */
  @NonNull
  protected Locale getDefaultLocale() {
    return LocaleHelper.getLocale();
  }

  /**
   * Parses a given locale string according to IETF BCP 47, as supported by {@link Locale}, and adding support for
   * underscore-separated sections.
   * <p>
   * If the specified locale string is {@code null} or empty, {@code null} is returned.
   * <p>
   * Otherwise, the locale string is split into sections separated by underscore and then proceeds to create a
   * {@link Locale} instance as follows:
   * <ol>
   *   <li>
   *     If a single section is provided, it is treated as a language code parseable by the
   *     {@link Locale#Locale(String)} constructor.
   *   </li>
   *   <li>
   *     If two sections are provided, they are treated as a language code and a country code, and passed to the
   *     {@link Locale#Locale(String, String)} constructor.
   *   </li>
   *   <li>
   *     If three sections are provided, they are treated as a language code, a country code, and a variant code, and
   *     passed to the {@link Locale#Locale(String, String, String)} constructor.
   *   </li>
   *   <li>
   *     If additional sections are present, only the first section is considered, and it is passed to the
   *     {@link Locale#Locale(String)} constructor.
   *   </li>
   * </ol>
   *
   * @param localeString The locale string to parse.
   * @return A {@link Locale} instance representing the parsed locale string.
   */
  @Nullable
  protected Locale parseLocale( @Nullable String localeString ) {
    final String UNDERSCORE = "_";

    if ( StringUtil.isEmpty( localeString ) ) {
      return null;
    }

    String[] tokens = localeString.split( UNDERSCORE );
    if ( tokens.length == 3 ) {
      return new Locale( tokens[ 0 ], tokens[ 1 ], tokens[ 2 ] );
    }

    if ( tokens.length == 2 ) {
      return new Locale( tokens[ 0 ], tokens[ 1 ] );
    }

    return new Locale( tokens[ 0 ] );
  }

  /**
   * Gets the localized display name of the action for a specific locale.
   * <p>
   * This implementation retrieves the display name from the resource bundle using a resource string key equal to the
   * {@link #getName() action name}.
   * For example, if the action name is {@code org.pentaho.action} then that is also the resource string key.
   *
   * @param localeString The locale to use for localization.
   * @return The localized name.
   */
  @NonNull
  @Override
  public String getLocalizedDisplayName( @Nullable String localeString ) {
    return getResourceBundle( localeString ).getString( getName() );
  }

  /**
   * Gets the localized description of the action for a specific locale.
   * <p>
   * This implementation retrieves the description from the resource bundle using a resource string key composed of the
   * {@link #getName() action name} followed by {@code .description}.
   * For example, if the action name is {@code org.pentaho.action} then the resource string key is
   * {@code org.pentaho.action.description}.
   *
   * @param localeString The locale to use for localization.
   * @return The localized description.
   */
  @Nullable
  @Override
  public String getLocalizedDescription( @Nullable String localeString ) {
    return getResourceBundle( localeString ).getString( getName() + ".description" );
  }
}
