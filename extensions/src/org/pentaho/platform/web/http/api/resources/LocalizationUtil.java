package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * @author Rowell Belen
 */
public class LocalizationUtil {

  private RepositoryFileDto repositoryFile;
  private Locale locale;

  public LocalizationUtil(RepositoryFileDto repositoryFile, Locale locale){
    Assert.notNull(repositoryFile);
    this.repositoryFile = repositoryFile;
    this.locale = locale;
  }

  public String resolveLocalizedString(final String propertyName, final String defaultValue){

    String localizedString = findLocalizedString(this.locale, propertyName);
    if(StringUtils.isNotBlank(localizedString)){
      return localizedString;
    }

    // find default locale if provided locale is not found
    localizedString = findLocalizedString(new Locale("default"), propertyName);
    if(StringUtils.isNotBlank(localizedString)){
      return localizedString;
    }

    // return default value if provided/default locales are not found
    return defaultValue;
  }


  private String findLocalizedString(final Locale locale, final String propertyName){

    String localeString = "default";
    if(locale != null){
      localeString = locale.toString();
    }

    if(repositoryFile != null && repositoryFile.getLocalePropertiesMapEntries() != null){
      if(repositoryFile.getLocalePropertiesMapEntries() != null){
        // loop through the locale maps
        for(LocaleMapDto localeMapDto : repositoryFile.getLocalePropertiesMapEntries()){
          if(localeMapDto.getLocale().equals(localeString)){
            if(localeMapDto.getProperties() != null){
              // loop through the locale properties
              for(StringKeyStringValueDto keyValue : localeMapDto.getProperties()){
                if(StringUtils.isNotBlank(keyValue.getKey()) && StringUtils.isNotBlank(keyValue.getValue())){
                  if(keyValue.getKey().equals(propertyName)){
                    return keyValue.getValue(); // found localized string in map
                  }
                }
              }
            }
          }
        }
      }
    }

    return null;
  }
}
