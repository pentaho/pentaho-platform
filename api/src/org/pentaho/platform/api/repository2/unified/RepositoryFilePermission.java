package org.pentaho.platform.api.repository2.unified;

/**
 * Repository file permission enumeration. These are the permission "bits." Loosely based on <a 
 * href="http://developer.apple.com/mac/library/documentation/Security/Conceptual/Security_Overview/Concepts/Concepts.html#//apple_ref/doc/uid/TP30000976-CH203-SW3">
 * Mac OS X File System Access Control Policy bits</a>.
 * 
 * <p>Why no {@code EXECUTE}?</p>
 * <p>
 * See <a href="http://issues.apache.org/jira/browse/JCR-2446">JCR-2446</a>.
 * </p>
 * 
 * <p>Why no {@code APPEND}?</p>
 * <p>
 * Some implementations of these bits may not be able to distinguish between a file create and update. In this case,
 * {@code APPEND} is useless. In the case of JCR, one might reasonably map APPEND to set_property and WRITE to add_node.
 * However, even an update on a file might involve the addition of a node.
 * </p>
 * 
 * <p>Why no {@code DELETE_CHILD}?</p>
 * <p>
 * {@code DELETE_CHILD} was believed to be too technical for end users to understand.
 * </p>
 * 
 * <p>Why no {@code DELETE}?</p>
 * <p>
 * {@code DELETE} mapped to jcr:removeNode which was also required for {@code WRITE}. So if you had {@code WRITE}, you
 * had {@code DELETE}, making {@code DELETE} redundant.
 * </p>
 * 
 * @author mlowery
 */
public enum RepositoryFilePermission {
  READ, WRITE, /*EXECUTE,*/ /*DELETE,*/ /*APPEND,*/ /*DELETE_CHILD,*/ READ_ACL, WRITE_ACL, ALL;
}
