package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A struct for a {@link RepositoryFile} and its immediate children.* The immediate children is a list of this type 
 * (which can have children and so on). This class is immutable.
 * 
 * <p>* This is necessary since a {@code RepositoryFile} does not (by design) have a reference to its children. 
 * A {@code RepositoryFile} is meant to be a lightweight object representing metadata about a file in isolation. It does 
 * not have references to other {@code RepositoryFile} instances. The potential for misuse of a children field on 
 * {@code RepositoryFile} was great enough to warrant the creation of {@code RepositoryFileTree}. This separation of 
 * single file vs. tree is seen as a cleaner API with less confusion about when children is populated.</p>
 * 
 * @author mlowery
 */
public class RepositoryFileTree implements Comparable<RepositoryFileTree>, Serializable {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private RepositoryFile file;

  private List<RepositoryFileTree> children;

  // ~ Constructors ====================================================================================================

  public RepositoryFileTree(final RepositoryFile file, final List<RepositoryFileTree> children) {
    super();
    this.file = file;
    // defensive copy
    this.children = children != null ? new ArrayList<RepositoryFileTree>(children) : null;
  }

  // ~ Methods =========================================================================================================

  public RepositoryFile getFile() {
    return file;
  }

  /**
   * Children can be have one of three values:
   * <ul>
   * <li>null: children were not fetched; used for operations that support depth</li>
   * <li>empty list: there are no children for this file (i.e. file is not a folder or folder has no children)</li>
   * <li>non-empty list: this file is a folder and it has children</li>
   * </ul>
   * @return
   */
  public List<RepositoryFileTree> getChildren() {
    return children != null ? Collections.unmodifiableList(children) : null;
  }

  /**
   * Compare is based on the root file.
   */
  public int compareTo(final RepositoryFileTree other) {
    return file.compareTo(other.file);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepositoryFileTree other = (RepositoryFileTree) obj;
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return toString(0);
  }
  
  protected String toString(final int depth) {
    final String SPACER = "  "; //$NON-NLS-1$
    final String NL = "\n"; //$NON-NLS-1$
    final String SLASH = "/"; //$NON-NLS-1$
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      buf.append(SPACER);
    }
    buf.append(file.getName());
    if (file.isFolder()) {
      buf.append(SLASH);
    }
    buf.append(NL);
    if (children != null) {
      for (RepositoryFileTree subtree : children) {
        buf.append(subtree.toString(depth + 1));
      }
    }
    return buf.toString();
  }
  
  // ~ Inner classes ===================================================================================================

  public static class Builder {
    
    private List<Builder> children;

    private RepositoryFile file;

    public Builder(final RepositoryFile file, final List<Builder> children) {
      this.file = file;
      this.children = children;
    }
    
    /**
     * Creates a builder with an empty list as children.
     */
    public Builder(final RepositoryFile file) {
      this(file, new ArrayList<Builder>());
    }

    public Builder(final RepositoryFileTree other) {
      this(other.file, toBuilderChildren(other.children));
    }

    public RepositoryFileTree build() {
      return new RepositoryFileTree(file, toTreeChildren(children));
    }

    public Builder child(final Builder child) {
      this.children.add(child);
      return this;
    }
    
    private static List<RepositoryFileTree> toTreeChildren(final List<Builder> children) {
      List<RepositoryFileTree> converted = new ArrayList<RepositoryFileTree>(children.size());
      for (Builder child : children) {
        converted.add(child.build());
      }
      return converted;
    }
    
    private static List<Builder> toBuilderChildren(final List<RepositoryFileTree> children) {
      List<Builder> converted = new ArrayList<Builder>(children.size());
      for (RepositoryFileTree child : children) {
        converted.add(new Builder(child));
      }
      return converted;
    }
    
    /*
     * Allow code that is building the tree to see current children.
     */
    public List<Builder> getChildren() {
      return Collections.unmodifiableList(children);
    }
    
    /*
     * Allow code that is building the tree to see file.
     */
    public RepositoryFile getFile() {
      return file;
    }
  }

}
