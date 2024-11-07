/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.engine;

/**
 * This is a port from spring-security 2.0.8.RELEASE
 * @see https://github.com/spring-projects/spring-security/blob/2.0.8.RELEASE/core/src/main/java/org/springframework/security/acl/basic/BasicAclEntry.java
 */
@Deprecated
public interface IPentahoBasicAclEntry extends IAclEntry {

  /**
   * Indicates the domain object instance that is subject of this <code>BasicAclEntry</code>. This
   * information may be of interest to relying classes (voters and business methods) that wish to know the actual
   * origination of the ACL entry (so as to distinguish individual ACL entries from others contributed by the
   * inheritance hierarchy).
   *
   * @return the ACL object identity that is subject of this ACL entry (never <code>null</code>)
   */
  IPentahoAclObjectIdentity getAclObjectIdentity();

  /**
   * Indicates any ACL parent of the domain object instance. This is used by <code>BasicAclProvider</code> to
   * walk the inheritance hierarchy. An domain object instance need <b>not</b> have a parent.
   *
   * @return the ACL object identity that is the parent of this ACL entry (may be <code>null</code> if no parent
   *         should be consulted)
   */
  IPentahoAclObjectIdentity getAclObjectParentIdentity();

  /**
   * Access control lists in this package are based on bit masking. The integer value of the bit mask can be
   * obtained from this method.
   *
   * @return the bit mask applicable to this ACL entry (zero indicates a bit mask where no permissions have been
   *         granted)
   */
  int getMask();

  /**
   * A domain object instance will usually have multiple <code>BasicAclEntry</code>s. Each separate
   * <code>BasicAclEntry</code> applies to a particular "recipient". Typical examples of recipients include (but do
   * not necessarily have to include) usernames, role names, complex granted authorities etc.<P><B>It is
   * essential that only one <code>BasicAclEntry</code> exists for a given recipient</B>. Otherwise conflicts as to
   * the mask that should apply to a given recipient will occur.</p>
   *  <P>This method indicates which recipient this <code>BasicAclEntry</code> applies to. The returned
   * object type will vary depending on the type of recipient. For instance, it might be a <code>String</code>
   * containing a username, or a <code>GrantedAuthorityImpl</code> containing a complex granted authority that is
   * being granted the permissions contained in this access control entry. The {@link EffectiveAclsResolver} and
   * {@link BasicAclProvider#getAcls(Object,org.springframework.security.Authentication)} can process the different recipient
   * types and return only those that apply to a specified <code>Authentication</code> object.</p>
   *
   * @return the recipient of this access control list entry (never <code>null</code>)
   */
  Object getRecipient();

  /**
   * Determine if the mask of this entry includes this permission or not
   *
   * @param permissionToCheck
   *
   * @return if the entry's mask includes this permission
   */
  boolean isPermitted( int permissionToCheck );

  /**
   * This setter should <B>only</B> be used by DAO implementations.
   *
   * @param aclObjectIdentity an object which can be used to uniquely identify the domain object instance subject of
   *        this ACL entry
   */
  void setAclObjectIdentity( IPentahoAclObjectIdentity aclObjectIdentity );

  /**
   * This setter should <B>only</B> be used by DAO implementations.
   *
   * @param aclObjectParentIdentity an object which represents the parent of the domain object instance subject of
   *        this ACL entry, or <code>null</code> if either the domain object instance has no parent or its parent
   *        should be not used to compute an inheritance hierarchy
   */
  void setAclObjectParentIdentity( IPentahoAclObjectIdentity aclObjectParentIdentity );

  /**
   * This setter should <B>only</B> be used by DAO implementations.
   *
   * @param mask the integer representing the permissions bit mask
   */
  void setMask( int mask );

  /**
   * This setter should <B>only</B> be used by DAO implementations.
   *
   * @param recipient a representation of the recipient of this ACL entry that makes sense to an
   *        <code>EffectiveAclsResolver</code> implementation
   */
  void setRecipient( Object recipient );

}
