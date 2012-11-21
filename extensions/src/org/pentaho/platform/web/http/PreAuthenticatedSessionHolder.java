package org.pentaho.platform.web.http;

import org.apache.commons.codec.digest.DigestUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import java.util.*;
import java.util.concurrent.*;

/**
 * This class serves to capture a users IPentahoSession and Spring Authentication by assigning them a key. The returned
 * key can be used by another party to regain the users session/authentication, in essence logging them in from another
 * client.
 *
 * User: nbaker
 * Date: 6/28/12
 */
public class PreAuthenticatedSessionHolder {

  private final Map<String, SessionAuthenticationTuple> sessionMap = Collections.synchronizedMap(new HashMap<String, SessionAuthenticationTuple>());

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
    new ThreadFactory(){
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "PreAuthenticationSessionHolder-Eviction");
        t.setDaemon(true);
        return t;
      }
    }
  );
  private ScheduledFuture<?> scheduleHandle;
  private int TTL = 10 * 60 * 1000; // default to 10 minutes
  private int evictionInterval = 60;


  /**
   * Constructs a new session holder with the given TTL and eviction schedule.
   *
   * @param ttl The time a captured session will be retained
   * @param evictionInterval The interval in which sessions will be scanned and evicted if over the TTL
   */
  public PreAuthenticatedSessionHolder(int ttl, int evictionInterval) {
    this.TTL = ttl * 1000;
    this.evictionInterval = evictionInterval;
    initializeEvictionSchedule();
  }


  /**
   * Constructs a new session holder with the default TTL and eviction schedule.
   */
  public PreAuthenticatedSessionHolder() {
    initializeEvictionSchedule();
  }

  private void initializeEvictionSchedule() {
    // Start a recurring thread to clean out old entries.
    final Runnable sessionCleaner = new Runnable() {
      public void run() {
        Iterator<Map.Entry<String, SessionAuthenticationTuple>> iterator = sessionMap.entrySet().iterator();
        while (iterator.hasNext()) {
          Map.Entry<String, SessionAuthenticationTuple> entry = iterator.next();
          if(entry.getValue().entryTime.getTime() + TTL < new Date().getTime()){
            // more than the allowed time has passed for this entry. Evict
            sessionMap.remove(entry.getKey());
          }
        }
      }
    };
    scheduleHandle = scheduler.scheduleWithFixedDelay(sessionCleaner, evictionInterval, evictionInterval, TimeUnit.SECONDS);

  }

  /**
   * Stores the active IPentahoSession and Spring Security Authentication objects (both ThreadLocal based).
   *
   * @return key associated with the captured session.
   */
  public String captureSession() {
    SessionAuthenticationTuple tuple = new SessionAuthenticationTuple();
    tuple.session = PentahoSessionHolder.getSession();
    tuple.auth = SecurityContextHolder.getContext().getAuthentication();
    tuple.entryTime = new Date();
    String hash = DigestUtils.md5Hex(tuple.session.getId().getBytes());
    sessionMap.put(hash, tuple);
    return hash;
  }

  /**
   * Assigns the IPentahoSession and Authentication stored with the given key to the current request.
   *
   * @param hash Stored key
   *
   * @return success if the given key matches a session stored in the holder.
   */
  public boolean restoreSession(String hash){
    SessionAuthenticationTuple tuple = sessionMap.get(hash);
    if(tuple == null){
      return false;
    }

    SecurityContextHolder.getContext().setAuthentication(tuple.auth);
    PentahoSessionHolder.setSession(tuple.session);
    return true;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  /**
   * This should be called with this class is no longer in use. Cleans-up the eviction scheduled task.
   */
  public void close() {
    if(!scheduler.isShutdown()){
      try{
        scheduleHandle.cancel(true);
        scheduler.shutdown();
      } catch (Exception ignored){ }
    }
  }

  private static class SessionAuthenticationTuple {
    IPentahoSession session;
    Authentication auth;
    Date entryTime;
  }

}
