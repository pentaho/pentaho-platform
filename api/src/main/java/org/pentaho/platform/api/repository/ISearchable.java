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


package org.pentaho.platform.api.repository;

public interface ISearchable {
  /**
   * SEARCH_TYPE_PHRASE Searches for the exact phrase
   */
  public static final int SEARCH_TYPE_PHRASE = 0;

  /**
   * SEARCH_TYPE_WORDS_OR Searches for each word with each column using an OR connector
   */
  public static final int SEARCH_TYPE_WORDS_OR = 1;

  /**
   * SEARCH_TYPE_WORDS_AND Searches for each word with each column using an AND connector
   */
  public static final int SEARCH_TYPE_WORDS_AND = 2;

  /**
   * @return Returns an array of the char/varchar columns that can be searched.
   */
  public String[] getSearchableColumns();

  /**
   * @return Returns the name of the hibernated object which will be used in the search.
   */
  public String getSearchableTable();

  /**
   * @return Returns the fully-package-qualified name of a named query in the Hibernate Schema that supports a
   *         full-text search of all searchable columns. The parameter name for the search term in the query must
   *         be :searchTerm or the query will fail.
   */
  public String getPhraseSearchQueryName();
}
