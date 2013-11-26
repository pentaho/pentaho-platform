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

package org.pentaho.platform.api.scheduler2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JobParamsAdapter extends XmlAdapter<JobParams, Map<String, Serializable>> {

  public JobParams marshal( Map<String, Serializable> v ) throws Exception {
    ArrayList<JobParam> params = new ArrayList<JobParam>();
    for ( Map.Entry<String, Serializable> entry : v.entrySet() ) {
      JobParam jobParam = new JobParam();
      jobParam.name = entry.getKey();
      jobParam.value = entry.getValue().toString();
      params.add( jobParam );
    }
    JobParams jobParams = new JobParams();
    jobParams.jobParams = params.toArray( new JobParam[0] );
    return jobParams;
  }

  public Map<String, Serializable> unmarshal( JobParams v ) throws Exception {
    HashMap<String, Serializable> paramMap = new HashMap<String, Serializable>();
    for ( JobParam jobParam : v.jobParams ) {
      paramMap.put( jobParam.name, jobParam.value );
    }
    return paramMap;
  }

}
