package org.pentaho.platform.api.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by nbaker on 2/20/15.
 */
public class ServiceBarrierManager implements IServiceBarrierManager {
  WeakHashMap<String, IServiceBarrier> barriers = new WeakHashMap<String, IServiceBarrier>();

  @Override
  public IServiceBarrier getServiceBarrier( String serviceID ) {
    if ( barriers.containsKey( serviceID ) ) {
      return barriers.get( serviceID );
    }
    PhaserServiceBarrier phaserServiceBarrier = new PhaserServiceBarrier( serviceID );
    barriers.put( serviceID, phaserServiceBarrier );
    return phaserServiceBarrier;
  }

  @Override
  public List<IServiceBarrier> getAllServiceBarriers() {
    return new ArrayList<IServiceBarrier>( barriers.values() );
  }

}
