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

package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable Extra Meta Data. Use the {@link RepositoryFileExtraMetaData.Builder} to create instances.
 *
 * @author bcosta
 */
public class RepositoryFileExtraMetaData implements Serializable {

  // ~ Static fields/initializers
  // ======================================================================================
  private static final long serialVersionUID = 3489219057324293842L;

  // ~ Instance fields
  // =================================================================================================
  private final Serializable id;
  private final Map<String, Serializable> extraMetaData;

  // ~ Constructors
  // ====================================================================================================
  public RepositoryFileExtraMetaData( Serializable id, Map<String, Serializable> extraMetaData ) {
    super();
    notNull( extraMetaData );
    this.id = id;
    this.extraMetaData = new HashMap<>( extraMetaData );
  }

  // ~ Methods
  // =========================================================================================================

  private void notNull( final Object obj ) {
    if ( obj == null ) {
      throw new IllegalArgumentException();
    }
  }

  public Map<String, Serializable> getExtraMetaData() {
    return Collections.unmodifiableMap( extraMetaData );
  }

  public Serializable getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 1289;
    int result = 1;
    result = prime * result + ( ( extraMetaData == null ) ? 0 : extraMetaData.hashCode() );
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {

    if ( this == obj ) {
      return true;
    }

    if ( obj == null ) {
      return false;
    }

    if ( getClass() != obj.getClass() ) {
      return false;
    }

    RepositoryFileExtraMetaData other = (RepositoryFileExtraMetaData) obj;

    if ( extraMetaData == null ) {
      if ( other.extraMetaData != null ) {
        return false;
      }
    } else if ( !extraMetaData.equals( other.extraMetaData ) ) {
      return false;
    }

    if ( id == null ) {
      if ( other.id != null ) {
        return false;
      }
    } else if ( !id.equals( other.id ) ) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "RepositoryFileExtraMetaData [id=" + id + ", extraMetaData=" + extraMetaData + "]";
  }


  // ~ Inner classes
  // ===================================================================================================
  public static class Builder {

    private Serializable id;
    private Map<String, Serializable> extraMetaData;

    public Builder( final Serializable id, final Map<String, Serializable> extraMetaData ) {
      this.id = id;
      this.extraMetaData = Collections.unmodifiableMap( extraMetaData );
    }

    public Builder( final Map<String, Serializable> extraMetaData ) {
      this( null, extraMetaData );
    }

    public Builder( final Serializable id ) {
      this( id, new HashMap<>() );
    }

    public RepositoryFileExtraMetaData build() {
      return new RepositoryFileExtraMetaData( id, extraMetaData );
    }

    public Builder id( final Serializable id1 ) {
      this.id = id1;
      return this;
    }

    public Builder extraMetaData( final Map<String, Serializable> extraMetaData ) {
      this.extraMetaData = extraMetaData;
      return this;
    }
  }

}
