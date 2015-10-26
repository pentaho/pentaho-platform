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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class RepositoryRequest {
  private static final Pattern FILES_TYPES_PATTERN = FILES_TYPE_FILTER.getRegExPattern();

  private static final Pattern FILES_MEMBERS_INCLUDE_PATTERN = Pattern.compile( "includeMembers=(.+)" );
  private static final Pattern FILES_MEMBERS_EXCLUDE_PATTERN = Pattern.compile( "excludeMembers=(.+)" );
  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  private String path;
  private boolean showHidden = false;
  private boolean includeAcls = false;
  private Integer depth = -1;
  private FILES_TYPE_FILTER types = FILES_TYPE_FILTER.FILES_FOLDERS;
  private Set<String> includeMemberSet = null;
  private Set<String> excludeMemberSet = null;

  private transient String workingFilter; // temporary storage of remaining filter text as it is parsed.
  private String childNodeFilter;

  /**
   * This class encapsulates the parameters received by the "children" and "tree" REST calls. It provides default values
   * for parameters not specified and breaks down the legacy "filter" parameters into it's component parts.
   */
  public RepositoryRequest() {
  }

  public RepositoryRequest( String path, Boolean showHidden, Integer depth, String legacyFilter ) {
    this.path = path;
    this.showHidden = showHidden == null ? false : showHidden;
    setDepth( depth );
    setLegacyFilter( legacyFilter );
  }

  /**
   * Strips out and sets the file types portion of the legacyFilter
   */
  private void parseOutFileTypes() {
    // Check for File type filter
    types = FILES_TYPE_FILTER.FILES_FOLDERS;
    StringBuilder strippedFilter = new StringBuilder();
    if ( !workingFilter.isEmpty() ) {
      String[] parts = workingFilter.split( "\\|" );
      for ( String part : parts ) {
        Matcher m = FILES_TYPES_PATTERN.matcher( part );
        if ( m.matches() ) {
          FILES_TYPE_FILTER newType = FILES_TYPE_FILTER.valueOf( m.group( 1 ) );
          if ( newType != null ) {
            types = newType;
            // note it only makes sense to have FILES if the depth is 1
            if ( types == FILES_TYPE_FILTER.FILES && depth != 1 ) {
              types = FILES_TYPE_FILTER.FILES_FOLDERS;
            }
          }
        } else {
          appendFilter( strippedFilter, part );
        }
      }
    }
    workingFilter = strippedFilter.toString();
  }

  private void appendFilter( StringBuilder strippedFilter, String part ) {
    if ( strippedFilter.length() != 0 ) {
      strippedFilter.append( "|" );
    }
    strippedFilter.append( part );
  }

  /**
   * Strips out and sets the include/exclude member sets from the filter
   */
  private void parseOutIncludeExclude() {
    includeMemberSet = parseOutPattern( FILES_MEMBERS_INCLUDE_PATTERN );
    excludeMemberSet = parseOutPattern( FILES_MEMBERS_EXCLUDE_PATTERN );
    if ( ( includeMemberSet != null && !includeMemberSet.isEmpty() )
        && ( excludeMemberSet != null && !excludeMemberSet.isEmpty() ) ) {
      throw new RuntimeException( "Cannot include and exclude values in the same Legacy Filter" );
    }
  }

  private Set<String> parseOutPattern( Pattern pattern ) {
    StringBuilder strippedFilter = new StringBuilder();
    String[] parts = workingFilter.split( "\\|" );
    Set<String> memberSet = null;

    for ( String part : parts ) {
      Matcher m = pattern.matcher( part );
      if ( m.matches() ) {
        String includeMembersStr = m.group( 1 );
        memberSet = new HashSet<String>( Arrays.asList( includeMembersStr.split( "," ) ) );
      } else {
        appendFilter( strippedFilter, part );
      }
    }
    workingFilter = strippedFilter.toString();
    return memberSet;
  }

  public enum FILES_TYPE_FILTER {
    FILES, FOLDERS, FILES_FOLDERS;

    public static Pattern getRegExPattern() {
      StringBuilder sb = new StringBuilder( "(PLACEHOLDER" );
      for ( FILES_TYPE_FILTER val : FILES_TYPE_FILTER.values() ) {
        sb.append( "|" ).append( val.toString() );
      }
      sb.append( ")" );
      return Pattern.compile( sb.toString() );
    }
  }

  private void setLegacyFilter( String legacyFilter ) {
    this.workingFilter = ( legacyFilter == null || StringUtils.isEmpty( legacyFilter ) ) ? "*" : legacyFilter;
    parseOutFileTypes();
    parseOutIncludeExclude();
    childNodeFilter = workingFilter.isEmpty() ? null : workingFilter;
    workingFilter = null; //garbage collect
  }

  public FILES_TYPE_FILTER getTypes() {
    return types;
  }

  /**
   * Sets whether files, folders, or both are returned: ( FILES | FOLDERS | [default] FILES_FOLDERS )
   */
  public void setTypes( FILES_TYPE_FILTER types ) {
    this.types = types;
  }

  public Set<String> getIncludeMemberSet() {
    return includeMemberSet;
  }

  /**
   * @param includeMemberSet A set of field names to be included in the output.
   */
  public void setIncludeMemberSet( Set<String> includeMemberSet ) {
    this.includeMemberSet = includeMemberSet;
  }

  public Set<String> getExcludeMemberSet() {
    return excludeMemberSet;
  }

  /**
   * @param includeMemberSet A set of field names to be excluded in the output.
   */
  public void setExcludeMemberSet( Set<String> excludeMemberSet ) {
    this.excludeMemberSet = excludeMemberSet;
  }

  public boolean isShowHidden() {
    return showHidden;
  }

  /**
   * @param showHidden
   *      Whether to return information about hidden files.  Default is false.
   */
  public void setShowHidden( boolean showHidden ) {
    this.showHidden = showHidden;
  }

  public Integer getDepth() {
    return depth;
  }

  /**
   * @param depth
   *          0 fetches just file at path; positive integer n fetches node at path plus n levels of children; negative
   *          integer fetches all children. If n > 0 and {@link RepositoryRequest#setTypes(FILES_TYPE_FILTER)} is set to
   *          FILES then only the top level children will be processed.
   */
  public void setDepth( Integer depth ) {
    if ( depth == null ) {
      depth = -1; // search all
    }
    this.depth = depth;
  }

  public String getChildNodeFilter() {
    return childNodeFilter;
  }

  /**
   * @param childNodefilter
   *          filter may be a full name or a partial name with one or more wildcard characters ("*"), or a disjunction
   *          (using the "|" character to represent logical OR) of these; filter does not apply to root node.
   */
  public void setChildNodeFilter( String childNodeFilter ) {
    this.childNodeFilter = childNodeFilter;
  }

  public String getPath() {
    return path;
  }

  /**
   * @param path
   *         Path to file
   */
  public void setPath( String path ) {
    this.path = path;
  }

  public boolean isIncludeAcls() {
    return includeAcls;
  }

  /**
   * 
   * @param includeAcls
   *     Set to true to return ACL permission information with the output.  Default is false.
   */
  public void setIncludeAcls( boolean includeAcls ) {
    this.includeAcls = includeAcls;
  }

}
