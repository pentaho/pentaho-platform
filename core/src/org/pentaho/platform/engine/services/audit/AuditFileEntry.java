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
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mbatchel
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class AuditFileEntry implements IAuditEntry {
  private static final String auditDirPath = "system/logs/audit"; //$NON-NLS-1$

  private static final String auditFileName = PentahoSystem.getSystemSetting(
      "audit/auditLogFile", "/PentahoAuditLog.log" ); //$NON-NLS-1$ //$NON-NLS-2$

  private static File auditFile = null;

  private static String ID_SEPARATOR = PentahoSystem.getSystemSetting( "audit/id_separator", "\t" ); //$NON-NLS-1$ //$NON-NLS-2$

  private static final SimpleDateFormat auditDateFormat = new SimpleDateFormat( PentahoSystem.getSystemSetting(
      "audit/auditDateFormat", "yyyy/MM/dd k:mm:ss" ) ); //$NON-NLS-1$ //$NON-NLS-2$

  public AuditFileEntry() {
    File auditDir = new File( PentahoSystem.getApplicationContext().getFileOutputPath( AuditFileEntry.auditDirPath ) );
    if ( !auditDir.exists() ) {
      auditDir.mkdirs();
    } else if ( !auditDir.isDirectory() ) {
      Logger.error( this, Messages.getInstance().getErrorString(
          "AUDFILEENT.ERROR_0001_AUDIT_PATH_NOT_DIRECTORY", AuditFileEntry.auditDirPath ) ); //$NON-NLS-1$
      return;
    }
    AuditFileEntry.auditFile = new File( auditDir, AuditFileEntry.auditFileName );
    if ( "\\t".equals( AuditFileEntry.ID_SEPARATOR ) ) { //$NON-NLS-1$
      AuditFileEntry.ID_SEPARATOR = "\t"; //$NON-NLS-1$
    }
  }

  public synchronized void auditAll( final String jobId, final String instId, final String objId, final String objType,
      final String actor, final String messageType, final String messageName, final String messageTxtValue,
      final BigDecimal messageNumValue, final double duration ) throws AuditException {

    if ( AuditFileEntry.auditFile == null ) {
      return;
    }
    try {
      BufferedWriter fw = new BufferedWriter( new FileWriter( AuditFileEntry.auditFile, true ) );
      try {
        Date dt = new Date();
        fw.write( AuditFileEntry.auditDateFormat.format( dt ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( jobId ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( instId ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( objId ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( objType ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( actor ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( messageType ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( messageName ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( messageTxtValue ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( messageNumValue ) );
        fw.write( AuditFileEntry.ID_SEPARATOR );
        fw.write( getWritable( duration ) );
        fw.newLine();
      } finally {
        fw.flush();
        fw.close();
      }
    } catch ( IOException ex ) {
      throw new AuditException( ex );
    }
  }

  private String getWritable( final Object obj ) {
    if ( obj instanceof BigDecimal ) {
      DecimalFormat format = new DecimalFormat( "#.###" ); //$NON-NLS-1$
      return format.format( obj );
    } else {
      return ( obj != null ) ? obj.toString() : ""; //$NON-NLS-1$
    }
  }

}
