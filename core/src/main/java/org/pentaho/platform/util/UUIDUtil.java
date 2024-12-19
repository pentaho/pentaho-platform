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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.messages.Messages;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import java.net.NetworkInterface;
import java.util.Enumeration;

public class UUIDUtil {
  private static final Log log = LogFactory.getLog( UUIDUtil.class );

  static boolean nativeInitialized = false;

  static UUIDGenerator ug;

  static org.safehaus.uuid.EthernetAddress eAddr;

  static {
    UUIDUtil.ug = UUIDGenerator.getInstance();
    /*
     * Add support for running in clustered environments. In this way, the MAC address of the running server can be
     * added to the environment with a -DMAC_ADDRESS=00:50:56:C0:00:01
     */
    if ( UUIDUtil.eAddr == null ) {
      String macAddr = System.getProperty( "MAC_ADDRESS" ); //$NON-NLS-1$
      if ( macAddr != null ) {
        // On Windows machines, people would be inclined to get the MAC
        // address with ipconfig /all. The format of this would be
        // something like 00-50-56-C0-00-08. So, replace '-' with ':' before
        // creating the address.
        //
        macAddr = macAddr.replace( '-', ':' );
        UUIDUtil.eAddr = new org.safehaus.uuid.EthernetAddress( macAddr );
      }
    }

    // No explicit override - let's try the new Java 6 methods...
    if ( UUIDUtil.eAddr == null ) {
      try {
        // Use reflection to check for getHardwardAddress with no parameters...
        NetworkInterface.class.getMethod( "getHardwareAddress", new Class[] {} ); //$NON-NLS-1$
        // Yep - running Java6 or later - keep going.
        String macAddr = getActiveNetworkMacAddress();
        if ( macAddr != null ) {
          // Would be here if machine is completely disconnected from all networks - there
          // is no active network adapter.
          UUIDUtil.eAddr = new org.safehaus.uuid.EthernetAddress( macAddr );
        }
      } catch ( Exception ignored ) {
        // Not running on Java 6 - interesting...
      }
    }

    // Hmmm - not running in Java6 - use the Dummy one.
    if ( UUIDUtil.eAddr == null ) {
      // Still don't have an Ethernet Address - generate a dummy one.
      UUIDUtil.eAddr = UUIDUtil.ug.getDummyAddress();
    }

    // Generate a UUID to make sure everything is running OK.
    UUID olduuId = UUIDUtil.ug.generateTimeBasedUUID( UUIDUtil.eAddr );
    if ( olduuId == null ) {
      UUIDUtil.log.error( Messages.getInstance().getErrorString( "UUIDUtil.ERROR_0003_GENERATEFAILED" ) ); //$NON-NLS-1$
    }

  }

  public static String getUUIDAsString() {
    return UUIDUtil.getUUID().toString();
  }

  public static UUID getUUID() {
    UUID uuId = UUIDUtil.ug.generateTimeBasedUUID( UUIDUtil.eAddr );
    // while (uuId.toString().equals(olduuId.toString())) {
    // uuId = ug.generateTimeBasedUUID(eAddr);
    // }
    // olduuId = uuId;
    return uuId;
  }

  private static String getInterfaceInfo( NetworkInterface nif ) throws Exception {
    return getInterfaceInfo( nif, ":" );
  }

  private static String getInterfaceInfo( NetworkInterface nif, String sep ) throws Exception {
    byte[] addrBytes = nif.getHardwareAddress(); // get the MAC address
    StringBuffer buff = new StringBuffer();
    for ( int i = 0; i < addrBytes.length; i++ ) {
      buff.append( String.format( "%02X%s", addrBytes[i], ( i < addrBytes.length - 1 ) ? sep : "" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return buff.toString();
  }

  public static String getActiveNetworkMacAddress() throws Exception {
    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while ( interfaces.hasMoreElements() ) {
      NetworkInterface nif = interfaces.nextElement();
      // Obvious what the next IF is doing, but it must be connected, non-PPP, non-loopback, and non-virtual
      if ( ( nif.isUp() ) && ( !nif.isPointToPoint() ) && ( !nif.isLoopback() ) && ( !nif.isVirtual() ) ) {
        return getInterfaceInfo( nif );
      }
    }
    return null;
  }

}
