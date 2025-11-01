/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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

function prepareHorizontalScroll( id ) {
    var outerDivId = '#' + id;
    var selectSelector = outerDivId + ' select';
    var helperDivSelector = outerDivId + ' div:last';
    $(helperDivSelector).css('width', $(selectSelector).outerWidth());
    $(selectSelector).css('width', $(outerDivId).outerWidth());
    $(outerDivId).scroll(function() {
        $(selectSelector).css('width', $(this).outerWidth() + $(this).scrollLeft());
    });
}

function updateHelperDiv( id ) {
    var outerDivId = '#' + id;
    var selectSelector = outerDivId + ' select';
    var helperDivSelector = outerDivId + ' div:last';
    var selectStyle = $(selectSelector).prop('style');
    if( selectStyle != null ) {
        //remove width to get element client width
        selectStyle.removeProperty('width');
        //set select full width
        $(helperDivSelector).css('width', $(selectSelector).outerWidth());
        //restore select width to fixed
        $(selectSelector).css('width', $(outerDivId).outerWidth());
    }
}

function sendRequest( url ) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", url, false ); // send synchronous request
    xmlHttp.send( null );
    return xmlHttp.responseText;
}
