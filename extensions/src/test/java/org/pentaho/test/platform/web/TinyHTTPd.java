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

package org.pentaho.test.platform.web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Originally snagged from Mike's Report Design Wizard, this allows me to setup a quick HTTP server for serving up
 * images.
 * 
 * @author mdamour
 * 
 */
public class TinyHTTPd implements Runnable {
  public static String solutionRoot = "./"; //$NON-NLS-1$
  public static boolean die = false;
  Socket c;

  public TinyHTTPd( Socket socket ) {
    c = socket;
  }

  public static void startServer( String solRoot, final int port ) {
    TinyHTTPd.solutionRoot = solRoot;
    die = false;
    Runnable r = new Runnable() {
      public void run() {
        try {
          ServerSocket serverSocket = new ServerSocket( port );
          serverSocket.setSoTimeout( 500 );
          while ( !die ) {
            try {
              Socket client = serverSocket.accept();
              Thread t = new Thread( new TinyHTTPd( client ) );
              t.start();
            } catch ( Exception e ) {
              //ignored
            }
          }
        } catch ( Exception e ) {
          // e.printStackTrace();
        }
      }
    };
    Thread t = new Thread( r );
    t.setDaemon( true );
    t.start();
  }

  public static void stopServer() {
    die = true;
  }

  public static void main( String[] args ) {
    startServer( "./resources/solutions", 6736 ); //$NON-NLS-1$
  }

  public void run() {
    try {
      BufferedReader i = new BufferedReader( new InputStreamReader( c.getInputStream() ) );
      DataOutputStream o = new DataOutputStream( c.getOutputStream() );
      try {
        while ( true ) {
          String s = i.readLine();
          if ( s.length() < 1 ) {
            break;
          }
          if ( s.startsWith( "GET" ) ) { //$NON-NLS-1$
            StringTokenizer t = new StringTokenizer( s, " " ); //$NON-NLS-1$
            t.nextToken();
            String p = t.nextToken();
            p = solutionRoot + "/system/tmp/" + p.substring( p.indexOf( "=" ) + 1 ); //$NON-NLS-1$ //$NON-NLS-2$
            File file = new File( p );
            int l = (int) new File( p ).length();
            byte[] b = new byte[l];
            FileInputStream f = new FileInputStream( file );
            f.read( b );
            o.writeBytes( "HTTP/1.0 200 OK\nContent-Length:" + l + "\n\n" ); //$NON-NLS-1$ //$NON-NLS-2$
            o.write( b, 0, l );
          }
          if ( s.startsWith( "POST" ) ) { //$NON-NLS-1$
            StringTokenizer t = new StringTokenizer( s, " " ); //$NON-NLS-1$
            t.nextToken();
            String p = t.nextToken();
            p = solutionRoot + "/system/tmp/" + p.substring( p.indexOf( "=" ) + 1 ); //$NON-NLS-1$ //$NON-NLS-2$
            File file = new File( p );
            int l = (int) new File( p ).length();
            byte[] b = new byte[l];
            FileInputStream f = new FileInputStream( file );
            f.read( b );
            o.writeBytes( "HTTP/1.0 200 OK\nContent-Length:" + l + "\n\n" ); //$NON-NLS-1$ //$NON-NLS-2$
            o.write( b, 0, l );
          }

        }
      } catch ( Exception e ) {
        o.writeBytes( "HTTP/1.0 404 ERROR\n\n\n" ); //$NON-NLS-1$
      }
      o.close();
    } catch ( Exception e ) {
      //ignored
    }
  }
}
