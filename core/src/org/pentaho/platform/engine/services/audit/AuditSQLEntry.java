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

package org.pentaho.platform.engine.services.audit;

import org.pentaho.platform.api.engine.AuditException;
import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * @author mbatchel
 * 
 */
public class AuditSQLEntry implements IAuditEntry {
  private static AuditConnection audc;

  /**
   * This ugliness exists because of bug http://jira.pentaho.com/browse/BISERVER-3478. Once this is fixed, we can
   * move this initialization into a one liner for each setting in the class construction.
   * 
   * The logic needs to be that if the config file does not exist, we can fall over to the pentaho.xml file for the
   * attribute value (for backward compatibility).
   */
  private static String INSERT_STMT;
  static {

    String tmp = PentahoSystem.getSystemSetting( "audit_sql.xml", "auditConnection/insertSQL", null ); //$NON-NLS-1$ //$NON-NLS-2$
    INSERT_STMT =
        ( tmp != null ) ? tmp : PentahoSystem.getSystemSetting(
            "auditConnection/insertSQL", Messages.getInstance().getString( "AUDSQLENT.CODE_AUDIT_INSERT_STATEMENT" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  static {
    try {
      AuditSQLEntry.audc = new AuditConnection();
      AuditSQLEntry.audc.initialize();
    } catch ( Exception ex ) {
      Logger.error( AuditHelper.class.getName(), Messages.getInstance().getErrorString(
          "AUDSQLENT.ERROR_0001_INVALID_CONNECTION" ), ex ); //$NON-NLS-1$
    }
  }

  public AuditSQLEntry() {
    retrieveParameters();
  }

  /**
   * This ugliness exists because of bug http://jira.pentaho.com/browse/BISERVER-3478. Once this is fixed, we can
   * move this initialization into a one liner for each setting in the class construction.
   * 
   * The logic needs to be that if the config file does not exist, we can fall over to the pentaho.xml file for the
   * attribute value (for backward compatibility).
   */
  private void retrieveParameters() {

    String tmp = PentahoSystem.getSystemSetting( "audit_sql.xml", "auditConnection/insertSQL", null ); //$NON-NLS-1$ //$NON-NLS-2$
    INSERT_STMT =
        ( tmp != null ) ? tmp : PentahoSystem.getSystemSetting(
            "auditConnection/insertSQL", Messages.getInstance().getString( "AUDSQLENT.CODE_AUDIT_INSERT_STATEMENT" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private void setString( final PreparedStatement stmt, final int num, final String val ) throws SQLException {
    if ( val != null ) {
      stmt.setString( num, val );
    } else {
      stmt.setNull( num, Types.VARCHAR );
    }
  }

  private void setObject( final PreparedStatement stmt, final int num, final String val ) throws SQLException {
    if ( val != null ) {
      stmt.setObject( num, val );
    } else {
      stmt.setNull( num, Types.CLOB );
    }
  }

  private void setBigDec( final PreparedStatement stmt, final int num, final BigDecimal val ) throws SQLException {
    if ( val != null ) {
      stmt.setBigDecimal( num, val );
    } else {
      stmt.setNull( num, Types.DECIMAL );
    }
  }

  /*
   * private void setInteger(PreparedStatement stmt, int num, Integer val) throws SQLException{ if (val != null) {
   * stmt.setInt(num, val.intValue()); } else { stmt.setNull(num, Types.INTEGER); } }
   */
  public void auditAll( final String jobId, final String instId, final String objId, final String objType,
      final String actor, final String messageType, final String messageName, final String messageTxtValue,
      final BigDecimal messageNumValue, final double duration ) throws AuditException {

    Connection con = null;
    try {
      con = AuditSQLEntry.audc.getAuditConnection();
      try {
        PreparedStatement stmt = con.prepareStatement( AuditSQLEntry.INSERT_STMT );
        try {
          setString( stmt, 1, jobId );
          setString( stmt, 2, instId );
          setString( stmt, 3, objId );
          setString( stmt, 4, objType );
          setString( stmt, 5, actor );
          setString( stmt, 6, messageType );
          setString( stmt, 7, messageName );
          setObject( stmt, 8, messageTxtValue );
          setBigDec( stmt, 9, messageNumValue );
          setBigDec( stmt, 10, new BigDecimal( duration ) );
          stmt.setTimestamp( 11, new Timestamp( System.currentTimeMillis() ) );
          stmt.executeUpdate();
        } catch ( SQLException ex ) {
          Logger.error( this.getClass().getName(), ex.getMessage(), ex );
          try {
            con.rollback();
          } catch ( Exception rollbackExc ) {
            throw new AuditException( rollbackExc );
          }
          throw new AuditException( ex );
        } finally {
          stmt.close();
        }
      } finally {
        con.close();
      }
    } catch ( SQLException ex ) {
      throw new AuditException( ex );
    }
  }

}
