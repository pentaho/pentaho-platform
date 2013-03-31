package org.pentaho.platform.engine.core;

import java.util.List;

/**
 * User: nbaker
 * Date: 3/2/13
 */
public class MimeListenerCollection {
  private List<MimeTypeListener> listeners;
  private MimeTypeListener highestListener;
  private MimeTypeListener queriedBean;
  private List<MimeTypeListener> queriedList;

  public List<MimeTypeListener> getListeners() {
    return listeners;
  }

  public void setListeners(List<MimeTypeListener> listeners) {
    this.listeners = listeners;
  }

  public MimeTypeListener getHighestListener() {
    return highestListener;
  }

  public void setHighestListener(MimeTypeListener highestListener) {
    this.highestListener = highestListener;
  }

  public MimeTypeListener getQueriedBean() {
    return queriedBean;
  }

  public void setQueriedBean(MimeTypeListener queriedBean) {
    this.queriedBean = queriedBean;
  }

  public List<MimeTypeListener> getQueriedList() {
    return queriedList;
  }

  public void setQueriedList(List<MimeTypeListener> queriedList) {
    this.queriedList = queriedList;
  }
}
