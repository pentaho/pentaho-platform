/*
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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * Created Feb 21, 2006 
 * @author wseyler
 */

package org.pentaho.platform.plugin.action.builtin;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

public class ContentRepositoryCleaner extends ComponentBase {

  private static final long serialVersionUID = 1L;

  private static final String AGE = "days_old"; //$NON-NLS-1$

  private static final String DELETE_COUNT = "delete_count"; //$NON-NLS-1$

  @Override
  protected boolean validateAction() {
    if (!isDefinedInput(ContentRepositoryCleaner.AGE)) {
      return false;
    }
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }

  @Override
  public void done() {
  }

  @Override
  protected boolean executeAction() throws Throwable {
    // get daysback off the input
    int daysBack = Integer.parseInt(getInputValue(ContentRepositoryCleaner.AGE).toString());
    // make sure it's a negative number
    daysBack = Math.abs(daysBack) * -1;

    // get todays calendar
    Calendar calendar = Calendar.getInstance();
    // subtract (by adding a negative number) the daysback amount
    calendar.add(Calendar.DATE, daysBack);
    // create the new date for the content repository to use
    Date agedDate = new Date(calendar.getTimeInMillis());
    // get the content repository and tell it to remove the items older than
    // agedDate
    IContentRepository contentRepository = PentahoSystem.get(IContentRepository.class, getSession());
    int deleteCount = contentRepository.deleteContentOlderThanDate(agedDate);
    // return the number of files deleted
    setOutputValue(ContentRepositoryCleaner.DELETE_COUNT, Integer.toString(deleteCount));

    OutputStream feedbackOutputStream = getFeedbackOutputStream();
    if (feedbackOutputStream != null) { // We have a feedback stream so we'll send some messages to it.
      feedbackOutputStream.write(Messages.getInstance().getString("ContentRepositoryCleaner.INFO_0001").getBytes()); //$NON-NLS-1$
      feedbackOutputStream.write(Integer.toString(deleteCount).getBytes());
      feedbackOutputStream.write(Messages.getInstance().getString("ContentRepositoryCleaner.INFO_0002").getBytes()); //$NON-NLS-1$
      feedbackOutputStream.write(Integer.toString(Math.abs(daysBack)).getBytes());
      feedbackOutputStream.write(Messages.getInstance().getString("ContentRepositoryCleaner.INFO_0003").getBytes()); //$NON-NLS-1$
    }
    return true;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(ContentRepositoryCleaner.class);
  }

}
