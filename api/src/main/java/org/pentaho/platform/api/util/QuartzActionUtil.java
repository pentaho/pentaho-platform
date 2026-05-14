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

package org.pentaho.platform.api.util;

public class QuartzActionUtil {

  public static final String QUARTZ_ACTIONCLASS = "ActionAdapterQuartzJob-ActionClass"; //$NON-NLS-1$
  public static final String QUARTZ_ACTIONID = "ActionAdapterQuartzJob-ActionId"; //$NON-NLS-1$
  public static final String QUARTZ_ACTIONUSER = "ActionAdapterQuartzJob-ActionUser"; //$NON-NLS-1$
  public static final String QUARTZ_APPEND_DATE_FORMAT = "appendDateFormat"; //$NON-NLS-1$
  public static final String QUARTZ_AUTO_CREATE_UNIQUE_FILENAME = "autoCreateUniqueFilename"; //$NON-NLS-1$
  public static final String QUARTZ_LINEAGE_ID = "lineage-id"; //$NON-NLS-1$
  public static final String QUARTZ_LAST_EXECUTION_TIME = "QuartzScheduler-LastExecutionTime"; //$NON-NLS-1$
    /**
   * @deprecated since 2026-02
   * 
   * This reserved map key is no longer used by the scheduler and should not be used by callers.
   * Can only remove if we ensure that no clients have schedules with this property set in their jobParams.
   * Otherwise, it will appear in the UI variable list.
   */
  @Deprecated( since = "2026-02", forRemoval = false )
  public static final String QUARTZ_PREVIOUS_TRIGGER_NOW = "previousTriggerNow"; //$NON-NLS-1$
  public static final String QUARTZ_RESTART_FLAG = "ActionAdapterQuartzJob-Restart"; //$NON-NLS-1$
  public static final String QUARTZ_START_TIME = "startTime"; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER = "ActionAdapterQuartzJob-StreamProvider"; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INLINE_INPUT_FILE = "input file ="; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INLINE_OUTPUT_FILE = ":output file="; //$NON-NLS-1$
  public static final String QUARTZ_STREAMPROVIDER_INPUT_FILE =
    "ActionAdapterQuartzJob-StreamProvider-InputFile"; //$NON-NLS-1$
  public static final String QUARTZ_UIPASSPARAM = "uiPassParam"; //$NON-NLS-1$

}
