package org.apache.jackrabbit.core;

import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;

/**
 * Used to write and remove unversioned properties
 * in versioned nodes.
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 *
 */
public class PentahoUnversionedPropertyHelper {

  public void setUnversionedProperty(final Node node, final String namespace, final String name, final String value) throws RepositoryException {
    Name nobj = NameFactoryImpl.getInstance().create(namespace, name);
    ValueFactory vf = ((NodeImpl)node).session.getValueFactory();
    Value vobj = vf.createValue(value);
    InternalValue intVs = InternalValue.create(vobj, ((NodeImpl)node).session);
    ((NodeImpl)node).internalSetProperty(nobj, intVs);
  }
  
  public void setUnversionedProperty(final Node node, final String namespace, final String name, final Calendar value) throws RepositoryException {
    Name nobj = NameFactoryImpl.getInstance().create(namespace, name);
    ValueFactory vf = ((NodeImpl)node).session.getValueFactory();
    Value vobj = vf.createValue(value);
    InternalValue intVs = InternalValue.create(vobj, ((NodeImpl)node).session);
    ((NodeImpl)node).internalSetProperty(nobj, intVs);
  }
  
  public void removeUnversionedItem(final Item item) throws RepositoryException {
    ((ItemImpl)item).internalRemove(true);
  }
}
