/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.ISimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GatherStatsListener implements IPentahoSystemListener {

  private static final String JOB_NAME = "Gather Stats";

  private String transFileName;

  private static final Log logger = LogFactory.getLog( GatherStatsListener.class );
  private int intervalInSeconds = -1;

  Map<String, Object> jobMap = new HashMap<>();

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean startup( IPentahoSession arg0 ) {
    try {
      scheduleJob( intervalInSeconds );
    } catch ( Exception e ) {
      logger.error( "Exception when scheduling stats job", e );
    }
    return true;
  }

  private void scheduleJob( int intervalInSeconds ) throws Exception {

    IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    assert scheduler != null;
    IJobTrigger trigger = scheduler.createSimpleJobTrigger( new Date(), null, -1, intervalInSeconds );

    jobMap.put( "transFileName", getTransFileName() );
    scheduler.createJob( GatherStatsListener.JOB_NAME, GatherStatsAction.class, jobMap, trigger );
    logger.info( "Statistics gathering jop has been scheduled." );
  }

  /*
   * protected Class<?> resolveClass(String className) throws PluginBeanException {
   *
   * Class<?> clazz = null;
   *
   * IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class); clazz = pluginManager.loadClass(className);
   * if (clazz == null) { throw new PluginBeanException(); } return clazz; }
   */

  public int getIntervalInSeconds() {
    return intervalInSeconds;
  }

  public String getTransFileName() {
    return transFileName;
  }

  public void setTransFileName( String transfileName ) {
    this.transFileName = transfileName;
  }

  public void setIntervalInSeconds( int intervalInSeconds ) {
    this.intervalInSeconds = intervalInSeconds;
  }

}
