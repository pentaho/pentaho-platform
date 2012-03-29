package org.pentaho.platform.repository2.unified.jcr;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.pentaho.platform.repository.RepositoryFilenameUtils;

/**
 * Special principal used in ACEs that contains two pieces of metadata about the ACL as a whole:
 * 
 * <ul>
 * <li>Owner: Separate from all ACEs, what Principal is the owner? (Owners can be treated specially.)</li>
 * <li>Entries Inheriting: Whether or not the ACEs of this ACL apply or instead an ancestor.</li>
 * </ul>
 * 
 * @author mlowery
 */
public class PentahoMetadataAcePrincipal implements PentahoInternalPrincipal {

  /**
   * Helps to guarantee uniqueness of this principal name so that it never matches a real principal.
   */
  public static final String PRINCIPAL_PREFIX = "org.pentaho.jcr"; //$NON-NLS-1$

  public static final char SEPARATOR = ':';

  private static final List<Character> RESERVED_CHARS = Arrays.asList(new Character[] { SEPARATOR });

  private final boolean entriesInheriting;

  private final String owner;

  private final String encodedName;

  public PentahoMetadataAcePrincipal(final String owner, final boolean entriesInheriting) {
    super();
    this.owner = owner;
    this.entriesInheriting = entriesInheriting;
    // escape just in case owner name contains separator character
    encodedName = PRINCIPAL_PREFIX + SEPARATOR + RepositoryFilenameUtils.escape(owner, RESERVED_CHARS) + SEPARATOR
        + entriesInheriting;
  }

  @Override
  public String getName() {
    return encodedName;
  }
  
  public String getOwner() {
    return owner;
  }
  
  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }
  
  public static boolean isPentahoMetadataAcePrincipal(final String name) {
    return name.startsWith(PRINCIPAL_PREFIX + SEPARATOR);
  }

}
