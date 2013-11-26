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

package org.pentaho.platform.api.action;

import java.io.OutputStream;

/**
 * The interface for Actions that want to stream content to the caller. A "streaming" output is a special type of
 * action definition output that will result in an {@link OutputStream} being set on the Action.
 * 
 * It is generally recommended that if your Action bean yields any content for user consumption, that it implement
 * {@link IStreamingAction}. This guarantees that your Action will have the option of:
 * <ul>
 * <li>streaming the generated content back to the user, such as an HTTP servlet response
 * <li>saving the content to a content repository
 * <li>communicating with existing BI Platform components (such as EmailComponent, which expects to find
 * attachments in the content repository)
 * </ul>
 * <p>
 * From the perspective of the Action Sequence itself, an output is considered a "streaming" output if the
 * attribute "type" is set to "content", e.g. <code>
 * <myContentOutput type="content"/>
 * </code> The output may or may not have a globally defined destination to which it corresponds. If the output
 * does correspond to a globally defined output with a destination, then the source of the {@link OutputStream}
 * will be determined by the {@link org.pentaho.platform.api.engine.IOutputHandler} provided during the execution
 * of the Action Sequence by the {@link org.pentaho.platform.api.engine.ISolutionEngine}.
 * <p>
 * In the case that an output is considered "streaming", it will basically be treated similar to an input in that
 * it will be set on the Action with a setter method. For example, if an action definition declares a streaming
 * output called "reportContent", then the platform will attempt to call a method on the Action called
 * "setReportContentStream(OutputStream os)". Note that the post-fix "Stream" will be added to the name of your
 * output. Streaming outputs will not be queried once the Action has finished executing like a normal non-streaming
 * output would.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface IStreamingAction extends IAction {

  /**
   * Requests the mimetype of the content that the Action will write to the provided stream name. To use the
   * example in the above javadoc, this streamPropertyName would be "reportContent".
   * 
   * @param streamPropertyName
   *          the action definition output name representing the streamed output, e.g. "reportContent"
   * @return the mimeType for the stream indicated by streamPropertyName
   */
  public String getMimeType( String streamPropertyName );

  public void setOutputStream( OutputStream outputStream );

}
