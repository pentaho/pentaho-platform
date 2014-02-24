/**
 * 
 */
package org.pentaho.platform.security.policy.rolebased.actions;

import java.util.Locale;
import java.util.ResourceBundle;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.security.policy.rolebased.messages.Messages;

/**
 * 
 *
 */
public abstract class AbstractAuthorizationAction implements IAuthorizationAction {

  protected ResourceBundle getResourceBundle( String localeString ) {
    final String UNDERSCORE = "_"; //$NON-NLS-1$
    Locale locale;
    if ( localeString == null ) {
      return Messages.getInstance().getBundle();
    } else {
      String[] tokens = localeString.split( UNDERSCORE );
      if ( tokens.length == 3 ) {
        locale = new Locale( tokens[0], tokens[1], tokens[2] );
      } else if ( tokens.length == 2 ) {
        locale = new Locale( tokens[0], tokens[1] );
      } else {
        locale = new Locale( tokens[0] );
      }
      return Messages.getInstance().getBundle( locale );
    }
  }
}
