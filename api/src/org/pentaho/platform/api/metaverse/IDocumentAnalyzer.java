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

package org.pentaho.platform.api.metaverse;

import java.util.Set;

/**
 * The IDocumentAnalyzer interface represents an object capable of analyzing certain types of
 * IMetaverseDocuments.
 * @author mburgess
 *
 */
public interface IDocumentAnalyzer {
  
  /**
   * Analyze the given document
   *
   * @param document the document
   */
  void analyze(IMetaverseDocument document);
  
  /**
   * Gets the types of documents supported by this analyzer
   *
   * @return the supported types
   */
  Set<String> getSupportedTypes();
  
  /**
   * Sets the metaverse builder, used by the analyzer to create nodes and links in the metaverse generated
   * by analysis of documents.
   *
   * @param builder the metaverse builder
   */
  void setMetaverseBuilder(IMetaverseBuilder builder);

}
