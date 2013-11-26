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

package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.repository.IContentItem;

import java.io.IOException;

/**
 * An OutputHandler manages the content generated from a Component execution. Output can take the form of the
 * generated results from a component, or content that solicits additional information from the requester. The
 * handler also manages the relationship with the ActionDefinition and output content validation.
 */
public interface IOutputHandler {
  // TODO sbarkdull, convert these 3 OUTPUT_* to enumerated type
  public static final int OUTPUT_TYPE_PARAMETERS = 1;

  public static final int OUTPUT_TYPE_CONTENT = 2;

  public static final int OUTPUT_TYPE_DEFAULT = 3;

  public static final String RESPONSE = "response"; //$NON-NLS-1$

  public static final String CONTENT = "content"; //$NON-NLS-1$

  public static final String FILE = "file"; //$NON-NLS-1$

  public void setSession( IPentahoSession session );

  public IPentahoSession getSession();

  /**
   * @deprecated This method could never tell you if the content was actually done. Use
   *             {@link #isResponseExpected()} if you need information about a handlers likelihood to generate a
   *             response.
   */
  @Deprecated
  public boolean contentDone();

  /**
   * Indicates whether or not the handler is expected to have data written to a response output stream managed by
   * the handler. Typically, a handler will want to return true here if its getOutputContentItem or setOutput
   * methods have been invoked and their invocations can result in a write to the response output stream that is
   * managed by the handler. In general, handlers are responsible for setting this flag any time a client response
   * is possible.
   * 
   * @return true if the handler gave something the opportunity to write data to the its response output stream
   */
  public boolean isResponseExpected();

  /**
   * Retrieve the ContentItem that describes the request interface for additional or missing information (missing
   * from the original request)
   * 
   * @return ContentItem describing user feedback
   */
  public IContentItem getFeedbackContentItem();

  /**
   * Retrieve the ContentItem that describes the output from this request's component execution.
   * 
   * @return ContentItem describing end result output
   */
  // public IContentItem getOutputContentItem();
  /**
   * Retrieve the ContentItem that describes the output from this request's component execution.
   * 
   * @param objectName
   *          Name of the object
   * @param contentName
   *          Name of the content
   * @return ContentItem describing end result output
   */
  public IContentItem getOutputContentItem( String objectName, String contentName, String instanceId, String mimeType );

  /**
   * Determines whether this output handler can send feedback ContentItems or not.
   * <p>
   * Generally, if there is no client on the other side of the request that could receive and process feedback,
   * then this boolean should be setto false.
   * 
   * @return true if feedback is allowed, false otherwise
   */
  public boolean allowFeedback();

  /**
   * Sets the output type that is wanted by the handler. Valid values are OUTPUT_TYPE_PARAMETERS,
   * OUTPUT_TYPE_CONTENT, OUTPUT_TYPE_DEFAULT
   * 
   * @param outputType
   *          Output type requested
   */
  public void setOutputPreference( int outputType );

  /**
   * Gets the output type prefered by the handler. Values are defined in
   * org.pentaho.platform.api.engine.IOutputHandler and are OUTPUT_TYPE_PARAMETERS, OUTPUT_TYPE_CONTENT, or
   * OUTPUT_TYPE_DEFAULT
   * 
   * @return Output type
   */
  public int getOutputPreference();

  /**
   * Sets an output of the handler. For example the HTTP handler will accept output names of 'header' allowing an
   * HTTP header to be set, and 'redirect' allowing the responses sendRedirect to be called.
   * 
   * @param name
   *          Name of the output
   * @param value
   *          Value of the output
   */
  public void setOutput( String name, Object value ) throws IOException;

  public IMimeTypeListener getMimeTypeListener();

  public void setMimeTypeListener( IMimeTypeListener mimeTypeListener );
}
