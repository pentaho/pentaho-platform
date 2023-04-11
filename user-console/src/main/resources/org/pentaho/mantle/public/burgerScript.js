function burgerClick(){
  var pucMenuBar = document.getElementById('pucMenuBar');
  (pucMenuBar.style.display == 'none') ? pucMenuBar.style.display='block' : pucMenuBar.style.display='none';
}

function burgerMode(x){
  var perspectiveSwitcher = document.getElementById('mantle-perspective-switcher');
  var mainMenubar = document.getElementById('mainMenubar');
  var pucPerspectives = document.getElementById('pucPerspectives');

  if(x.matches){
    document.getElementById('pucMenuBar').style.display = 'none';
    document.getElementById('burgerButton').style.display = 'block';
    mainMenubar.appendChild(perspectiveSwitcher);
  }else{
    document.getElementById('pucMenuBar').style.display = 'block';
    document.getElementById('burgerButton').style.display = 'none';
    pucPerspectives.appendChild(perspectiveSwitcher);
  }
}
var x = window.matchMedia("(max-height: 500px), (max-width: 650px)");
x.addListener(burgerMode);
burgerMode(x);

document.getElementById('burgerButton').setAttribute( "onClick", "burgerClick();" );