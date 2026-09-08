/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.platform.repository2.unified;

import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.util.regex.Pattern;

/**
 * Parses the <i>structured</i> form of the legacy child node filter, which allows a caller to filter files and folders
 * independently instead of applying a single node name filter to both:
 *
 * <pre>
 *   {@value #FILE_FILTER_TOKEN}&lt;patterns&gt;[{@value #FILTER_TOKEN_SEPARATOR}{@value #FOLDER_FILTER_TOKEN}&lt;patterns&gt;]
 *   {@value #FOLDER_FILTER_TOKEN}&lt;patterns&gt;[{@value #FILTER_TOKEN_SEPARATOR}{@value #FILE_FILTER_TOKEN}&lt;patterns&gt;]
 * </pre>
 * <p>
 * where <code>patterns</code> is a comma separated list of node names, each one accepting the
 * <code>{@value RepositoryRequest#FILTER_WILDCARD}</code> wildcard.
 * The pipe ( {@value RepositoryRequest#FILTER_SEPARATOR} ) character cannot be used because it is the separator of the
 * legacy filter itself. Examples of the complete <code>filter</code> parameter:
 *
 * <pre>
 *   {@value #FILE_FILTER_TOKEN}*.ktr{@value #INSIDE_FILTER_TOKEN_SEPARATOR}*.kjb{@value
 *   RepositoryRequest#FILTER_SEPARATOR}FILES_FOLDERS
 *                                              the whole folder structure plus the ktr and kjb files
 *   {@value #FOLDER_FILTER_TOKEN}sales*{@value RepositoryRequest#FILTER_SEPARATOR}FILES_FOLDERS
 *                                              only folders named sales*, empty ones included, plus the files they
 *                                              carry
 *   {@value #FILE_FILTER_TOKEN}*.prpt{@value #FILTER_TOKEN_SEPARATOR}{@value #FOLDER_FILTER_TOKEN}2026*
 *                                              both filters combined
 *   *.ktr{@value RepositoryRequest#FILTER_SEPARATOR}FILES        legacy filter, untouched
 * </pre>
 * <p>
 * Each filter narrows its own kind of node and nothing else: the folder filter alone decides which folders the
 * result carries, empty ones included, and the file filter alone decides which files it carries. A file whose folder
 * the folder filter rejected goes with it, for the result carries no folder to hold it, and the folders between a
 * matching folder and the requested path belong to the result so that it stays reachable.
 * <p>
 * A filter that carries none of the tokens is a plain legacy glob and {@link #isStructured()} returns
 * <code>false</code>,
 * meaning the caller must keep using the regular tree traversal.
 */
public class TreeNodeFilterSpec {
  public static final String FILE_FILTER_TOKEN = "fileFilter=";
  public static final String FOLDER_FILTER_TOKEN = "folderFilter=";

  /**
   * separates the {@link #FILE_FILTER_TOKEN} clause from the {@link #FOLDER_FILTER_TOKEN} one
   */
  public static final String FILTER_TOKEN_SEPARATOR = ";";

  /**
   * separates the individual node name patterns inside a single clause
   */
  public static final String INSIDE_FILTER_TOKEN_SEPARATOR = ",";

  /**
   * the filter a request degrades to when the provider cannot honor the structured form: "everything"
   */
  public static final String DEFAULT_CHILD_NODE_FILTER = RepositoryRequest.FILTER_WILDCARD;

  /**
   * characters that are never valid inside a node name filter, including the legacy filter separator, which is
   * quoted because it is injected into a regular expression
   */
  private static final Pattern ILLEGAL_CHARS =
    Pattern.compile( ".*[/\\\\\\[\\]'\"" + Pattern.quote( RepositoryRequest.FILTER_SEPARATOR ) + "\t\r\n"
      + "]+.*" );

  private static final TreeNodeFilterSpec LEGACY = new TreeNodeFilterSpec( null, null );

  private final String fileFilter;
  private final String folderFilter;

  private TreeNodeFilterSpec( final String fileFilter, final String folderFilter ) {
    this.fileFilter = fileFilter;
    this.folderFilter = folderFilter;
  }

  /**
   * Parses the given child node filter.
   *
   * @param childNodeFilter the value of
   *                        {@link org.pentaho.platform.api.repository2.unified.RepositoryRequest#getChildNodeFilter()}
   * @return the parsed specification; never <code>null</code>
   * @throws IllegalArgumentException when the structured syntax is malformed
   */
  public static TreeNodeFilterSpec parse( final String childNodeFilter ) {
    if ( !hasStructuredTokens( childNodeFilter ) ) {
      // plain legacy glob; nothing to do here
      return LEGACY;
    }

    String fileFilter = null;
    String folderFilter = null;

    for ( String clause : childNodeFilter.split( FILTER_TOKEN_SEPARATOR ) ) {
      String trimmed = clause.trim();

      if ( trimmed.isEmpty() ) {
        continue;
      }

      if ( trimmed.startsWith( FILE_FILTER_TOKEN ) ) {
        fileFilter = validate( trimmed.substring( FILE_FILTER_TOKEN.length() ), FILE_FILTER_TOKEN, childNodeFilter );
      } else if ( trimmed.startsWith( FOLDER_FILTER_TOKEN ) ) {
        folderFilter =
          validate( trimmed.substring( FOLDER_FILTER_TOKEN.length() ), FOLDER_FILTER_TOKEN, childNodeFilter );
      } else {
        throw new IllegalArgumentException(
          "Unknown clause '" + trimmed + "' in filter '" + childNodeFilter + "'. Expected '" + FILE_FILTER_TOKEN
            + "' or '" + FOLDER_FILTER_TOKEN + "'" );
      }
    }

    return new TreeNodeFilterSpec( fileFilter, folderFilter );
  }

  private static String validate( final String value, final String token, final String childNodeFilter ) {
    StringBuilder normalized = new StringBuilder();

    for ( String pattern : value.split( INSIDE_FILTER_TOKEN_SEPARATOR, -1 ) ) {
      String trimmed = pattern.trim();

      if ( trimmed.isEmpty() || ILLEGAL_CHARS.matcher( trimmed ).matches() ) {
        throw new IllegalArgumentException( "Invalid '" + token + "' value in filter '" + childNodeFilter + "'" );
      }

      if ( !normalized.isEmpty() ) {
        normalized.append( INSIDE_FILTER_TOKEN_SEPARATOR );
      }

      normalized.append( trimmed );
    }

    if ( normalized.isEmpty() ) {
      throw new IllegalArgumentException( "Empty '" + token + "' value in filter '" + childNodeFilter + "'" );
    }

    return normalized.toString();
  }

  /**
   * Cheap syntax detection, without validating the clauses.
   *
   * @param childNodeFilter the child node filter to inspect
   * @return <code>true</code> when the filter carries at least one structured token
   */
  public static boolean hasStructuredTokens( final String childNodeFilter ) {
    return childNodeFilter != null
      && ( childNodeFilter.contains( FILE_FILTER_TOKEN ) || childNodeFilter.contains( FOLDER_FILTER_TOKEN ) );
  }

  /**
   * Degrades a structured child node filter to {@link #DEFAULT_CHILD_NODE_FILTER}, for call paths that cannot honor
   * it ( e.g. a children request, where no provider applies the file/folder distinction ). A plain legacy filter is
   * left untouched.
   *
   * @param repositoryRequest the request about to be handed to the provider
   * @return <code>true</code> when the filter was degraded
   */
  public static boolean applyFallback( final RepositoryRequest repositoryRequest ) {
    if ( repositoryRequest == null || !hasStructuredTokens( repositoryRequest.getChildNodeFilter() ) ) {
      return false;
    }

    repositoryRequest.setChildNodeFilter( DEFAULT_CHILD_NODE_FILTER );

    return true;
  }

  /**
   * @return <code>true</code> when the caller asked for independent file and folder filtering, which requires the
   * query based tree implementation.
   */
  public boolean isStructured() {
    return fileFilter != null || folderFilter != null;
  }

  /**
   * @return comma separated file name patterns, or <code>null</code> meaning "every file"
   */
  public String getFileFilter() {
    return fileFilter;
  }

  /**
   * @return comma separated folder name patterns, or <code>null</code> meaning "every folder", empty ones included
   */
  public String getFolderFilter() {
    return folderFilter;
  }
}
