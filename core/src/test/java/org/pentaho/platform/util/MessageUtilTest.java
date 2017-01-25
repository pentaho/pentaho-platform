/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

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
