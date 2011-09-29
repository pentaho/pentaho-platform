/*
  Helper function for translating IFrame mouse events for use in outter window environment.
  This function computes the actual mouse position and returns back a simulated Event 
  object with those values.
*/
function translateInnerMouseEvent(element, srcEvent){
  //position relative to to-left corner of IFrame
  var offsetHeight = srcEvent.clientY;
  var offsetWidth = srcEvent.clientX;
  
  //walks to DOM adding offsets
  while(element.offsetParent){
    offsetHeight += element.offsetTop;
    offsetWidth += element.offsetLeft;
    element = element.offsetParent;
  }

  //create a new simulated event to pass on to GWT
  var event;
  if(document.all){   //IE
    event = document.createEventObject();
    evObj.detail = srcEvent.detail;
    evObj.screenX = offsetWidth;
    evObj.screenY = offsetHeight;
    evObj.clientX = offsetWidth;
    evObj.clientY = offsetHeight;
    evObj.ctrlKey = srcEvent.ctrlKey;
    evObj.altKey = srcEvent.altKey;
    evObj.shiftKey = srcEvent.shiftKey;
    evObj.metaKey = srcEvent.metaKey;
    evObj.button = srcEvent.button;
    evObj.relatedTarget = srcEvent.relatedTarget;
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