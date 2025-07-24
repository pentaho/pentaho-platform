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
