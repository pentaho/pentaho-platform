package org.pentaho.platform.api.engine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies IServiceBarrier instances by service id.
 *
 * Created by nbaker on 1/26/15.
 */
public interface IServiceBarrierManager {

  /**
   * Get the barrier for the given service ID
   *
   * @param serviceID
   * @return
   */
  IServiceBarrier getServiceBarrier( String serviceID );
  
  List<IServiceBarrier> getAllServiceBarriers();
  
  Locator LOCATOR = new Locator();

  class Locator {
    private static final String MANAGER_CLASS = "org.pentaho.platform.api.engine.IServiceBarrierManager.class";
    IServiceBarrierManager instance;
    private static Logger logger = LoggerFactory.getLogger( IServiceBarrierManager.Locator.class );

    public IServiceBarrierManager getManager() {

      if ( instance == null ) {
        if ( System.getProperty( MANAGER_CLASS ) != null ) {
          try {
            instance = (IServiceBarrierManager) Class.forName( System.getProperty( MANAGER_CLASS ) ).newInstance();
          } catch ( ClassNotFoundException e ) {
            logger.error( "IServiceBarrierManager class not found", e );
          } catch ( InstantiationException | IllegalAccessException e ) {
            logger.error( "IServiceBarrierManager class could not be instantiated", e );
          }
        }
        if ( instance == null ) {
          instance = new ServiceBarrierManager();
        }
      }
      return instance;
    }
  }
}
