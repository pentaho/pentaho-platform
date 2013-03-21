package org.pentaho.platform.repository.webservices;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.LocalizationUtil;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Rowell Belen
 */
public class LocalizationUtilTest extends TestCase{

  public void testResolveLocalizedString(){

    final String ORIG_TITLE = "orig title";
    final String ORIG_DESCRIPTION = "orig description";

    final String KEY_TITLE = "title";
    final String KEY_DESCRIPTION = "description";

    final String DEFAULT_LOCALE = "default";
    final String DEFAULT_TITLE = "default title";
    final String DEFAULT_DESCRIPTION = "default description";

    List<StringKeyStringValueDto> defaultProps = new ArrayList<StringKeyStringValueDto>();
    defaultProps.add(new StringKeyStringValueDto(KEY_TITLE, DEFAULT_TITLE));
    defaultProps.add(new StringKeyStringValueDto(KEY_DESCRIPTION, DEFAULT_DESCRIPTION));
    final LocaleMapDto defaultMap = new LocaleMapDto(DEFAULT_LOCALE,  defaultProps);

    final String EN_LOCALE = "en";
    final String EN_TITLE = "english title";
    final String EN_DESCRIPTION = "english description";

    List<StringKeyStringValueDto> enProps = new ArrayList<StringKeyStringValueDto>();
    enProps.add(new StringKeyStringValueDto(KEY_TITLE, EN_TITLE));
    enProps.add(new StringKeyStringValueDto(KEY_DESCRIPTION, EN_DESCRIPTION));
    final LocaleMapDto enMap = new LocaleMapDto(EN_LOCALE,  enProps);

    final String ES_LOCALE = "es";
    final String ES_TITLE = "spanish title";
    //final String ES_DESCRIPTION = "spanish description";

    List<StringKeyStringValueDto> esProps = new ArrayList<StringKeyStringValueDto>();
    esProps.add(new StringKeyStringValueDto(KEY_TITLE, ES_TITLE));
    //esProps.add(new StringKeyStringValueDto(KEY_DESCRIPTION, ES_DESCRIPTION));
    final LocaleMapDto esMap = new LocaleMapDto(ES_LOCALE,  esProps);

    List<LocaleMapDto> maps = new ArrayList<LocaleMapDto>();
    maps.add(defaultMap);
    maps.add(enMap);
    maps.add(esMap);

    RepositoryFileDto file = new RepositoryFileDto();
    file.setTitle(ORIG_TITLE);
    file.setDescription(ORIG_DESCRIPTION);
    file.setLocalePropertiesMapEntries(maps);

    // test original value
    Assert.assertEquals(ORIG_TITLE, file.getTitle());
    Assert.assertEquals(ORIG_DESCRIPTION,  file.getDescription());

    // test default locale
    LocalizationUtil utils = new LocalizationUtil(file, new Locale(DEFAULT_LOCALE));
    Assert.assertEquals(DEFAULT_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));
    Assert.assertEquals(DEFAULT_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test english locale
    utils = new LocalizationUtil(file, new Locale(EN_LOCALE));
    Assert.assertEquals(EN_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));
    Assert.assertEquals(EN_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test spanish locale
    utils = new LocalizationUtil(file, new Locale(ES_LOCALE));
    Assert.assertEquals(ES_TITLE, utils.resolveLocalizedString(KEY_TITLE, null));

    // spanish description does not exist, should find default
    Assert.assertEquals(DEFAULT_DESCRIPTION, utils.resolveLocalizedString(KEY_DESCRIPTION, null));

    // test missing
    final String MISSING = "missing";
    Assert.assertEquals(null, utils.resolveLocalizedString(MISSING, null));
    Assert.assertEquals(MISSING, utils.resolveLocalizedString(MISSING, MISSING));
  }
}
