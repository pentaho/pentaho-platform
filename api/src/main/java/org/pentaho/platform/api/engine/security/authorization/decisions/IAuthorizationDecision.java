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

package org.pentaho.platform.api.engine.security.authorization.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;

/**
 * The {@code IAuthorizationDecision} interface represents a grant or denial result of an authorization process.
 * <p>
 * A decision cannot represent an abstention result. To that end, an authorization rule can return an empty optional
 * result, from its {@link IAuthorizationRule#authorize(IAuthorizationRequest, IAuthorizationContext)} method.
 * <p>
 * The {@link Object#toString()} method should provide a description of the decision that is suitable for debugging,
 * auditing and logging purposes. For example:
 * <pre><code>
 * public String toString() {
 *   return String.format(
 *     "%s [granted=`%s`]",
 *     getClass().getSimpleName(),
 *     isGranted() );
 * }
 * </code></pre>
 */
public interface IAuthorizationDecision {
  /**
   * Gets the authorization request that this decision grants or denies.
   */
  @NonNull
  IAuthorizationRequest getRequest();

  /**
   * Indicates whether the authorization was granted.
   *
   * @return {@code true} if the authorization was granted; {@code false} if it was denied.
   * @see #isDenied()
   */
  boolean isGranted();

  /**
   * Indicates whether the authorization was denied.
   * <p>
   * This method is just a convenience for {@code !isGranted()}.
   *
   * @return {@code true} if the authorization was denied; {@code false} if it was granted.
   * @see #isGranted()
   */
  default boolean isDenied() {
    return !isGranted();
  }

  /**
   * Gets a short, human-readable justification for the authorization decision.
   * <p>
   * Should be localized in the current (thread's) system locale, regardless of the user for which the authorization
   * was evaluated.
   * <p>
   * Should be relatively short, suitable for displaying in a short space in a user interface.
   * <p>
   * The text should assume that the major details of the authorization request are known, as well as the decision's
   * granted status. The text should read as, depending on the granted status, having the following implied prefixes
   * "Granted because ..." or "Denied because ...". For example, "(granted because) Has role admin", or
   * "(denied because) Not has role admin (but should)".
   * <p>
   * Should not attempt to describe the decision recursively, especially if generally composed of a variable number of
   * other decisions. At most include the first contained decision.
   * <p>
   * Can be empty, for degenerate decision objects with no additional information, or for composite decisions.
   * <p>
   * Text can contain simple markdown-like syntax, that UIs should attempt to honor if possible:
   * <ul>
   *   <li>{@code *text*} or {@code _text_} for italic</li>
   *   <li>{@code **text**} or {@code __text__} for bold</li>
   *   <li>{@code ***text***} or {@code ___text___} for bold and italic</li>
   *   <li>{@code \*} for literal {@code *}</li>
   *   <li>{@code \_} for literal {@code _}</li>
   *   <li>{@code *unbalanced}, {@code *mismatch_ed} or empty delimiters, {@code __} and {@code **}, are left as is</li>
   * </ul>
   * <p>
   * Examples:
   * <ul>
   *   <li>"" - degenerate, empty, or composite</li>
   *   <li>"Has _Other_ permission"</li>
   *   <li>"Not has **Other** permission" - for a decision denied due to requiring a grant for another one (if !A
   *   then !C)</li>
   *   <li>"From role 'Administrator'"</li>
   * </ul>
   *
   * @return A short justification for the decision.
   */
  @NonNull
  String getShortJustification();

  /**
   * Gets the base decision type.
   *
   * @return The base decision type class; one of {@link IAllAuthorizationDecision}, {@link IAnyAuthorizationDecision},
   * {@link IOpposedAuthorizationDecision}, or {@link IAuthorizationDecision}.
   */
  @NonNull
  default Class<? extends IAuthorizationDecision> getBaseType() {

    if ( this instanceof ICompositeAuthorizationDecision ) {
      if ( this instanceof IAllAuthorizationDecision ) {
        return IAllAuthorizationDecision.class;
      }

      assert this instanceof IAnyAuthorizationDecision;

      return IAnyAuthorizationDecision.class;
    }


    if ( this instanceof IOpposedAuthorizationDecision ) {
      return IOpposedAuthorizationDecision.class;
    }

    return IAuthorizationDecision.class;
  }
}
