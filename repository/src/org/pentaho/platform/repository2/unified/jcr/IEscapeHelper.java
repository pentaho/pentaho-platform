package org.pentaho.platform.repository2.unified.jcr;

/**
 * Handles escaping and unescaping of illegal JCR characters.
 * 
 * @author mlowery
 */
public interface IEscapeHelper {

  /**
   * Escapes all illegal JCR name characters of a string.
   * @param the name to escape
   * @return the escaped name
   */
  String escapeIllegalJcrChars(final String name);

  /**
   * Unescapes previously escaped JCR name characters.
   * @param name the name to unescape 
   * @return the unescaped name
   */
  String unescapeIllegalJcrChars(final String name);
}
