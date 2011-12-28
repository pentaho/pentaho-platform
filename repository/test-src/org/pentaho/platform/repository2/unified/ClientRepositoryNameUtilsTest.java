package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pentaho.platform.repository2.RepositoryNameUtils;

/**
 * Unit test for {@link RepositoryNameUtils}.
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class ClientRepositoryNameUtilsTest {

  @Test
  public void testEscape() {
    List<Character> emptyList = Collections.emptyList();
    
    // null name
    try {
      RepositoryNameUtils.escape(null, emptyList);
      fail();
    } catch (IllegalArgumentException e) {
      // passed
    }
    
    // empty list
    assertEquals("hello", RepositoryNameUtils.escape("hello", emptyList));
    
    // nothing to escape
    assertEquals("hello", RepositoryNameUtils.escape("hello", Arrays.asList(new Character[] { '/' })));
    
    // something to escape
    assertEquals("h%65llo", RepositoryNameUtils.escape("hello", Arrays.asList(new Character[] { 'e' })));

    // % in name
    assertEquals("hel%25lo", RepositoryNameUtils.escape("hel%lo", emptyList));
  }

  @Test
  public void testUnescape() {
    // null name
    try {
      RepositoryNameUtils.unescape(null);
      fail();
    } catch (IllegalArgumentException e) {
      // passed
    }
    
    // nothing to unescape
    assertEquals("hello", RepositoryNameUtils.unescape("hello"));
    
    // something to unescape
    assertEquals("hello", RepositoryNameUtils.unescape("h%65llo"));

    // % in name
    assertEquals("hel%lo", RepositoryNameUtils.unescape("hel%25lo"));
  }
  
}
