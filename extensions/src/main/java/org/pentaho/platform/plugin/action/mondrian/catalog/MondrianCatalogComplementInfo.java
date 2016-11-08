/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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

  public void addWhereCondition( final String cube, final String condition ) {
    whereConditions.put( cube, new WhereConditionBean( cube, condition ) );
  }

  public String getWhereCondition( final String cube ) {
    if ( whereConditions.containsKey( cube ) ) {
      return whereConditions.get( cube ).getCondition();
    }
    return null;
  }

  private class WhereConditionBean implements Serializable {

    private static final long serialVersionUID = -5108507671581605262L;

    private String cube;
    private String condition;

    private WhereConditionBean( final String cube, final String condition ) {
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
