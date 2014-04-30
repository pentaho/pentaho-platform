package org.pentaho.platform.engine.core.system.objfac.references;

import org.pentaho.platform.api.engine.IPentahoObjectReference;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * A base class for IPentahoObjectReference implementations.
 * <p/>
 * Created by nbaker on 4/11/14.
 */
public abstract class AbstractPentahoObjectReference<T> implements IPentahoObjectReference<T> {

  private final Map<String, Object> attributes;
  private final int ranking;
  private Class<T> type;
  private Logger logger = LoggerFactory.getLogger( AbstractPentahoObjectReference.class );

  protected AbstractPentahoObjectReference( Class<T> type, Map<String, Object> attributes,
                                            int ranking ) {
    if ( type == null ) {
      throw new IllegalArgumentException( "type cannot be null" );
    }
    if ( attributes == null ) {
      attributes = Collections.<String, Object>emptyMap();
    }
    this.type = type;
    this.attributes = attributes;
    this.ranking = ranking;
  }


  @Override public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override public T getObject() {
    try {
      return createObject();
    } catch ( ObjectFactoryException e ) {
      logger.error( "Error creating object. Null will be returned.", e );
    }
    return null;
  }

  protected abstract T createObject() throws ObjectFactoryException;

  @Override public Integer getRanking() {
    return ranking;
  }

  @Override public int compareTo( IPentahoObjectReference<T> o ) {
    if ( o == null ) {
      return 1;
    }
    if ( o == this ) {
      return 0;
    }
    return this.getRanking().compareTo( o.getRanking() );
  }

  public Class<T> getObjectClass() {
    return this.type;
  }

  public abstract static class Builder<T, B extends Builder<T, B>> {

    protected int priority;
    protected Map<String, Object> attributes;
    protected Class<T> type;

    public Builder() {

    }

    public B attributes( Map<String, Object> attributes ) {
      this.attributes = attributes;
      return self();
    }

    public B priority( int priority ) {
      this.priority = priority;
      return self();
    }

    protected B type( Class<T> type ) {
      this.type = type;
      return self();
    }

    protected abstract B self();


    public abstract IPentahoObjectReference<T> build();
  }


}
