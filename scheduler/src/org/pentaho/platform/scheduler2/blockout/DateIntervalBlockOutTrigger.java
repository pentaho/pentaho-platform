package org.pentaho.platform.scheduler2.blockout;

import java.util.Date;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.DateIntervalTrigger;

public class DateIntervalBlockOutTrigger extends DateIntervalTrigger implements IBlockoutTrigger {

  private static final long serialVersionUID = -9075793310161634134L;

  /**
   * Required by web services
   */
  public DateIntervalBlockOutTrigger() {
    super();
  }

  public DateIntervalBlockOutTrigger(String name, Date startTime, Date endTime, IntervalUnit intervalUnit,
      int repeatInterval, long blockOutDuration) {
    super();

    new DateIntervalTrigger(name, IBlockoutManager.BLOCK_GROUP, name, IBlockoutManager.BLOCK_GROUP, startTime, endTime,
        intervalUnit, repeatInterval);
    setBlockDuration(blockOutDuration);
  }

  @Override
  public long getBlockDuration() {
    return BlockOutTriggerUtil.getBlockDuration(this);
  }

  @Override
  public void setBlockDuration(long blockDuration) {
    BlockOutTriggerUtil.setBlockDuration(this, blockDuration);
  }

}
