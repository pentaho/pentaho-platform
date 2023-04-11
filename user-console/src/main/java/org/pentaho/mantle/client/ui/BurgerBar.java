package org.pentaho.mantle.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import org.pentaho.gwt.widgets.client.buttons.ThemeableImageButton;

import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;

public class BurgerBar extends SimplePanel {


  public BurgerBar() {
    super();

    ThemeableImageButton burgerButton = new ThemeableImageButton( (String[]) null, null, "Menu" );
    burgerButton.setUrl( GWT.getModuleBaseURL() + "themes/ruby/images/burgerButton.png" );
    Element bbElem = burgerButton.getElement();
    bbElem.setId( "burgerButton" );
    bbElem.setAttribute( "style", "display: block;" );
    this.setWidget( burgerButton );
  }

  public static void injectBurgerScript() {
    String burgerScript = "function burgerClick(){\n"
      + "  var pucMenuBar = document.getElementById('pucMenuBar');\n"
      + "  (pucMenuBar.style.display == 'none') ? pucMenuBar.style.display='block' : pucMenuBar.style.display='none';\n"
      + "}\n"
      + "\n"
      + "function burgerMode(x){\n"
      + "  if(x.matches){\n"
      + "    document.getElementById('pucMenuBar').style.display = 'none';\n"
      + "    document.getElementById('burgerButton').style.display = 'block';\n"
      + "  }else{\n"
      + "    document.getElementById('pucMenuBar').style.display = 'block';\n"
      + "    document.getElementById('burgerButton').style.display = 'none';\n"
      + "  }\n"
      + "}\n"
      + "var x = window.matchMedia(\"(max-height: 500px), (max-width: 650px)\");\n"
      + "x.addListener(burgerMode);\n"
      + "burgerMode(x);\n"
      + "\n"
      + "document.getElementById('burgerButton').setAttribute( \"onClick\", \"burgerClick();\" );\n"
      + "\n"
      + "function testFunction(){\n"
      + "  var menuBarPopups = document.getElementsByClassName('gwt-MenuBarPopup');\n"
      + "  if( menuBarPopups.length > 0 ) {\n"
      + "    menuBarPopups[menuBarPopups.length - 1].remove();\n"
      + "  }\n"
      + "}";

    ScriptInjector.fromString( burgerScript ).setWindow( TOP_WINDOW ).setRemoveTag( false ).inject();
  }
}
