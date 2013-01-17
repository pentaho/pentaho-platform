/*
 
Correctly handle PNG transparency in Win IE 5.5 & 6.
http://homepage.ntlworld.com/bobosola. Updated 18-Jan-2006.

Use in <HEAD> of your page
<script type="text/javascript" src="pngfix.js"></script>

add  fixPNGs() call to your document's <body> onload attribute
<body onLoad="fixPNGs()">

REQUIREMENTS:
There needs to be a 1 pixel transparent gif named "spacer.gif" in the same directory where this script file is loaded from.
Optionally you can set a global variable PngFix.spacerURL as below:


OPTIONS:
You may declare a base url for images. This shouldn't been needed but is available. Below is an example of how to set it:
<script type="text/javascript">
    PngFix.baseURL = "http://www.mysite.com/images/";
</script>

Instead of having a spacer.gif where this script is loaded, you can provide a URL to an existing spacer.gif as follows:
<script type="text/javascript">
    PngFix.spacerURL = "/images/spacer.gif";
</script>


WARNINGS!!
Problem: Containers with PNGs as backgrounds that "stretch" (repeat-x,y) that also have a padding style will not scale properly. 
The filter will stretch the image into the padding area in error
Solution: Try to pad the parent or use margin instead.

Problem: Containers with PNGs that have a background-position other than "top left" or "left top" will not be converted to the filter 
method as we cannot position the filter image properly.
Solution: Do not enter a position of "top left" as this is the defualt. There is no workaround for other positionings.

Problem: Filter honors CSS heights moreso than HTML. If an element is defined as 10px, but in that element something makes it 
15px (pushing the parent), the filter will still render it as 10px. 
Solution: This is more a problem of a style not matching implementation. 

*/
PngFix = {};
PngFix.spacerURL = "spacer.gif";
PngFix.baseURL = "";

function fixPNGs(){
    var arVersion = navigator.appVersion.split("MSIE")
    var version = parseFloat(arVersion[1])

    if ((version >= 5.5) && (version < 7) && (document.body.filters)) 
    {
    
       if(PngFix.baseURL != "" && !PngFix.baseURL.match("/$")){
            PngFix.baseURL += "/";
       }
       
      //replaces images with spacers and puts the existing image as a background filter
       for(var i=0; i<document.images.length; i++)
       {
          var img = document.images[i]
          var imgName = img.src.toUpperCase()
          if (imgName.substring(imgName.length-3, imgName.length) == "PNG"  && !img.getAttribute("previouslyProcessed"))
          {
            if(img.onclick || img.onmouseover || img.onmouseout){ 
              // If there are events on this image, re-use the tag (some layout issues with 
              // doing this, but at least the events work)
              img.style.height = img.height + "px";
              img.style.width = img.width+"px";
              img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader"
              + "(src=\'" + img.src + "\', sizingMethod='scale');\""; 
              img.src = PngFix.spacerURL;
              img.style.position = "relative";
              
              //subsequent calls to fixPNGs will erroneously reprocess this image. Marking it here
              img.setAttribute("previouslyProcessed", true); 
              
            } else {
              // absent script events this is by far the most robust approach.
              var imgID = (img.id) ? "id='" + img.id + "' " : ""
              var imgClass = (img.className) ? "class='" + img.className + "' " : ""
              var imgTitle = (img.title) ? "title='" + img.title + "' " : "title='" + img.alt + "' "
              var imgStyle = "display:inline-block;" + img.style.cssText 
              if (img.align == "left") imgStyle = "float:left;" + imgStyle
              if (img.align == "right") imgStyle = "float:right;" + imgStyle
              if (img.parentElement.href) imgStyle = "cursor:hand;" + imgStyle
              var strNewHTML = "<span " + imgID + imgClass + imgTitle
              + " style=\"" + "width:" + img.width + "px; height:" + img.height + "px;" + imgStyle + ";"
              + "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
              + "(src=\'" + img.src + "\', sizingMethod='scale');\"></span>" 
              img.outerHTML = strNewHTML;
              i = i-1; //one less image in the collection, stay at the same position in the loop
            }
          }
       }
       
       //search for all containers (div,span,td, etc) with a PNG backgroundImage
       var list = searchForBGs([], document.body);
       for(var i=0; i<list.length;  i++){
           fixStyle(list[i].style);
       }

       //Search defined classes in all stylesheets for PNG background images
        for(var i=0; i<document.styleSheets.length; i++){
            var sheet = document.styleSheets[i].rules;
            for(var y=0; y<sheet.length; y++){
                var background = sheet[y].style.backgroundImage;
                if(!background){
                    continue;
                }
                if(background.toLowerCase().indexOf(".png") > -1){
                    fixStyle(sheet[y].style);
                }
            }
        }

    }

}

/*==================================================================
Recurse to find all elements that have a style with a PNG backgroundImage
====================================================================*/
function searchForBGs(list, node){
  
  if(node.style 
      && node.style.backgroundImage 
      && node.style.backgroundImage.toLowerCase().indexOf(".png") > -1
      && ! node.getAttribute("previouslyProcessed")
      ){
    list[list.length] = node;
  }
  for(var i=0; i<node.childNodes.length; i++){
    list = searchForBGs(list, node.childNodes[i]);
  }
  return list;
}

/*==================================================================
For the given style, replace the backgroundImage with the AlphaImageLoader Filter.
====================================================================*/
function fixStyle(style){
    if(style.backgroundPosition != "" && style.backgroundPosition.toLowerCase() != "top left" && style.backgroundPosition.toLowerCase() != "left top"){
        //It's not possible
        return;
    }
    var background = style.backgroundImage;
   if(background.indexOf("url(") > -1){
        background = background.substring(background.indexOf("url(")+4, background.indexOf(")"));
   }
   background.replace("'","");
   background.replace("\"","");
   var repeat = style.backgroundRepeat;
   var sizingMethod = (repeat && repeat.toLowerCase() != "no-repeat")? "scale":"image";
   style.backgroundImage = "";
   //style.position = "relative";
   style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\'" + PngFix.baseURL + background + "\', sizingMethod='"+sizingMethod+"')";
}