package org.pentaho.platform.scheduler2.blockout;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.NthIncludedDayTrigger;

public class NthIncludedDayBlockOutTrigger extends NthIncludedDayTrigger implements IBlockoutTrigger {

  private static final long serialVersionUID = -3200469156209304063L;

  /**
   * Required by web services
   */
  public NthIncludedDayBlockOutTrigger() {
    super();
  }

  public NthIncludedDayBlockOutTrigger(String name, long blockOutDuration) {
    super(name, IBlockoutManager.BLOCK_GROUP, name, IBlockoutManager.BLOCK_GROUP);
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
