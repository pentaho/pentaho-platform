package org.pentaho.platform.engine.core.system.objfac.references;

import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.util.Map;

/**
 * This reference type will call the configured {@link org.pentaho.platform.api.engine.IObjectCreator} to serve every
 * call to getObject(). While this class is called "Prototype", the implementation of the configured IObjectCreator is
 * not required to return unique instances. This is done to support other unanticipated object lifecycles.
 * <p/>
 * <p/>
 * Created by nbaker on 4/15/14.
 */
public class PrototypePentahoObjectReference<T> extends AbstractPentahoObjectReference<T> {

  private final IObjectCreator<T> creator;


  public PrototypePentahoObjectReference( Class<T> type, IObjectCreator<T> creator, Map<String, Object> attributes,
                                          int priority ) {
    super( type, attributes, priority );
    this.creator = creator;
  }

  @Override protected T createObject() throws ObjectFactoryException {
    final IPentahoSession session = PentahoSessionHolder.getSession();
    try {
      return creator.create( session );
    } catch ( Exception e ) {
      throw new ObjectFactoryException( "Error creating instance", e );
    }
  }

  /**
   * workaround for inheritance in Builders. Ideas taken from: https://weblogs.java
   * .net/blog/emcmanus/archive/2010/10/25/using-builder-pattern-subclasses
   */
  private abstract static class BuilderBase<T, B extends BuilderBase<T, B>>
    extends AbstractPentahoObjectReference.Builder<T, B> {
    IObjectCreator<T> creator;

    public B creator( IObjectCreator<T> creator ) {
      this.creator = creator;
      return self();
    }

    @Override public PrototypePentahoObjectReference<T> build() {
      return new PrototypePentahoObjectReference<T>( this.type, this.creator, this.attributes, this.priority );
    }
  }

  /**
   * Public builder, implementation specific methods should be in BuilderBase, only self() should be defined here
   */
  public static class Builder<T> extends BuilderBase<T, Builder<T>> {
    public Builder( Class<T> type ) {
      type( type );
    }

    @Override public Builder<T> self() {
      return this;
    }
  }


}
