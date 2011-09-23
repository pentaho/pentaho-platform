package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class to contain extra attributes that we want to connect to the Mondrian Catalog
 *
 * @author Pedro Alves
 */
public class MondrianCatalogComplementInfo implements Serializable {

  private static final long serialVersionUID = 6753216762906769486L;

  private HashMap<String, WhereConditionBean> whereConditions;

  public MondrianCatalogComplementInfo() {
    this.whereConditions = new HashMap<String, WhereConditionBean>();
  }

  public HashMap<String, WhereConditionBean> getWhereConditions() {
    return whereConditions;
  }

  public void addWhereCondition(final String cube, final String condition) {
    whereConditions.put(cube, new WhereConditionBean(cube, condition));
  }

  public String getWhereCondition(final String cube) {
    if (whereConditions.containsKey(cube)) {
      return whereConditions.get(cube).getCondition();
    }
    return null;
  }

  private class WhereConditionBean implements Serializable {

    private static final long serialVersionUID = -5108507671581605262L;

    private String cube;
    private String condition;

    private WhereConditionBean(final String cube, final String condition) {
      this.cube = cube;
      this.condition = condition;
    }

    public String getCube() {
      return cube;
    }

    public String getCondition() {
      return condition;
    }
  }
}
