var wshShell = WScript.CreateObject("WScript.Shell");
// popup with no timeout, ok and cancel buttons, and a question mark icon
var btnCode = wshShell.Popup("The Pentaho BI Platform now contains a version checker that will notify you \nwhen newer versions of the software are available. The version checker is enabled by default. \n\nFor information on what the version checker does, why it is beneficial, and how it works see: \nhttp://wiki.pentaho.com/display/ServerDoc2x/Version+Checker \n\nClick OK to continue, or Cancel to prevent the server from starting. \nYou will only be prompted once with this question.", 30, "Version Checker Warning", 1 + 32);
switch(btnCode) {
  case 1: // user clicked ok
    WScript.Quit(1);
    break;
  case 2: // user clicked cancel
    WScript.Quit(0);
    break;
  case -1: // timeout; no user selection
    WScript.Quit(1);
    break;
}