package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.logging.Log;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Unit tests for BlockingQuartzJob
 *
 * @author kwalker
 */
public class BlockingQuartzJobTest {
  private Job underlyingJob;

  private JobExecutionContext context;

  private IBlockoutManager blockoutManager;

  private Mockery mockery;

  private Log logger;

  private SchedulerException schedulerException = new SchedulerException("something bad happened");

  @Before
  public void setUp() throws Exception {
    mockery = new Mockery() {
      {
        setImposteriser(ClassImposteriser.INSTANCE);
      }
    };
    underlyingJob = mockery.mock(Job.class);
    context = mockery.mock(JobExecutionContext.class);
    blockoutManager = mockery.mock(IBlockoutManager.class);
    logger = mockery.mock(Log.class);
  }

  @After
  public void tearDown() throws Exception {
    mockery.assertIsSatisfied();
  }

  @Test
  public void testJobIsBlockedDuringABlockout() throws JobExecutionException {
    BlockingQuartzJob blockingJob = createTestBlockingJob(false);
    try {
      mockery.checking(new Expectations() {
        {
          one(blockoutManager).shouldFireNow();
          will(returnValue(false));
          one(logger).warn("Job 'myjob' attempted to run during a blockout period.  This job was not executed");
          one(context).getJobDetail();
          will(returnValue(new JobDetail("myjob", BlockingQuartzJob.class)));
        }
      });
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    blockingJob.execute(context);
  }

  @Test
  public void testJobIsRunWhenNoBlockout() throws JobExecutionException {
    BlockingQuartzJob blockingJob = createTestBlockingJob(false);
    try {
      mockery.checking(new Expectations() {
        {
          one(blockoutManager).shouldFireNow();
          will(returnValue(true));
          one(underlyingJob).execute(with(same(context)));
        }
      });
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    blockingJob.execute(context);
  }

  @Test
  public void testJobIsRunWhenThereIsAnExceptionRetrievingTheBlockoutManager() throws JobExecutionException {
    BlockingQuartzJob blockingJob = createTestBlockingJob(true);
    mockery.checking(new Expectations() {
      {
        one(underlyingJob).execute(with(same(context)));
        one(context).getJobDetail();
        will(returnValue(new JobDetail("somejob", BlockingQuartzJob.class)));
        one(logger)
            .warn(
                "Got Exception retrieving the Blockout Manager for job 'somejob'."
                    + " Executing the underlying job anyway", schedulerException);
      }
    });
    blockingJob.execute(context);
  }

  private BlockingQuartzJob createTestBlockingJob(final boolean throwSchedulerException) {
    return new BlockingQuartzJob() {
      @Override
      Job createUnderlyingJob() {
        return underlyingJob;
      }

      @Override
      IBlockoutManager getBlockoutManager() throws SchedulerException {

        if (throwSchedulerException) {
          throw schedulerException;
        } else {
          return blockoutManager;
        }
      }

      @Override
      Log getLogger() {
        return logger;
      }
    };
  }

}
