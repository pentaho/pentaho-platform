/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
