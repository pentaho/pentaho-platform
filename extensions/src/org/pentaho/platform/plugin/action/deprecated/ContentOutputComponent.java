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

package org.pentaho.platform.plugin.action.deprecated;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This component takes an input, and writes the output into the current output handler. Future extensions to this
 * component will allow writing the input variable to a file within the solution.
 * 
 * <p>
 * <b>Inputs</b>
 * <table>
 * <tr>
 * <th align=left>Input Name</th>
 * <th>&nbsp</th>
 * <th align=left>Description</th>
 * </tr>
 * <tr>
 * <td>CONTENTOUTPUT</td>
 * <td />
 * <td>The name of the input that contains the content to output</td>
 * </tr>
 * <tr>
 * <td>mime-type</td>
 * <td />
 * <td>The mime type to output.</td>
 * </tr>
 * </table>
 * <p>
 * <b>Outputs</b>
 * <p>
 * None
 * 
 * @author mbatchel
 */
public class ContentOutputComponent extends ComponentBase {

  private static final long serialVersionUID = -6300339081029611956L;

  private static final String INPUT_NAME_EXPECTED = "CONTENTOUTPUT"; //$NON-NLS-1$

  private static final String OUTPUT_NAME = "content"; //$NON-NLS-1$

  private static final String COMPONENT_SETTING_MIME_TYPE = "mime-type"; //$NON-NLS-1$

  /**
   * Validates that the input called CONTENTOUTPUT has been provided, and that the Mime type of the input is also set.
   */
  @Override
  protected boolean validateAction() {
    if ( !isDefinedInput( ContentOutputComponent.INPUT_NAME_EXPECTED ) ) {
      error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0001_CONTENTOUTPUT_NOT_DEFINED" ) ); //$NON-NLS-1$
      return false;
    }
    if ( !isDefinedInput( ContentOutputComponent.COMPONENT_SETTING_MIME_TYPE ) ) {
      error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0006_MIME_TYPE_REQUIRED" ) ); //$NON-NLS-1$
      return false;
    }
    if ( !isDefinedOutput( ContentOutputComponent.OUTPUT_NAME ) ) {
      error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0008_CONTENT_OUTPUT_REQUIRED" ) ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    // No system settings to validate
    return true;
  }

  @Override
  public void done() {
    // No cleanup necessary
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( ContentOutputComponent.class );
  }

  @Override
  protected boolean executeAction() throws Throwable {
    String mimeType = getInputStringValue( ContentOutputComponent.COMPONENT_SETTING_MIME_TYPE );
    Object dataToOutput = getInputValue( ContentOutputComponent.INPUT_NAME_EXPECTED );

    if ( dataToOutput != null ) {
      IContentItem outputContentItem = getOutputContentItem( ContentOutputComponent.OUTPUT_NAME, mimeType );
      if ( outputContentItem != null ) {
        outputContentItem.setMimeType( mimeType );
        OutputStream outputStream = null;
        if ( dataToOutput instanceof String ) {
          String theOutput = (String) dataToOutput;
          if ( theOutput.length() > 0 ) {
            try {
              outputStream = outputContentItem.getOutputStream( getActionName() );
              outputStream.write( theOutput.getBytes( LocaleHelper.getSystemEncoding() ) );
              outputStream.flush();
              outputStream.close();
              outputContentItem.closeOutputStream();
            } catch ( Exception e ) {
              error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0003_WRITING_OUTPUT" ), e ); //$NON-NLS-1$
              return false;
            }
            trace( theOutput );
            return true;
          } else {
            error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0002_EMPTY_OUTPUT" ) ); //$NON-NLS-1$
            return false;
          }
        } else if ( dataToOutput instanceof InputStream ) {
          InputStream is = (InputStream) dataToOutput;
          byte[] buff = new byte[1024];
          int len;
          outputStream = outputContentItem.getOutputStream( null );
          while ( ( len = is.read( buff ) ) >= 0 ) {
            outputStream.write( buff, 0, len );
          }
          outputStream.flush();
          outputStream.close();
          outputContentItem.closeOutputStream();
          return true;
        } else if ( dataToOutput instanceof ByteArrayOutputStream ) {
          ByteArrayOutputStream baos = (ByteArrayOutputStream) dataToOutput;
          outputStream = outputContentItem.getOutputStream( null );
          outputStream.write( baos.toByteArray() );
          outputStream.flush();
          outputStream.close();
          outputContentItem.closeOutputStream();
          return true;
        } else {
          error( Messages.getInstance().getErrorString(
              "ContentOutputComponent.ERROR_0007_UNKNOWN_TYPE", dataToOutput.getClass().getName() ) ); //$NON-NLS-1$
          return false;
        }

      } else {
        error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0005_OUTPUT_CONTENT_ITEM" ) ); //$NON-NLS-1$
        return false;
      }
    }
    error( Messages.getInstance().getErrorString( "ContentOutputComponent.ERROR_0004_CONTENTOUTPUT_NULL" ) ); //$NON-NLS-1$
    return false;
  }

  // Nothing to do here.
  @Override
  public boolean init() {
    return true;
  }

}
