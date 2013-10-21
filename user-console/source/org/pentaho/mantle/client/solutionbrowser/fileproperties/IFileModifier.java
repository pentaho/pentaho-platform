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

package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.xml.client.Document;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;

import java.util.List;

/**
 * Interface for sub panels of the FilePropertiesDialog which provides methods for passing
 */
public interface IFileModifier {
  public void init( RepositoryFile fileSummary, Document fileInfo );

  public void apply();

  /**
   * Use this method to create RequestBuilder objects and use RequestBuilder.setRequestData Add RequestBuilder
   * objects to the List which is then used by FilePropertiesDialog to iterate through and call each request
   * sequentially by chaining them in the callbacks. It is not necessary to set a callback since
   * FilePropertiesDialog will add its own
   * 
   * @see FilePropertiesDialog#applyPanel()
   * @return
   */
  public List<RequestBuilder> prepareRequests();
}
