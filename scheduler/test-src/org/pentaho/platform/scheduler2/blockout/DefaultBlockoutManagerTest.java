/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 13, 2013 
 * @author wseyler
 */


package org.pentaho.platform.scheduler2.blockout;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManagerTest {
  IBlockoutManager blockoutManager;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    blockoutManager = new DefaultBlockoutManager();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#addBlockout(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testAddBlockout() {
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("blockout", new Date(), null, -1, 1000000, 50000 );
    try {
      blockoutManager.addBlockout(trigger);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      assertEquals(blockoutManager.getBlockout("blockout"), trigger);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#getBlockout(java.lang.String)}.
   */
  @Test
  public void testGetBlockout() {
    SimpleBlockoutTrigger trigger1 = new SimpleBlockoutTrigger("blockout1", new Date(), null, -1, 1000000, 50000 );
    SimpleBlockoutTrigger trigger2 = new SimpleBlockoutTrigger("blockout2", new Date(), null, -1, 1000000, 50000 );
    try {
      blockoutManager.addBlockout(trigger1);
      blockoutManager.addBlockout(trigger2);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      assertEquals(blockoutManager.getBlockout("blockout1"), trigger1);
      assertEquals(blockoutManager.getBlockout("blockout2"), trigger2);
      assertNotSame(trigger1, blockoutManager.getBlockout("blockout2"));
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  public void testGetBlockouts() {
    try {
      IBlockoutTrigger[] blockouts = blockoutManager.getBlockouts();
      for (IBlockoutTrigger trigger : blockouts) {
        blockoutManager.deleteBlockout(((Trigger)trigger).getName());
      }
      SimpleBlockoutTrigger trigger1 = new SimpleBlockoutTrigger("blockout1", new Date(), null, -1, 1000000, 50000 );
      SimpleBlockoutTrigger trigger2 = new SimpleBlockoutTrigger("blockout2", new Date(), null, -1, 1000000, 50000 );
      blockoutManager.addBlockout(trigger1);
      blockoutManager.addBlockout(trigger2);

      blockouts = blockoutManager.getBlockouts();
      assertEquals(2, blockouts.length);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#updateBlockout(java.lang.String, org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testUpdateBlockout() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#deleteBlockout(java.lang.String)}.
   */
  @Test
  public void testDeleteBlockout() {
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("deleteBlockout", new Date(), null, -1, 1000000, 50000 );
    try {
      blockoutManager.addBlockout(trigger);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      boolean success = blockoutManager.deleteBlockout("deleteBlockout");
      assertTrue(success);
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willFire(org.quartz.Trigger)}.
   */
  @Test
  public void testWillFire() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#shouldFireNow()}.
   */
  @Test
  public void testShouldFireNow() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testWillBlockSchedules() {
    fail("Not yet implemented");
  }

}
