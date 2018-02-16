/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JobParamsAdapter extends XmlAdapter<JobParams, Map<String, Serializable>> {

  private static final String VARIABLES = "variables";
  private static final String PARAMETERS = "parameters";

  public JobParams marshal( Map<String, Serializable> v ) throws Exception {
    Object variables = v.get( VARIABLES );
    Object parameters = v.get( PARAMETERS );
    if ( parameters != null && parameters instanceof Map
            && variables != null && variables instanceof Map ) {
      Map<String, String> paramMap = (Map) parameters;
      Map<String, String> variableMap = (Map) variables;
      if ( !paramMap.isEmpty() && !variableMap.isEmpty() ) {
        for ( Map.Entry<String, String> paramEntry : paramMap.entrySet() ) {
          if ( variableMap.containsKey( paramEntry.getKey() ) && paramEntry.getValue() != null ) {
            variableMap.remove( paramEntry.getKey() );
          }
        }
      }
    }

    ArrayList<JobParam> params = new ArrayList<JobParam>();
    for ( Map.Entry<String, Serializable> entry : v.entrySet() ) {
      if ( entry != null && entry.getKey() != null && entry.getValue() != null ) {
        if ( entry.getValue() instanceof Collection ) {
          for ( Object iValue : (Collection<?>) entry.getValue() ) {
            if ( iValue != null ) {
              JobParam jobParam = new JobParam();
              jobParam.name = entry.getKey();
              jobParam.value = iValue.toString();
              params.add( jobParam );
            }
          }
        } else if ( entry.getValue() instanceof Map ) {
          ( (Map<String, Serializable>) entry.getValue() ).forEach( ( key, value ) -> {
            if ( value != null ) {
              JobParam jobParam = new JobParam();
              jobParam.name = key;
              jobParam.value = value.toString();
              params.add( jobParam );
            }
          } );
        } else {
          JobParam jobParam = new JobParam();
          jobParam.name = entry.getKey();
          jobParam.value = entry.getValue().toString();
          params.add( jobParam );
        }
      }
    }
    JobParams jobParams = new JobParams();
    jobParams.jobParams = params.toArray( new JobParam[0] );
    return jobParams;
  }

  public Map<String, Serializable> unmarshal( JobParams v ) throws Exception {
    HashMap<String, ArrayList<Serializable>> draftParamMap = new HashMap<String, ArrayList<Serializable>>();
    for ( JobParam jobParam : v.jobParams ) {
      ArrayList<Serializable> p = draftParamMap.get( jobParam.name );
      if ( p == null ) {
        p = new ArrayList<Serializable>();
        draftParamMap.put( jobParam.name, p );
      }
      p.add( jobParam.value );
    }
    HashMap<String, Serializable> paramMap = new HashMap<String, Serializable>();
    for ( String paramName : draftParamMap.keySet() ) {
      ArrayList<Serializable> p = draftParamMap.get( paramName );
      if ( p.size() == 1 ) {
        paramMap.put( paramName, p.get( 0 ) );
      } else {
        paramMap.put( paramName, p );
      }
    }
    return paramMap;
  }

}
