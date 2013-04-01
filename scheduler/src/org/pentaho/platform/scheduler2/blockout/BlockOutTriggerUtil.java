package org.pentaho.platform.scheduler2.blockout;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.quartz.Trigger;

public class BlockOutTriggerUtil {

  public static long getBlockDuration(Trigger trigger) {
    return trigger.getJobDataMap().getLong(IBlockoutManager.BLOCK_DURATION_KEY);
  }

  public static void setBlockDuration(Trigger trigger, long blockDuration) {
    trigger.getJobDataMap().put(IBlockoutManager.BLOCK_DURATION_KEY, blockDuration);
  }
}
