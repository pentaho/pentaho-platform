
var docHead = document.getElementsByTagName("head")[0];

if(window.location.href.indexOf("theme=") > -1){
  var startIdx = window.location.href.indexOf("theme=")+("theme=".length);
  var endIdx = window.location.href.indexOf("&", startIdx) > -1 ? window.location.href.indexOf("&", startIdx) : window.location.href.length;
  active_theme = window.location.href.substring(startIdx, endIdx);
}

var originalOnLoad = window.onload;
window.onload = function () {
  if (originalOnLoad) {
    originalOnLoad();
  }
  customizeThemeStyling();
}

if(window.core_theme_tree){
  includeResources(core_theme_tree);
}

if(window.module_theme_tree){
  includeResources(module_theme_tree);
}

function includeResources(resourceTree){
  if(!resourceTree || !resourceTree[active_theme]){
    return;
  }
  var cssPat = /\.css$/;
  for(var i=0; i<resourceTree[active_theme].resources.length; i++){
    var baseName = resourceTree[active_theme].resources[i];
    var selectedTheme = active_theme;
  
    if(cssPat.test(baseName)){
      
      document.write("<link rel='stylesheet' type='text/css' href='"+CONTEXT_PATH + resourceTree[selectedTheme].rootDir +baseName+ "'/>");
      
    } else {
      
      document.write("<script type='text/javascript' src='"+CONTEXT_PATH + resourceTree[selectedTheme].rootDir +baseName+ "'></script>");
    }
  }
}

function customizeThemeStyling() {
  // if it is IE, inject an IE class to the body tag to allow for custom IE css by --> .IE .myclass {}
  if (document.all) {
    document.getElementsByTagName("body")[0].className += " IE";
  }
}