package org.pentaho.test.platform.plugin;

import org.pentaho.metadata.query.impl.sql.SqlGenerator;


public class TestPostSqlGenerator extends SqlGenerator {


  @Override
  protected String processGeneratedSql(String sql) {
    
    System.out.println("processGeneratedSql was called...");
    sql = sql.replaceAll("customername", "contactfirstname");
    sql = sql.replaceAll("CUSTOMERNAME", "CONTACTFIRSTNAME");
    
    return super.processGeneratedSql(sql);
  }

}
