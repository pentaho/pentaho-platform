/*
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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.osgi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.settings.ServerPortService;

public class KarafInstancePortFactory {
  final static String delimiter = ",";
  protected static KarafInstance karafInstance;
  private String importFilePath;
  int lineNumber;

  public KarafInstancePortFactory( String importFilePath ) {
    this.importFilePath = importFilePath;
    if ( karafInstance == null ) {
      karafInstance = KarafInstance.getInstance();
    }
  }

  @SuppressWarnings( "resource" )
  public void process() throws FileNotFoundException, IOException {

    BufferedReader br = null;
    String line = "";
    String[] fields;
    lineNumber = 0;

    try {

      br = new BufferedReader( new FileReader( importFilePath ) );
      while ( ( line = br.readLine() ) != null ) {
        try {
          lineNumber++;
          if ( !line.startsWith( "#" ) && line.trim().length() > 1 ) {
            fields = parseLine( line );
            switch ( fields[0] ) {
              case "port":
                if ( fields.length < 6 ){
                  throw new IllegalStateException( getErrorPrefix() + "Port definition lines must contain 6 fields" );
                }
                karafInstance.registerPort( new KarafInstancePort( fields[1], fields[2], fields[3], Integer
                    .valueOf( fields[4] ), Integer.valueOf( fields[5] ), fields[6] ) );
                break;
              case "service":
                if ( fields.length < 2 ){
                  throw new IllegalStateException( getErrorPrefix() + "Port definition lines must contain 2 fields" );
                }
                ServerPortService service = new ServerPortService( fields[1], fields[2]);
                karafInstance.registerService( service );
                break;
              default:
                throw new IllegalStateException( getErrorPrefix() + "First field of line did not contain a valid record type");
            }
          }
        } catch ( Exception e ) {
          throw new IllegalStateException( getErrorPrefix() );
        }

      }

    } catch ( FileNotFoundException e ) {
      throw new FileNotFoundException( "File " + importFilePath + " does not exist.  Could not determine karaf port assignment" );
    } catch ( IOException e ) {
      throw new IOException( "File " + importFilePath + " could not be read.  Could not determine karaf port assignment" );
    } finally {
      IOUtils.closeQuietly( br );
    }
  }

  protected String[] parseLine( String line ) {
    String[] fields = line.split( delimiter );
    // Search for quoted delimeter and collapse the array if found
    for ( int i = 0; i < fields.length; i++ ) {
      if ( fields[i] == null ) {
        break;
      }
      if ( fields[i].startsWith( "\"" ) ) {
        while ( !StringUtils.right( fields[i].trim(), 1 ).equals( "\"" ) && i < fields.length - 2 ) {
          fields[i] = fields[i] + "," + fields[i + 1];
          for ( int j = i + 1; j < fields.length - 1; j++ ) {
            fields[j] = fields[j + 1];
            fields[j + 1] = null;
          }
        }
      }
      fields[i] = fields[i].trim();
      if ( fields[i].startsWith( "\"" ) && fields[i].endsWith( "\"" ) ) {
        fields[i] = fields[i].substring( 1, fields[i].length() - 1 );
      }
    }
    return fields;
  }

  private String getErrorPrefix() {
    return "Badly formated file: " + importFilePath + " line: " + lineNumber;
  }
}
