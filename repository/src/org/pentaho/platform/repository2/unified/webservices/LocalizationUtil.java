package org.pentaho.platform.repository2.unified.webservices;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * @author Rowell Belen
 */
public class LocalizationUtil {

  private RepositoryFileDto repositoryFile;
  private Locale locale;

  private final String DEFAULT = "default";

  public LocalizationUtil(RepositoryFileDto repositoryFile, Locale locale){
    Assert.notNull(repositoryFile);
    this.repositoryFile = repositoryFile;
    this.locale = locale;
  }

  public String resolveLocalizedString(final String propertyName, final String defaultValue){

    String localizedString;

    if(this.locale != null){
      // find locale (language & country) - example: en_US
      localizedString = findLocalizedString(this.locale.toString(), propertyName);
      if(StringUtils.isNotBlank(localizedString)){
        return localizedString;
      }

      // find locale (language only) - example: en
      localizedString = findLocalizedString(this.locale.getLanguage(), propertyName);
      if(StringUtils.isNotBlank(localizedString)){
        return localizedString;
      }
    }

    // find default locale if provided locale is not found
    localizedString = findLocalizedString(DEFAULT, propertyName);
    if(StringUtils.isNotBlank(localizedString)){
      return localizedString;
    }

    // return default value if provided/default locales are not found
    return defaultValue;
  }


  private String findLocalizedString(final String locale, final String propertyName){

    String localeString = locale;
    if(StringUtils.isBlank(localeString)){
      localeString = DEFAULT;
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
