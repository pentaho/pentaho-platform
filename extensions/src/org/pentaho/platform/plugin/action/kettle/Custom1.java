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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.kettle;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

public class Custom1 extends KettleComponent {

  private static final long serialVersionUID = -3534575935705861245L;

  @Override 
	  protected boolean customizeTrans( Trans trans, LogWriter logWriter ) {
		  // override this to customize the transformation before it runs
		  // by default there is no transformation
		
		
		  return true;
	  }
	
	@SuppressWarnings("unused")
  private void execSQL( TransMeta transMeta, String targetDatabaseName ) throws KettleStepException, KettleDatabaseException {
		
		// OK, What's the SQL we need to execute to generate the target table?
		String sql = transMeta.getSQLStatementsString();

		// Execute the SQL on the target table:
		Database targetDatabase = new Database(transMeta.findDatabase(targetDatabaseName));
		targetDatabase.connect();
		targetDatabase.execStatements(sql);

	}
	
	public static final TransMeta buildCopyTable(String transformationName,
			String sourceDatabaseName, String sourceTableName,
			String[] sourceFields, String targetDatabaseName,
			String targetTableName, String[] targetFields, LogWriter logWriter)
			throws KettleException {

//		LogWriter log = LogWriter.getInstance();

		try
		{
			// Create a new transformation...
			//

			TransMeta transMeta = new TransMeta();
			transMeta.setName(transformationName);

			// Add the database connections
			/*
			for (int i = 0; i < databasesXML.length; i++) {
				DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
				transMeta.addDatabase(databaseMeta);
			}
*/
			DatabaseMeta sourceDBInfo = transMeta
					.findDatabase(sourceDatabaseName);
			DatabaseMeta targetDBInfo = transMeta
					.findDatabase(targetDatabaseName);

			//
			// create the source step...
			//

			String fromstepname = "read from [" + sourceTableName + "]";
			TableInputMeta tii = new TableInputMeta();
			tii.setDatabaseMeta(sourceDBInfo);
			String selectSQL = "SELECT " + Const.CR; //$NON-NLS-1$

			for (int i = 0; i < sourceFields.length; i++) {
				if (i > 0)
					selectSQL += ", "; //$NON-NLS-1$
				else
					selectSQL += " "; //$NON-NLS-1$
				selectSQL += sourceFields[i] + Const.CR;
			}
			selectSQL += "FROM " + sourceTableName; //$NON-NLS-1$
			tii.setSQL(selectSQL);

			
      PluginRegistry registry = PluginRegistry.getInstance();
			String fromstepid = registry.getPluginId(tii);
			StepMeta fromstep = new StepMeta( fromstepid, fromstepname,
					(StepMetaInterface) tii);
			fromstep.setLocation(150, 100);
			fromstep.setDraw(true);
			fromstep.setDescription("Reads information from table ["
					+ sourceTableName + "] on database [" + sourceDBInfo + "]");
			transMeta.addStep(fromstep);

			//
			// add logic to rename fields
			// Use metadata logic in SelectValues, use SelectValueInfo...
			//

			SelectValuesMeta svi = new SelectValuesMeta();
			svi.allocate(0, 0, sourceFields.length);

			for (int i = 0; i < sourceFields.length; i++) {
				svi.getMeta()[i].setName( sourceFields[i] );
				svi.getMeta()[i].setRename( sourceFields[i] );
			}

			String selstepname = "Rename field names";
			String selstepid = registry.getPluginId(svi);
			StepMeta selstep = new StepMeta(selstepid, selstepname,
					(StepMetaInterface) svi);
			selstep.setLocation(350, 100);
			selstep.setDraw(true);
			selstep.setDescription("Rename field names");
			transMeta.addStep(selstep);

			TransHopMeta shi = new TransHopMeta(fromstep, selstep);
			transMeta.addTransHop(shi);
			fromstep = selstep;

			//
			// Create the target step...
			//

			//
			// Add the TableOutputMeta step...
			//

			String tostepname = "write to [" + targetTableName + "]";
			TableOutputMeta toi = new TableOutputMeta();
			toi.setDatabaseMeta(targetDBInfo);
			toi.setTablename(targetTableName);
			toi.setCommitSize(200);
			toi.setTruncateTable(true);

			String tostepid = registry.getPluginId(toi);
			StepMeta tostep = new StepMeta(tostepid, tostepname,
					(StepMetaInterface) toi);
			tostep.setLocation(550, 100);

			tostep.setDraw(true);
			tostep.setDescription("Write information to table ["
					+ targetTableName + "] on database [" + targetDBInfo + "]");
			transMeta.addStep(tostep);

			//
			// Add a hop between the two steps...
			//

			TransHopMeta hi = new TransHopMeta(fromstep, tostep);
			transMeta.addTransHop(hi);

			// The transformation is complete, return it...
			return transMeta;
		} catch (Exception e) {
			throw new KettleException("An unexpected error occurred creating the new transformation", e);
		}

	}
	
}
