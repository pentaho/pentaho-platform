/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.repository2.unified.jcr;

import javax.jcr.Session;

import org.springframework.extensions.jcr.JcrConstants;

/**
 * Pentaho JCR constants. The {@code get* } methods automatically prepend the appropriate namespace prefix.
 * 
 * @author mlowery
 */
public class PentahoJcrConstants extends JcrConstants {

  // ~ Static fields/initializers ======================================================================================

  /**
   * Pentaho item name namespace.
   */
  public static final String PHO_NS = "http://www.pentaho.org/jcr/1.0"; //$NON-NLS-1$

  /**
   * Pentaho node type namespace.
   */
  public static final String PHO_NT_NS = "http://www.pentaho.org/jcr/nt/1.0"; //$NON-NLS-1$

  /**
   * Pentaho mixin type namespace.
   */
  public static final String PHO_MIX_NS = "http://www.pentaho.org/jcr/mix/1.0"; //$NON-NLS-1$

  private static final String PHO_MIX_LOCKABLE = "pentahoLockable"; //$NON-NLS-1$

  private static final String PHO_MIX_VERSIONABLE = "pentahoVersionable"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOFILE = "pentahoFile"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOFOLDER = "pentahoFolder"; //$NON-NLS-1$

  private static final String PHO_NT_INTERNALFOLDER = "pentahoInternalFolder"; //$NON-NLS-1$

  private static final String PHO_NT_LOCKTOKENSTORAGE = "pentahoLockTokenStorage"; //$NON-NLS-1$

  private static final String PHO_NT_LOCALIZEDSTRING = "localizedString"; //$NON-NLS-1$

  private static final String PHO_NT_PENTAHOHIERARCHYNODE = "pentahoHierarchyNode"; //$NON-NLS-1$

  private static final String PHO_LASTMODIFIED = "lastModified"; //$NON-NLS-1$

  public static final String PHO_LOCKMESSAGE = "lockMessage"; //$NON-NLS-1$

  public static final String PHO_LOCKDATE = "lockDate"; //$NON-NLS-1$

  public static String PHO_CONTENTCREATOR = "contentCreator";  //$NON-NLS-1$

  private static final String PHO_LOCKEDNODEREF = "lockedNodeRef"; //$NON-NLS-1$

  private static final String PHO_LOCKTOKEN = "lockToken"; //$NON-NLS-1$

  private static final String PHO_BOUNDROLES = "boundRoles"; //$NON-NLS-1$
  
  private static final String PHO_VERSIONAUTHOR = "versionAuthor"; //$NON-NLS-1$

  private static final String PHO_VERSIONMESSAGE = "versionMessage"; //$NON-NLS-1$

  private static final String PHO_ACLOWNERNAME = "aclOwnerName"; //$NON-NLS-1$
  
  private static final String PHO_ACLOWNERTYPE = "aclOwnerType"; //$NON-NLS-1$

  private static final String PHO_TITLE = "title"; //$NON-NLS-1$

  private static final String PHO_DESCRIPTION = "description"; //$NON-NLS-1$
  
  private static final String PHO_CONTENTTYPE = "contentType"; //$NON-NLS-1$

  private static final String PHO_HIDDEN = "hidden"; //$NON-NLS-1$

  private static String PHO_DELETEDDATE = "deletedDate"; //$NON-NLS-1$
  
  private static String PHO_FILESIZE = "fileSize";  //$NON-NLS-1$
  
  private static String PHO_METADATA = "metadata";  //$NON-NLS-1$

  private static String PHO_ORIGPARENTFOLDERPATH = "origParentFolderPath"; //$NON-NLS-1$
  
  private static String PHO_ORIGNAME = "origName"; //$NON-NLS-1$
  
  /**
   * Same abstraction as {@code Locale.ROOT}.
   */
  private static final String PHO_ROOTLOCALE = "rootLocale"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public PentahoJcrConstants(final Session session) {
    super(session);
  }

  public PentahoJcrConstants(final Session session, final boolean cache) {
    super(session, cache);
  }

  // ~ Methods ========================================================================================================= 

  public String getPHO_NT_PENTAHOFILE() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOFILE);
  }

  public String getPHO_MIX_LOCKABLE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_LOCKABLE);
  }

  public String getPHO_MIX_VERSIONABLE() {
    return resolveName(PHO_MIX_NS, PHO_MIX_VERSIONABLE);
  }

  public String getPHO_NT_INTERNALFOLDER() {
    return resolveName(PHO_NT_NS, PHO_NT_INTERNALFOLDER);
  }

  public String getPHO_NT_LOCKTOKENSTORAGE() {
    return resolveName(PHO_NT_NS, PHO_NT_LOCKTOKENSTORAGE);
  }

  public String getPHO_LOCKMESSAGE() {
    return resolveName(PHO_NS, PHO_LOCKMESSAGE);
  }

  public String getPHO_LOCKDATE() {
    return resolveName(PHO_NS, PHO_LOCKDATE);
  }

  public String getPHO_LOCKEDNODEREF() {
    return resolveName(PHO_NS, PHO_LOCKEDNODEREF);
  }

  public String getPHO_LOCKTOKEN() {
    return resolveName(PHO_NS, PHO_LOCKTOKEN);
  }

  public String getPHO_VERSIONAUTHOR() {
    return resolveName(PHO_NS, PHO_VERSIONAUTHOR);
  }

  public String getPHO_VERSIONMESSAGE() {
    return resolveName(PHO_NS, PHO_VERSIONMESSAGE);
  }

  public String getPHO_ACLOWNERNAME() {
    return resolveName(PHO_NS, PHO_ACLOWNERNAME);
  }

  public String getPHO_ACLOWNERTYPE() {
    return resolveName(PHO_NS, PHO_ACLOWNERTYPE);
  }

  public String getPHO_LASTMODIFIED() {
    return resolveName(PHO_NS, PHO_LASTMODIFIED);
  }

  public String getPHO_TITLE() {
    return resolveName(PHO_NS, PHO_TITLE);
  }

  public String getPHO_DESCRIPTION() {
    return resolveName(PHO_NS, PHO_DESCRIPTION);
  }

  public String getPHO_ROOTLOCALE() {
    return resolveName(PHO_NS, PHO_ROOTLOCALE);
  }

  public String getPHO_NT_LOCALIZEDSTRING() {
    return resolveName(PHO_NT_NS, PHO_NT_LOCALIZEDSTRING);
  }

  public String getPHO_NT_PENTAHOFOLDER() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOFOLDER);
  }

  public String getPHO_NT_PENTAHOHIERARCHYNODE() {
    return resolveName(PHO_NT_NS, PHO_NT_PENTAHOHIERARCHYNODE);
  }

  public String getPHO_BOUNDROLES() {
    return resolveName(PHO_NS, PHO_BOUNDROLES);
  }
  
  public String getPHO_CONTENTTYPE() {
    return resolveName(PHO_NS, PHO_CONTENTTYPE);
  }

  public String getPHO_HIDDEN() {
    return resolveName(PHO_NS, PHO_HIDDEN);
  }

  public String getPHO_DELETEDDATE() {
    return resolveName(PHO_NS, PHO_DELETEDDATE);
  }
  
  public String getPHO_FILESIZE() {
    return resolveName(PHO_NS, PHO_FILESIZE);
  }

  public String getPHO_METADATA() {
    return resolveName(PHO_NS, PHO_METADATA);
  }

  public String getPHO_ORIGPARENTFOLDERPATH() {
    return resolveName(PHO_NS, PHO_ORIGPARENTFOLDERPATH);
  }
  
  public String getPHO_ORIGNAME() {
    return resolveName(PHO_NS, PHO_ORIGNAME);
  }

}
