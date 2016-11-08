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
