package org.pentaho.commons.util.encoding;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Base64Utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Decoder {
  private Decoder() { }

  public static String decodeIfEncoded( String encodedString ) {
    if ( !StringUtils.isEmpty( encodedString ) && encodedString.startsWith( "ENC:" ) ) {
      String password = new String( Base64Utils.decode( encodedString.substring( 4 ).getBytes() ) );
      return URLDecoder.decode( password.replace( "+", "%2B" ), StandardCharsets.UTF_8 );
    } else {
      return encodedString;
    }
  }
}
