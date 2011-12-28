package org.pentaho.platform.repository2;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs percent-encoding as per specification of {@code IUnifiedRepository}.
 * 
 * @author mlowery
 */
public class RepositoryNameUtils {

  /**
   * Performs lossless escaping of invalid characters in {@code name}.
   * 
   * @param name name to escape
   * @param reservedChars chars within name to escape
   * @return escaped name
   */
  public static String escape(final String name, final List<Character> reservedChars) {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    List<Character> mergedReservedChars = new ArrayList<Character>(reservedChars);
    if (!mergedReservedChars.contains('%')) {
      mergedReservedChars.add('%');
    }
    StringBuilder buffer = new StringBuilder(name.length() * 2);
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (mergedReservedChars.contains(ch)) {
        buffer.append('%');
        buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
        buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
      } else {
        buffer.append(ch);
      }
    }
    return buffer.toString();
  }

  /**
   * Undoes modifications of {@link #escape(String)} such that for all {@code String} {@code t}, 
   * {@code t.equals(unescape(escape(t)))}.
   * 
   * @param name name to unescape
   * @return unescaped name
   */
  public static String unescape(final String name) {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    StringBuilder buffer = new StringBuilder(name.length());
    String str = name;
    int i = str.indexOf('%');
    while (i > -1 && i + 2 < str.length()) {
      buffer.append(str.toCharArray(), 0, i);
      int a = Character.digit(str.charAt(i + 1), 16);
      int b = Character.digit(str.charAt(i + 2), 16);
      if (a > -1 && b > -1) {
        buffer.append((char) (a * 16 + b));
        str = str.substring(i + 3);
      } else {
        buffer.append('%');
        str = str.substring(i + 1);
      }
      i = str.indexOf('%');
    }
    buffer.append(str);
    return buffer.toString();
  }

}
