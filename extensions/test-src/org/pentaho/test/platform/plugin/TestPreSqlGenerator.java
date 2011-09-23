package org.pentaho.test.platform.plugin;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.query.impl.sql.SqlGenerator;
import org.pentaho.metadata.query.model.Selection;
import org.pentaho.pms.mql.dialect.SQLQueryModel;


public class TestPreSqlGenerator extends SqlGenerator {

  @Override
  protected void preprocessQueryModel(SQLQueryModel query, List<Selection> selections,
      Map<LogicalTable, String> tableAliases, DatabaseMeta databaseMeta) {
    
    System.out.println("preprocessQueryModel was called...");
    query.addWhereFormula("LT.CUSTOMERNAME like 'Au%'", "AND");
    
    super.preprocessQueryModel(query, selections, tableAliases, databaseMeta);
  }


}
