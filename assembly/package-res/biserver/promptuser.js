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
