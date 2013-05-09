package org.pentaho.mantle.client.images;

import com.google.gwt.user.client.ui.Image;

/**
 * User: RFellows
 * Date: 5/8/13
 */
public class ImageUtil {

  private static final String BLANK_IMAGE_PATH = "mantle/images/spacer.gif";

  /**
   * Returns a GWT Image with the src value set to a blank image and applies the provided css style to the element
   * to allow for a background image to be used instead.
   * @param cssStyleName
   * @return
   */
  public static Image getThemeableImage(String... cssStyleName) {
    Image image = new Image(BLANK_IMAGE_PATH);

    for(String style : cssStyleName) {
      image.addStyleName(style);
    }

    return image;
  }

}
