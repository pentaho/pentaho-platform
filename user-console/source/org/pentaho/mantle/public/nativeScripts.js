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

/*
 Helper function for translating IFrame mouse events for use in outter window environment.
 This function computes the actual mouse position and returns back a simulated Event
 object with those values.
 */

function translateInnerMouseEvent(element, srcEvent) {
  //position relative to to-left corner of IFrame
  var offsetHeight = srcEvent.clientY;
  var offsetWidth = srcEvent.clientX;

  //walks to DOM adding offsets
  while (element.offsetParent) {
    offsetHeight += element.offsetTop;
    offsetWidth += element.offsetLeft;
    element = element.offsetParent;
  }

  //create a new simulated event to pass on to GWT
  var event;
  if (document.all) {   //IE
    event = document.createEventObject();
    event.detail = srcEvent.detail;
    event.screenX = offsetWidth;
    event.screenY = offsetHeight;
    event.clientX = offsetWidth;
    event.clientY = offsetHeight;
    event.ctrlKey = srcEvent.ctrlKey;
    event.altKey = srcEvent.altKey;
    event.shiftKey = srcEvent.shiftKey;
    event.metaKey = srcEvent.metaKey;
    event.button = srcEvent.button;
    event.relatedTarget = srcEvent.relatedTarget;
  } else {    //Mozilla
    event = document.createEvent('MouseEvents');
    event.initMouseEvent(
        srcEvent.type,
        true,
        true,
        window,
        srcEvent.detail,
        offsetWidth,
        offsetHeight,
        offsetWidth,
        offsetHeight,
        srcEvent.ctrlKey,
        srcEvent.altKey,
        srcEvent.shiftKey,
        srcEvent.metaKey,
        srcEvent.button,
        srcEvent.relatedTarget
    );
  }
  return event;
}
