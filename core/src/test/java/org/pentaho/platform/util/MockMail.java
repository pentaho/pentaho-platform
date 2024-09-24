/*!
 *
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
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import java.util.ArrayList;

public class MockMail extends Transport {

  private static ArrayList<Message> MESSAGES = new ArrayList<>();

  public static void clear() {
    MESSAGES = new ArrayList<>();
  }

  public static Message get( final int i ) {
    return MESSAGES.get( i );
  }

  public static int size() {
    return MESSAGES.size();
  }


  public MockMail( Session session, URLName urlname ) {
    super( session, urlname );
  }

  public void connect( String host, int port, String user, String password ) throws MessagingException {
  }

  @Override public void sendMessage( Message message, Address[] addresses ) throws MessagingException {
    MESSAGES.add( message );
  }
}
