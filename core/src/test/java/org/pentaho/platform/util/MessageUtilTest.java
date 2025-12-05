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


package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.util.messages.MessageUtil;

import java.util.ResourceBundle;

public class MessageUtilTest extends TestCase {

  public void testMessageUtil() {

    System.out.println( "Error Message with two arguments  " + MessageUtil.formatErrorMessage( "arg1", "arg2" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    System.out.println( "Message with two arguments  " + MessageUtil.formatMessage( "arg1", "arg2" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    System.out.println( "Message with three arguments  " + MessageUtil.formatMessage( "arg1", "arg2", "arg3" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    System.out.println( "Message with four arguments  " + MessageUtil.formatMessage( "arg1", "arg2", "arg3", "arg4" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    ResourceBundle resourceBundle = null;
    System.out.println( "Error String with one arguments  " + MessageUtil.getErrorString( resourceBundle, "arg1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    System.out
        .println( "Error String with two arguments  " + MessageUtil.getErrorString( resourceBundle, "arg1", "arg2" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    System.out
        .println( "Error String with three arguments  " + MessageUtil.getErrorString( resourceBundle, "arg1", "arg2", "arg3" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    System.out
        .println( "Error String with four arguments  " + MessageUtil.getErrorString( resourceBundle, "arg1", "arg2", "arg3", "arg4" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    System.out
        .println( "Error String with five arguments  " + MessageUtil.getErrorString( resourceBundle, "arg1", "arg2", "arg3", "arg4", "arg5" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    System.out.println( "String with one arguments  " + MessageUtil.getString( resourceBundle, "arg1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    System.out.println( "String with two arguments  " + MessageUtil.getString( resourceBundle, "arg1", "arg2" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    System.out
        .println( "String with three arguments  " + MessageUtil.getString( resourceBundle, "arg1", "arg2", "arg3" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    System.out
        .println( "String with four arguments  " + MessageUtil.getString( resourceBundle, "arg1", "arg2", "arg3", "arg4" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    System.out
        .println( "String with five arguments  " + MessageUtil.getString( resourceBundle, "arg1", "arg2", "arg3", "arg4", "arg5" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    Assert.assertTrue( true );
  }

}
