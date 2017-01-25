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

package org.pentaho.platform.scheduler2.ws;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.scheduler2.ws.JaxBSafeMap.JaxBSafeEntry;

/**
 * Converts a Map<String, ParamValue> used to pass {@link ISchedulerService} job parameters to a JAXB marshallable type
 * and back. See <a href="https://jaxb.dev.java.net/guide/Mapping_your_favorite_class.html">this JAXB reference</a>
 * 
 * @author aphillips
 * 
 */
public class JobParamsAdapter extends XmlAdapter<JaxBSafeMap, Map<String, ParamValue>> {

  private static final Log logger = LogFactory.getLog( JobParamsAdapter.class );

  public JaxBSafeMap marshal( Map<String, ParamValue> unsafeMap ) throws Exception {
    try {
      JaxBSafeMap safeMap = new JaxBSafeMap( unsafeMap );
      return safeMap;
    } catch ( Throwable t ) {
      logger.error( t );
    }
    return null;
  }

  public Map<String, ParamValue> unmarshal( JaxBSafeMap safeMap ) throws Exception {
    Map<String, ParamValue> unsafeMap = null;
    try {
      unsafeMap = new HashMap<String, ParamValue>();
      for ( JaxBSafeEntry safeEntry : safeMap.entry ) {
        ParamValue v = safeEntry.getStringValue();
        if ( v == null ) {
          v = safeEntry.getListValue();
        }
        if ( v == null ) {
          v = safeEntry.getMapValue();
        }
        unsafeMap.put( safeEntry.key, v );
      }
      return unsafeMap;
    } catch ( Throwable t ) {
      logger.error( t );
    }
    return null;
  }
}
