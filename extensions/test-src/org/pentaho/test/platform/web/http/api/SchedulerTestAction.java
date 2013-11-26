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

package org.pentaho.test.platform.web.http.api;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IStreamProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;

import java.io.InputStream;
import java.io.OutputStream;

public class SchedulerTestAction implements IAction, IStreamingAction, IStreamProcessingAction {

  private InputStream inputStream;
  private OutputStream outputStream;

  public void execute() throws Exception {
    IOUtils.copy( inputStream, outputStream );
    outputStream.close();
  }

  public String getMimeType( String reportOutput ) {
    return null;
  }

  public void setInputStream( InputStream inputStream ) {
    this.inputStream = inputStream;
  }

  public void setOutputStream( OutputStream outputStream ) {
    this.outputStream = outputStream;
  }

}
