package org.pentaho.platform.repository2.unified.jcr;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


/**
 * @author Rowell Belen
 */
public class LocalizationUtilTest extends TestCase {

  public void testResolveLocalizedString(){

    final String ORIG_TITLE = "orig title";
    final String ORIG_DESCRIPTION = "orig description";

    final String KEY_TITLE = "title";
    final String KEY_DESCRIPTION = "description";

    final String DEFAULT_LOCALE = "default";
    final String DEFAULT_TITLE = "default title";
    final String DEFAULT_DESCRIPTION = "default description";

    Properties defaultProps = new Properties();
    defaultProps.put(KEY_TITLE, DEFAULT_TITLE);
    defaultProps.put(KEY_DESCRIPTION, DEFAULT_DESCRIPTION);

    final String EN_LOCALE = "en";
    final String EN_TITLE = "english title";
    final String EN_DESCRIPTION = "english description";

    Properties enProps = new Properties();
    enProps.put(KEY_TITLE, EN_TITLE);
    enProps.put(KEY_DESCRIPTION, EN_DESCRIPTION);

    final String ES_LOCALE = "es";
    final String ES_TITLE = "spanish title";
    //final String ES_DESCRIPTION = "spanish description";

    Properties esProps = new Properties();
    esProps.put(KEY_TITLE, ES_TITLE);
    //esProps.add(new StringKeyStringValueDto(KEY_DESCRIPTION, ES_DESCRIPTION));

    Map<String, Properties> localePropertiesMap = new HashMap<String, Properties>();
    localePropertiesMap.put(DEFAULT_LOCALE, defaultProps);
    localePropertiesMap.put(EN_LOCALE, enProps);
    localePropertiesMap.put(ES_LOCALE, esProps);


    // test default locale
    LocalizationUtil utils = new LocalizationUtil(localePropertiesMap, new Locale(DEFAULT_LOCALE));
    Assert.assertEquals(DEFAULT_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));
    Assert.assertEquals(DEFAULT_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test english locale
    utils = new LocalizationUtil(localePropertiesMap, new Locale(EN_LOCALE));
    Assert.assertEquals(EN_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));
    Assert.assertEquals(EN_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test spanish locale
    utils = new LocalizationUtil(localePropertiesMap, new Locale(ES_LOCALE));
    Assert.assertEquals(ES_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));

    // spanish description does not exist, should find default
    Assert.assertEquals(DEFAULT_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test missing
    final String MISSING = "missing";
    Assert.assertEquals(null, utils.resolveLocalizedString(MISSING, null));
    Assert.assertEquals(MISSING, utils.resolveLocalizedString(MISSING, MISSING));
  }
}
