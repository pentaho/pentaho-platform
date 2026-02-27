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
