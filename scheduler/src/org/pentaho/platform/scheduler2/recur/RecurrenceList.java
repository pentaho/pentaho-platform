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

package org.pentaho.platform.scheduler2.recur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;

/**
 * Used to specify a list of recurrences representing dates or times. The list may represent years, or days of month or
 * hours, etc. The method to which this class is passed will determine the meaning of the integers within the list.
 * 
 * @author arodriguez
 */
@XmlRootElement
public class RecurrenceList implements ITimeRecurrence {
  private List<Integer> values = new ArrayList<Integer>();

  public RecurrenceList( Integer... values ) {
    this.values = Arrays.asList( values );
  }

  public RecurrenceList() {
  }

  public List<Integer> getValues() {
    return values;
  }

  public void setValues( List<Integer> values ) {
    this.values = values;
  }

}
