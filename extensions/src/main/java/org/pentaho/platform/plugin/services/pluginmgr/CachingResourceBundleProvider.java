package org.pentaho.platform.plugin.services.pluginmgr;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.locale.IResourceBundleProvider;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class CachingResourceBundleProvider implements IResourceBundleProvider {
  private final Map<Locale, ResourceBundle> cache = new ConcurrentHashMap<>();
  private final IResourceBundleProvider delegate;

  public CachingResourceBundleProvider( IResourceBundleProvider delegate ) {
    this.delegate = Objects.requireNonNull( delegate );
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public ResourceBundle getResourceBundle( @Nullable Locale locale ) {
    if ( locale == null ) {
      locale = LocaleHelper.getLocale();
    }

    return cache.computeIfAbsent( locale, delegate::getResourceBundle );
  }
}
