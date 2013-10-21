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

package org.pentaho.platform.api.repository2.unified.data.sample;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

/**
 * An {@code IRepositoryFileData} for illustrative purposes.
 * 
 * @author mlowery
 */
public class SampleRepositoryFileData implements IRepositoryFileData {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final long serialVersionUID = 8243282317105073909L;

  // ~ Instance fields
  // =================================================================================================

  private String sampleString;

  private boolean sampleBoolean;

  private int sampleInteger;

  // ~ Constructors
  // ====================================================================================================

  public SampleRepositoryFileData( final String sampleString, final boolean sampleBoolean, final int sampleInteger ) {
    super();
    this.sampleString = sampleString;
    this.sampleBoolean = sampleBoolean;
    this.sampleInteger = sampleInteger;
  }

  // ~ Methods
  // =========================================================================================================

  public String getSampleString() {
    return sampleString;
  }

  public boolean getSampleBoolean() {
    return sampleBoolean;
  }

  public int getSampleInteger() {
    return sampleInteger;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.repository2.unified.IRepositoryFileData#getDataSize()
   */
  public long getDataSize() {
    // TODO Auto-generated method stub
    return sampleString.length() + 2;
  }
}
