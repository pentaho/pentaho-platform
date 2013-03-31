package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.*;
import org.pentaho.platform.engine.core.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * AggregateObectFactory holds a collection of IPentahoObjectFactory implementations, delegating calls to each and
 * collecting the results. Results are ordered by "priority" attribute if present, with the highest priority object
 * returned in the calls to retrieve a single object.
 *
 * {@inheritDoc}
 *
 * User: nbaker
 * Date: 1/15/13
 */
public class AggregateObjectFactory implements IPentahoObjectFactory {
  protected Set<IPentahoObjectFactory> factories = Collections.synchronizedSet(new HashSet<IPentahoObjectFactory>());
  protected IPentahoObjectFactory primaryFactory;
  private Logger logger = LoggerFactory.getLogger(AggregateObjectFactory.class);

  public AggregateObjectFactory(){

  }

  public void registerObjectFactory(IPentahoObjectFactory fact, boolean primary){
    factories.add(fact);
    if(primary){
      primaryFactory = fact;
    }
    logger.debug("New IPentahoObjectFactory registered: "+fact.getName());
  }

  public void registerObjectFactory(IPentahoObjectFactory fact){
    registerObjectFactory(fact, false);
  }


  public IPentahoObjectFactory getPrimaryFactory() {
    return primaryFactory;
  }

  @Override
  public <T> T get(Class<T> interfaceClass, String key, IPentahoSession session) throws ObjectFactoryException {
    // if they want it by id, check for that first
    if(key != null){
      for(IPentahoObjectFactory fact : factories){
        if(fact.objectDefined(key)){
          T object = fact.get(interfaceClass, key, session);
          logger.debug(MessageFormat.format("Found object for key: {0} in factory: {1}", key, fact.getName()));
          return object;
        }
      }
    }

    T fromType = get(interfaceClass, session, null);
    if(fromType != null){
      return fromType;
    }

    String msg = Messages.getInstance().getString("AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT", interfaceClass.getSimpleName());
    throw new ObjectFactoryException(msg);


  }

  @Override
  public <T> T get(Class<T> interfaceClass, IPentahoSession session) throws ObjectFactoryException {

    return get(interfaceClass, session, null);
  }

  private int computePriority(IPentahoObjectReference ref) {
    Map<String, Object> props = ref.getAttributes();
    if(props == null){
      return DEFAULT_PRIORTIY;
    }
    Object sPri = ref.getAttributes().get("priority");
    if(sPri == null){
      return DEFAULT_PRIORTIY;
    }
    try{
      return Integer.parseInt(sPri.toString());
    } catch (NumberFormatException e){
      return DEFAULT_PRIORTIY;
    }

  }

  @Override
  public boolean objectDefined(String key) {
    for(IPentahoObjectFactory fact : factories){
      if(fact.objectDefined(key)){
        logger.debug(MessageFormat.format("Object defined for key: {0} in factory: {1}", key, fact.getName()));
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated All usage of key methods are deprecated, use object properties instead
   */
  @Override
  public Class<?> getImplementingClass(String key) {
    for(IPentahoObjectFactory fact : factories){
      if(fact.objectDefined(key)){
        logger.debug(MessageFormat.format("Found implementing class for key: {0} in factory: {1}", key, fact.getName()));
        return fact.getImplementingClass(key);
      }
    }
    return null;
  }

  @Override
  public void init(String configFile, Object context) {

  }

  @Override
  public <T> List<T> getAll(Class<T> interfaceClass, IPentahoSession curSession) throws ObjectFactoryException {
    return getAll(interfaceClass, curSession, null);
  }

  @Override
  public <T> List<T> getAll(Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties) throws ObjectFactoryException {


    List<IPentahoObjectReference<T>> referenceList = new ArrayList<IPentahoObjectReference<T>>();

    for(IPentahoObjectFactory fact : factories){
      List<IPentahoObjectReference<T>> refs = fact.getObjectReferences(interfaceClass, curSession, properties);
      if(refs != null){
        referenceList.addAll(refs);
      }
    }

    Collections.sort(referenceList, referencePriorityComparitor);

    // create final list of impls
    List<T> entryList = new ArrayList<T>();
    for(IPentahoObjectReference<T> ref : referenceList){
      entryList.add(ref.getObject());
    }

    return entryList;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference(Class<T> clazz, IPentahoSession curSession) throws ObjectFactoryException{

    Set<IPentahoObjectReference<T>> references = new HashSet<IPentahoObjectReference<T>>();

    for(IPentahoObjectFactory fact : factories){
      IPentahoObjectReference<T> found = fact.getObjectReference(clazz, curSession);
      if(found != null){
        references.add(found);
      }
    }
    IPentahoObjectReference<T> highestRef = null;
    int highestRefPriority = -1;
    for(IPentahoObjectReference<T> ref : references){
      int pri = computePriority(ref);
      if(pri  > highestRefPriority){
        highestRef = ref;
        highestRefPriority = pri;
      }
    }

    return highestRef;
  }

  @Override
  public <T> T get(Class<T> clazz, IPentahoSession session, Map<String, String> properties) throws ObjectFactoryException {

    IPentahoObjectReference<T> highestRef = this.getObjectReference(clazz, session, properties);

    if(highestRef == null){
      String msg = Messages.getInstance().getString("AbstractSpringPentahoObjectFactory.WARN_FAILED_TO_RETRIEVE_OBJECT", clazz.getSimpleName());
      throw new ObjectFactoryException(msg);
    }

    return highestRef.getObject();
  }

  @Override
  public boolean objectDefined(Class<?> clazz) {
    for(IPentahoObjectFactory fact : factories){
      if(fact.objectDefined(clazz)){

        logger.debug(MessageFormat.format("Found object for class: {0} in factory: {1}", clazz.getName(), fact.getName()));
        return true;
      }
    }
    return false;
  }

  @Override
  public <T> IPentahoObjectReference<T> getObjectReference(Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties) throws ObjectFactoryException{

    Set<IPentahoObjectReference<T>> references = new HashSet<IPentahoObjectReference<T>>();

    for(IPentahoObjectFactory fact : factories){
      IPentahoObjectReference<T> found = fact.getObjectReference(interfaceClass, curSession, properties);
      if(found != null){
        references.add(found);
      }
    }
    IPentahoObjectReference<T> highestRef = null;
    int highestRefPriority = -1;
    for(IPentahoObjectReference<T> ref : references){
      int pri = computePriority(ref);
      if(pri  > highestRefPriority){
        highestRef = ref;
        highestRefPriority = pri;
      }
    }

    return highestRef;
  }

  public void clear() {
    this.factories.clear();
  }

  private static ReferencePriorityComparitor referencePriorityComparitor = new ReferencePriorityComparitor();


  private static class ReferencePriorityComparitor implements Comparator<IPentahoObjectReference>{
    private static final String PRIORITY = "priority";
    @Override
    public int compare(IPentahoObjectReference ref1, IPentahoObjectReference ref2) {
      int pri1 = extractPriority(ref1);
      int pri2 = extractPriority(ref2);
      if(pri1 == pri2){
        return 0;
      } else if(pri1 < pri2){
        return 1;
      } else {
        return -1;
      }

    }

    private int extractPriority(IPentahoObjectReference ref){
      if(ref == null || ref.getAttributes() == null || !ref.getAttributes().containsKey(PRIORITY)){
        // return default
        return DEFAULT_PRIORTIY;
      }

      try{
        return Integer.parseInt(ref.getAttributes().get(PRIORITY).toString());
      } catch (NumberFormatException e){
        // return default
        return DEFAULT_PRIORTIY;
      }
    }
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences(Class<T> interfaceClass, IPentahoSession curSession) throws ObjectFactoryException{
    return getObjectReferences(interfaceClass, curSession, null);
  }

  @Override
  public <T> List<IPentahoObjectReference<T>> getObjectReferences(Class<T> interfaceClass, IPentahoSession curSession, Map<String, String> properties) throws ObjectFactoryException{
    // Use a set to avoid duplicates
    Set<IPentahoObjectReference<T>> referenceSet = new TreeSet<IPentahoObjectReference<T>>();

    for(IPentahoObjectFactory fact : factories){
      IPentahoObjectReference<T> found = fact.getObjectReference(interfaceClass, curSession, properties);
      if(found != null){
        referenceSet.add(found);
      }
    }

    // transform to a list to sort
    List<IPentahoObjectReference<T>> referenceList = new ArrayList<IPentahoObjectReference<T>> ();
    referenceList.addAll(referenceSet);
    Collections.sort(referenceList, referencePriorityComparitor);
    return referenceList;
  }


  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
