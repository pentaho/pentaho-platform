/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

public class ColorHelper {
  private static final Map<String, Color> colors = new TreeMap<String, Color>();

  static {
    // TODO: Use an array and binary search at some point
    // Source - W3C defined RGB colors
    ColorHelper.colors.put( "alice blue", Color.decode( "#f0f8ff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "antique white", Color.decode( "#faebd7" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "aquamarine", Color.decode( "#7fffd4" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "azure", Color.decode( "#f0ffff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "beige", Color.decode( "#f5f5dc" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "bisque", Color.decode( "#ffe4c4" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "black", Color.decode( "#000000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "blanched almond", Color.decode( "#ffebcd" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "blue", Color.decode( "#0000ff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "blue violet", Color.decode( "#8a2be2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "brown", Color.decode( "#a52a2a" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "burlywood", Color.decode( "#deb887" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "cadet blue", Color.decode( "#5f9ea0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "chartreuse", Color.decode( "#7fff00" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "chocolate", Color.decode( "#d2691e" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "coral", Color.decode( "#ff7f50" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "cornflower blue", Color.decode( "#6495ed" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "cornsilk", Color.decode( "#fff8dc" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "crimson", Color.decode( "#dc143c" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "cyan", Color.decode( "#00ffff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark blue", Color.decode( "#00008b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark cyan", Color.decode( "#008b8b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark goldenrod", Color.decode( "#b8860b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark gray", Color.decode( "#a9a9a9" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark green", Color.decode( "#006400" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark khaki", Color.decode( "#bdb76b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark magenta", Color.decode( "#8b008b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark olive green", Color.decode( "#556b2f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark orange", Color.decode( "#ff8c00" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark orchid", Color.decode( "#9932cc" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark red", Color.decode( "#8b0000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark salmon", Color.decode( "#e9967a" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark seagreen", Color.decode( "#8dbc8f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark slate blue", Color.decode( "#483d8b" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark slate gray", Color.decode( "#2f4f4f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark turquoise", Color.decode( "#00ded1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dark violet", Color.decode( "#9400d3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "deep pink", Color.decode( "#ff1493" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "deep sky blue", Color.decode( "#00bfff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dim gray", Color.decode( "#696969" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "dodger blue", Color.decode( "#1e90ff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "firebrick", Color.decode( "#b22222" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "floral white", Color.decode( "#fffaf0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "forest green", Color.decode( "#228b22" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "gainsboro", Color.decode( "#dcdcdc" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "ghost white", Color.decode( "#f8f8ff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "gold", Color.decode( "#ffd700" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "goldenrod", Color.decode( "#daa520" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "gray", Color.decode( "#808080" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "green", Color.decode( "#008000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "green yellow", Color.decode( "#adff2f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "honeydew", Color.decode( "#f0fff0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "hot pink", Color.decode( "#ff69b4" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "indian red", Color.decode( "#cd5c5c" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "indigo", Color.decode( "#4b0082" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "ivory", Color.decode( "#fffff0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "khaki", Color.decode( "#f0e68c" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lavender", Color.decode( "#e6e6fa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lavender blush", Color.decode( "#fff0f5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lawngreen", Color.decode( "#7cfc00" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lemon chiffon", Color.decode( "#fffacd" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light blue", Color.decode( "#add8e6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light coral", Color.decode( "#f08080" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light cyan", Color.decode( "#e0ffff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light goldenrod yellow", Color.decode( "#fafad2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light green", Color.decode( "#90ee90" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light grey", Color.decode( "#d3d3d3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light pink", Color.decode( "#ffb6c1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light salmon", Color.decode( "#ffa07a" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light seagreen", Color.decode( "#20b2aa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light sky blue", Color.decode( "#87cefa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light slate gray", Color.decode( "#778899" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light steel blue", Color.decode( "#b0c4de" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "light yellow", Color.decode( "#ffffe0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lime", Color.decode( "#00ff00" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "lime green", Color.decode( "#32cd32" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "linen", Color.decode( "#faf0e6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "magenta", Color.decode( "#ff00ff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "maroon", Color.decode( "#800000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium aquamarine", Color.decode( "#66cdaa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium blue", Color.decode( "#0000cd" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium orchid", Color.decode( "#ba55d3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium purple", Color.decode( "#9370db" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium sea green", Color.decode( "#3cb371" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium slate blue", Color.decode( "#7b68ee" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium spring green", Color.decode( "#00fa9a" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium turquoise", Color.decode( "#48d1cc" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "medium violet red", Color.decode( "#c71585" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "midnight blue", Color.decode( "#191970" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "mint cream", Color.decode( "#f5fffa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "misty rose", Color.decode( "#ffe4e1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "moccasin", Color.decode( "#ffe4b5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "navajo white", Color.decode( "#ffdead" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "navy", Color.decode( "#000080" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "old lace", Color.decode( "#fdf5e6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "olive drab", Color.decode( "#6b8e23" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "orange", Color.decode( "#ffa500" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "orange red", Color.decode( "#ff4500" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "orchid", Color.decode( "#da70d6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "pale goldenrod", Color.decode( "#eee8aa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "pale green", Color.decode( "#98fb98" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "pale turquoise", Color.decode( "#afeeee" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "pale violet red", Color.decode( "#db7093" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "papaya whip", Color.decode( "#ffefd5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "peach puff", Color.decode( "#ffdab9" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "peru", Color.decode( "#cd853f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "pink", Color.decode( "#ffc8cb" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "plum", Color.decode( "#dda0dd" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "powder blue", Color.decode( "#b0e0e6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "purple", Color.decode( "#800080" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "red", Color.decode( "#ff0000" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "rosy brown", Color.decode( "#bc8f8f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "royal blue", Color.decode( "#4169e1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "saddle brown", Color.decode( "#8b4513" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "salmon", Color.decode( "#fa8072" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "sandy brown", Color.decode( "#f4a460" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "sea green", Color.decode( "#2e8b57" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "sea shell", Color.decode( "#fff5ee" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "sienna", Color.decode( "#a0522d" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "silver", Color.decode( "#c0c0c0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "sky blue", Color.decode( "#87ceeb" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "slate blue", Color.decode( "#6a5acd" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "snow", Color.decode( "#fffafa" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "spring green", Color.decode( "#00ff7f" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "steelblue", Color.decode( "#4682b4" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "tan", Color.decode( "#d2b48c" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "teal", Color.decode( "#008080" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "thistle", Color.decode( "#d8bfd8" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "tomato", Color.decode( "#ff6347" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "turquoise", Color.decode( "#40e0d0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "violet", Color.decode( "#ee82ee" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "wheat", Color.decode( "#f5deb3" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "white", Color.decode( "#ffffff" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "whitesmoke", Color.decode( "#f5f5f5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "yellow", Color.decode( "#ffff00" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    ColorHelper.colors.put( "yellow green", Color.decode( "#9acd32" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static Color lookupColor( final String col ) {
    if ( col == null ) {
      return null;
    }
    return ColorHelper.lookupColor( col.toLowerCase(), null );
  }

  public static Color lookupColor( final String col, final Color def ) {
    if ( col == null ) {
      return def;
    }
    Color rtn = ColorHelper.colors.get( col.toLowerCase() );
    return ( rtn != null ) ? rtn : def;
  }

}
