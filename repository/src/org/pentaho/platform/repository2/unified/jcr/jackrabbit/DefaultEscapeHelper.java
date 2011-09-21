package org.pentaho.platform.repository2.unified.jcr.jackrabbit;

import org.apache.jackrabbit.util.Text;
import org.pentaho.platform.repository2.unified.jcr.IEscapeHelper;

/**
 * Default implementation of {@link IEscapeHelper}.
 * 
 * @author mlowery
 */
public class DefaultEscapeHelper implements IEscapeHelper {

  public String escapeIllegalJcrChars(final String name) {
    return Text.escapeIllegalJcrChars(name);
  }

  public String unescapeIllegalJcrChars(final String name) {
    return Text.unescapeIllegalJcrChars(name);
  }

}
