package org.pentaho.platform.scheduler2.blockout;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.CronTrigger;

public class CronBlockOutTrigger extends CronTrigger implements IBlockoutTrigger {

  private static final long serialVersionUID = -6425320569934454278L;

  public CronBlockOutTrigger() {
    super();
  }

  public CronBlockOutTrigger(String name, Date startTime, Date endTime, String cronExpression, TimeZone timeZone,
      long blockOutDuration) throws ParseException {
    super(name, IBlockoutManager.BLOCK_GROUP, name, IBlockoutManager.BLOCK_GROUP, startTime, endTime, cronExpression,
        timeZone);
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
