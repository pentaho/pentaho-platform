/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.commons.util.repository.type;

public class AllowableActions {

  private String parentId;
  private String parentUrl;
  private boolean canDelete;
  private boolean canUpdateProperties;
  private boolean canGetProperties;
  private boolean canGetRelationships;
  private boolean canGetParents;
  private boolean canGetFolderParent;
  private boolean canGetDescendants;
  private boolean canMove;
  private boolean canDeleteVersion;
  private boolean canDeleteContent;
  private boolean canCheckout;
  private boolean canCancelCheckout;
  private boolean canCheckin;
  private boolean canSetContent;
  private boolean canGetAllVersions;
  private boolean canAddToFolder;
  private boolean canRemoveFromFolder;
  private boolean canViewContent;
  private boolean canAddPolicy;
  private boolean canGetAppliedPolicies;
  private boolean canRemovePolicy;
  private boolean canGetChildren;
  private boolean canCreateDocument;
  private boolean canCreateFolder;
  private boolean canCreateRelationship;
  private boolean canCreatePolicy;
  private boolean canDeleteTree;

  public String getParentId() {
    return parentId;
  }

  public void setParentId( String parentId ) {
    this.parentId = parentId;
  }

  public String getParentUrl() {
    return parentUrl;
  }

  public void setParentUrl( String parentUrl ) {
    this.parentUrl = parentUrl;
  }

  public boolean isCanDelete() {
    return canDelete;
  }

  public void setCanDelete( boolean canDelete ) {
    this.canDelete = canDelete;
  }

  public boolean isCanUpdateProperties() {
    return canUpdateProperties;
  }

  public void setCanUpdateProperties( boolean canUpdateProperties ) {
    this.canUpdateProperties = canUpdateProperties;
  }

  public boolean isCanGetProperties() {
    return canGetProperties;
  }

  public void setCanGetProperties( boolean canGetProperties ) {
    this.canGetProperties = canGetProperties;
  }

  public boolean isCanGetRelationships() {
    return canGetRelationships;
  }

  public void setCanGetRelationships( boolean canGetRelationships ) {
    this.canGetRelationships = canGetRelationships;
  }

  public boolean isCanGetParents() {
    return canGetParents;
  }

  public void setCanGetParents( boolean canGetParents ) {
    this.canGetParents = canGetParents;
  }

  public boolean isCanGetFolderParent() {
    return canGetFolderParent;
  }

  public void setCanGetFolderParent( boolean canGetFolderParent ) {
    this.canGetFolderParent = canGetFolderParent;
  }

  public boolean isCanGetDescendants() {
    return canGetDescendants;
  }

  public void setCanGetDescendants( boolean canGetDescendants ) {
    this.canGetDescendants = canGetDescendants;
  }

  public boolean isCanMove() {
    return canMove;
  }

  public void setCanMove( boolean canMove ) {
    this.canMove = canMove;
  }

  public boolean isCanDeleteVersion() {
    return canDeleteVersion;
  }

  public void setCanDeleteVersion( boolean canDeleteVersion ) {
    this.canDeleteVersion = canDeleteVersion;
  }

  public boolean isCanDeleteContent() {
    return canDeleteContent;
  }

  public void setCanDeleteContent( boolean canDeleteContent ) {
    this.canDeleteContent = canDeleteContent;
  }

  public boolean isCanCheckout() {
    return canCheckout;
  }

  public void setCanCheckout( boolean canCheckout ) {
    this.canCheckout = canCheckout;
  }

  public boolean isCanCancelCheckout() {
    return canCancelCheckout;
  }

  public void setCanCancelCheckout( boolean canCancelCheckout ) {
    this.canCancelCheckout = canCancelCheckout;
  }

  public boolean isCanCheckin() {
    return canCheckin;
  }

  public void setCanCheckin( boolean canCheckin ) {
    this.canCheckin = canCheckin;
  }

  public boolean isCanSetContent() {
    return canSetContent;
  }

  public void setCanSetContent( boolean canSetContent ) {
    this.canSetContent = canSetContent;
  }

  public boolean isCanGetAllVersions() {
    return canGetAllVersions;
  }

  public void setCanGetAllVersions( boolean canGetAllVersions ) {
    this.canGetAllVersions = canGetAllVersions;
  }

  public boolean isCanAddToFolder() {
    return canAddToFolder;
  }

  public void setCanAddToFolder( boolean canAddToFolder ) {
    this.canAddToFolder = canAddToFolder;
  }

  public boolean isCanRemoveFromFolder() {
    return canRemoveFromFolder;
  }

  public void setCanRemoveFromFolder( boolean canRemoveFromFolder ) {
    this.canRemoveFromFolder = canRemoveFromFolder;
  }

  public boolean isCanViewContent() {
    return canViewContent;
  }

  public void setCanViewContent( boolean canViewContent ) {
    this.canViewContent = canViewContent;
  }

  public boolean isCanAddPolicy() {
    return canAddPolicy;
  }

  public void setCanAddPolicy( boolean canAddPolicy ) {
    this.canAddPolicy = canAddPolicy;
  }

  public boolean isCanGetAppliedPolicies() {
    return canGetAppliedPolicies;
  }

  public void setCanGetAppliedPolicies( boolean canGetAppliedPolicies ) {
    this.canGetAppliedPolicies = canGetAppliedPolicies;
  }

  public boolean isCanRemovePolicy() {
    return canRemovePolicy;
  }

  public void setCanRemovePolicy( boolean canRemovePolicy ) {
    this.canRemovePolicy = canRemovePolicy;
  }

  public boolean isCanGetChildren() {
    return canGetChildren;
  }

  public void setCanGetChildren( boolean canGetChildren ) {
    this.canGetChildren = canGetChildren;
  }

  public boolean isCanCreateDocument() {
    return canCreateDocument;
  }

  public void setCanCreateDocument( boolean canCreateDocument ) {
    this.canCreateDocument = canCreateDocument;
  }

  public boolean isCanCreateFolder() {
    return canCreateFolder;
  }

  public void setCanCreateFolder( boolean canCreateFolder ) {
    this.canCreateFolder = canCreateFolder;
  }

  public boolean isCanCreateRelationship() {
    return canCreateRelationship;
  }

  public void setCanCreateRelationship( boolean canCreateRelationship ) {
    this.canCreateRelationship = canCreateRelationship;
  }

  public boolean isCanCreatePolicy() {
    return canCreatePolicy;
  }

  public void setCanCreatePolicy( boolean canCreatePolicy ) {
    this.canCreatePolicy = canCreatePolicy;
  }

  public boolean isCanDeleteTree() {
    return canDeleteTree;
  }

  public void setCanDeleteTree( boolean canDeleteTree ) {
    this.canDeleteTree = canDeleteTree;
  }

}
