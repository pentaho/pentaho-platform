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


package org.pentaho.platform.repository2.unified.jcr;

import org.springframework.extensions.jcr.JcrConstants;

import javax.jcr.Session;

/**
 * Pentaho JCR constants. The {@code get* } methods automatically prepend the appropriate namespace prefix.
 * 
 * @author mlowery
 */
public class PentahoJcrConstants extends JcrConstants {

  // ~ Static fields/initializers
  // ======================================================================================

  /**
   * Pentaho item name namespace.
   */
  public static final String PHO_NS = "http://www.pentaho.org/jcr/2.0"; //$NON-NLS-1$

  /**
   * Pentaho node type namespace.
   */
  public static final String PHO_NT_NS = "http://www.pentaho.org/jcr/nt/2.0"; //$NON-NLS-1$

  /**
   * Pentaho mixin type namespace.
   */
  public static final String PHO_MIX_NS = "http://www.pentaho.org/jcr/mix/2.0"; //$NON-NLS-1$

  private static final String PHO_MIX_VERSIONABLE = "pentahoVersionable"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOFILE = "pentahoFile"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOFOLDER = "pentahoFolder"; //$NON-NLS-1$

  private static final String PHO_NT_INTERNALFOLDER = "pentahoInternalFolder"; //$NON-NLS-1$

  private static final String PHO_NT_LOCKTOKENSTORAGE = "pentahoLockTokenStorage"; //$NON-NLS-1$

  private static final String PHO_NT_LOCALIZEDSTRING = "localizedString"; //$NON-NLS-1$

  private static final String PHO_NT_LOCALE = "locale"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOHIERARCHYNODE = "pentahoHierarchyNode"; //$NON-NLS-1$

  private static final String PHO_LASTMODIFIED = "lastModified"; //$NON-NLS-1$

  public static String PHO_CONTENTCREATOR = "contentCreator"; //$NON-NLS-1$

  public static String PHO_SCHEDULENAME = "scheduleName"; //$NON-NLS-1$

  private static final String PHO_LOCKEDNODEREF = "lockedNodeRef"; //$NON-NLS-1$

  private static final String PHO_LOCKTOKEN = "lockToken"; //$NON-NLS-1$

  private static final String PHO_BOUNDROLES = "boundRoles"; //$NON-NLS-1$

  private static final String PHO_VERSIONAUTHOR = "versionAuthor"; //$NON-NLS-1$

  private static final String PHO_VERSIONMESSAGE = "versionMessage"; //$NON-NLS-1$

  private static final String PHO_ACLONLYCHANGE = "aclOnlyChange"; //$NON-NLS-1$

  private static final String PHO_TITLE = "title"; //$NON-NLS-1$

  private static final String PHO_DESCRIPTION = "description"; //$NON-NLS-1$

  private static final String PHO_LOCALES = "locales"; //$NON-NLS-1$

  private static final String PHO_CONTENTTYPE = "contentType"; //$NON-NLS-1$

  private static final String PHO_HIDDEN = "hidden"; //$NON-NLS-1$

  private static String PHO_DELETEDDATE = "deletedDate"; //$NON-NLS-1$

  private static String PHO_FILESIZE = "fileSize"; //$NON-NLS-1$

  private static String PHO_METADATA = "metadata"; //$NON-NLS-1$

  private static String PHO_ORIGPARENTFOLDERPATH = "origParentFolderPath"; //$NON-NLS-1$

  private static String PHO_ORIGNAME = "origName"; //$NON-NLS-1$

  private static String PHO_ACL_MANAGEMENT_PRIVILEGE = "aclManagement"; //$NON-NLS-1$

  private static String PHO_ACLNODE = "aclNode";

  /**
   * Same abstraction as {@code Locale.ROOT}.
   */
  private static final String PHO_ROOTLOCALE = "rootLocale"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public PentahoJcrConstants( final Session session ) {
    super( session );
  }

  public PentahoJcrConstants( final Session session, final boolean cache ) {
    super( session, cache );
  }

  // ~ Methods
  // =========================================================================================================

  public String getPHO_NT_PENTAHOFILE() {
    return resolveName( PHO_NT_NS, PHO_NT_PENTAHOFILE );
  }

  public String getPHO_MIX_VERSIONABLE() {
    return resolveName( PHO_MIX_NS, PHO_MIX_VERSIONABLE );
  }

  public String getPHO_NT_INTERNALFOLDER() {
    return resolveName( PHO_NT_NS, PHO_NT_INTERNALFOLDER );
  }

  public String getPHO_NT_LOCKTOKENSTORAGE() {
    return resolveName( PHO_NT_NS, PHO_NT_LOCKTOKENSTORAGE );
  }

  public String getPHO_LOCKEDNODEREF() {
    return resolveName( PHO_NS, PHO_LOCKEDNODEREF );
  }

  public String getPHO_LOCKTOKEN() {
    return resolveName( PHO_NS, PHO_LOCKTOKEN );
  }

  public String getPHO_VERSIONAUTHOR() {
    return resolveName( PHO_NS, PHO_VERSIONAUTHOR );
  }

  public String getPHO_VERSIONMESSAGE() {
    return resolveName( PHO_NS, PHO_VERSIONMESSAGE );
  }

  public String getPHO_LASTMODIFIED() {
    return resolveName( PHO_NS, PHO_LASTMODIFIED );
  }

  public String getPHO_TITLE() {
    return resolveName( PHO_NS, PHO_TITLE );
  }

  public String getPHO_DESCRIPTION() {
    return resolveName( PHO_NS, PHO_DESCRIPTION );
  }

  public String getPHO_LOCALES() {
    return resolveName( PHO_NS, PHO_LOCALES );
  }

  public String getPHO_ROOTLOCALE() {
    return resolveName( PHO_NS, PHO_ROOTLOCALE );
  }

  public String getPHO_NT_LOCALIZEDSTRING() {
    return resolveName( PHO_NT_NS, PHO_NT_LOCALIZEDSTRING );
  }

  public String getPHO_NT_LOCALE() {
    return resolveName( PHO_NT_NS, PHO_NT_LOCALE );
  }

  public String getPHO_NT_PENTAHOFOLDER() {
    return resolveName( PHO_NT_NS, PHO_NT_PENTAHOFOLDER );
  }

  public String getPHO_NT_PENTAHOHIERARCHYNODE() {
    return resolveName( PHO_NT_NS, PHO_NT_PENTAHOHIERARCHYNODE );
  }

  public String getPHO_BOUNDROLES() {
    return resolveName( PHO_NS, PHO_BOUNDROLES );
  }

  public String getPHO_CONTENTTYPE() {
    return resolveName( PHO_NS, PHO_CONTENTTYPE );
  }

  public String getPHO_HIDDEN() {
    return resolveName( PHO_NS, PHO_HIDDEN );
  }

  public String getPHO_DELETEDDATE() {
    return resolveName( PHO_NS, PHO_DELETEDDATE );
  }

  public String getPHO_FILESIZE() {
    return resolveName( PHO_NS, PHO_FILESIZE );
  }

  public String getPHO_METADATA() {
    return resolveName( PHO_NS, PHO_METADATA );
  }

  public String getPHO_ORIGPARENTFOLDERPATH() {
    return resolveName( PHO_NS, PHO_ORIGPARENTFOLDERPATH );
  }

  public String getPHO_ORIGNAME() {
    return resolveName( PHO_NS, PHO_ORIGNAME );
  }

  public String getPHO_ACLONLYCHANGE() {
    return resolveName( PHO_NS, PHO_ACLONLYCHANGE );
  }

  public String getPHO_ACLMANAGEMENT_PRIVILEGE() {
    return resolveName( PHO_NS, PHO_ACL_MANAGEMENT_PRIVILEGE );
  }

  public String getPHO_ACLNODE() { return resolveName( PHO_NS, PHO_ACLNODE ); }

}
