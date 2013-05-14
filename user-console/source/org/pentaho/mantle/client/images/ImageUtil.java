package org.pentaho.mantle.client.images;

/**
 * User: RFellows
 * Date: 5/8/13
 */
public class ImageUtil extends org.pentaho.gwt.widgets.client.utils.ImageUtil {

  private static final String BLANK_IMAGE_PATH = "mantle/images/spacer.gif";

  @Override
  public String getBlankImagePath() {
    return BLANK_IMAGE_PATH;
  }

}
