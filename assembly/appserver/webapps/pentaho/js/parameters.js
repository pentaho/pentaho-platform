/*
 * Copyright 2006 - 2010 Pentaho Corporation. All Rights Reserved.
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
*/
// used in getParameters and doClearIgnoreFields to ignore specific hidden fields

var pentaho_ignoreFields = new Array();
var pentaho_ignoreIndexOfFields = new Array();
var pentaho_optionalParams = new Array();
var pentaho_paramName = new Array();

function dynExec(funcName, formName, objectName) {
	var func = funcName + formName + "('" + objectName + "');";
	var rtn = false;
	try {
		rtn = eval(func);
	} catch (ignored) {
		rtn = false;
	}

	return rtn;
}

function setXFormsValue(formName, objectName) {
	// Will cause invoke of setXFormsValueformname(object);
	return dynExec("setXFormsValue", formName, objectName);
}

function activate(formName, objectName) {
	// Will cause invoke of activateformname(object);
	return dynExec("activate", formName, objectName);
}

function upload(formName, objectName) {
	// Will cause invoke of uploadformname(object);
	return dynExec("upload", formName, objectName);
}

function doClearIgnoreFields(form1, form2) {

	var theForms = new Array(form1, form2);	
	for (i=0; i < theForms.length; i++) {
		if ( theForms[i] != null ) {
			for (j=0; j < pentaho_ignoreFields.length; j++) {
				var tmp = theForms[i].elements[pentaho_ignoreFields[j]];
				if (tmp != null) {
					tmp.value = '';
				}
			}
		}
	}
}
			
function doRun( id, baseUrl, target, background ) {
	// ----------------------------------------------------------
	// change this URL to point to another machine if required...
	// ----------------------------------------------------------
	var submitUrl = baseUrl;
	// delete line? var action = submitUrl;
	var formCheck = false;
	try {
		formCheck = doFormCheck( id );
	} catch (e) {
		formCheck = true;
	}
	if (!formCheck) {		
		return false;
	}
	var params = getParameters( id );
	if( params == null ) {
		return false;
	}
	// this is set in the xsl file
	if (!USEPOSTFORFORMS) {
		submitUrl += params;
		if( background == true ) {
			submitUrl += '&background=true';
			var mantle_enabled = window.parent != null && window.parent.mantle_initialized == true;
			if (mantle_enabled == true) {
				window.top.mantle_confirmBackgroundExecutionDialog(submitUrl);
			} else {
				// the old way
				var confirmMsg = convertHtmlEntitiesToCharacters(pentaho_backgroundWarning);
				if(!confirm (confirmMsg)) {
					return false;
				}
				return executeAction(target, submitUrl);
			}
		} else {
			// the old way
			return executeAction(target, submitUrl);
		}					
	} else {
		var form = document.forms[ 'form_'+id ];
		var form2 = document.forms['save_'+id];		
		doClearIgnoreFields(form, form2);

		form.submit();
		
		try {
			// Any after submit cleanup...
			var mthName = "doAfterSubmit"+id+"();";
			eval(mthName);
		} catch (ignored) {
		}
	}
}

// Called internally only
function executeAction (target, submitUrl) {
	if( target == '' ) {
		document.location.href=submitUrl;
	} else {
		window.open( submitUrl, target, '' );
	}
	return false;
}

// convert characters from entities like &#305; to display characters (HTML)
function convertHtmlEntitiesToCharacters(theStr) {
    var newDiv = document.createElement(newDiv);
    newDiv.innerHTML = theStr;
    return newDiv.innerHTML;
}

function checkParams(form, type, lastName, gotOne ) {
    // pentaho_optionalParams is defined in the XSL file
	if( gotOne == 0 && type != 'hidden' ) {
		try {
			var found = false;
			for (var i = 0; i < pentaho_optionalParams.length; i++) {
				if (pentaho_optionalParams[i] == form.name + '.' + lastName) {
					found = true;
				}
			}
			if (found) {
				form.elements[lastName].value = ''; // Poke in empty string for the post
				return 2;
			}
		} catch (e) {
		}		
		var msg = convertHtmlEntitiesToCharacters(pentaho_notOptionalMessage.replace("{0}", pentaho_paramName[form.name + '.' + lastName] ));
		alert( msg );
		return 0;
	}
	return 1;
}

function getParameters( id ) {
	var params = '';
	var form = document.forms['form_'+id];
	var elements = form.elements;
	var i;

	var gotOne = 0;
	var lastName = null;
	var element;
	var str;
	for( i=0; i < elements.length; i++ ) {

		if( elements[i].name != lastName ) {
			if( lastName != null && lastName.length > 0) {
				var ckRtn = checkParams( form, element.type, lastName, gotOne );
				if( ckRtn == 0 ) {
					return null;
				}
			}
			gotOne = 0;
			str = '';
		}
		element = elements[i];
		lastName = element.name;
		if( element.type == 'select-one') {
			if( element.selectedIndex > 0 ) {
				params += '&' + element.name + '=' + escape( element.value );
				gotOne++;
			}
		} else if( element.type == 'button') {
			gotOne++;
		} else if( element.type == 'text') {
			if( element.value != '' ) {
				params += '&' + element.name + '=' + escape( element.value );
				gotOne++;
			}
		} else if( element.type == 'hidden') {
			// make sure field isn't ignored first before adding it to the list.
			var found = false;
			for (var j = 0; j < pentaho_ignoreFields.length; j++) {
				if (element.name == pentaho_ignoreFields[j]) {
					found = true;
				}
			}
			
			for (var j = 0; j < pentaho_ignoreIndexOfFields.length; j++) {
				if (element.name.indexOf(pentaho_ignoreIndexOfFields[j]) >= 0) {
					found = true;
				}
			}
			
			if (!found) {
					params += '&' + element.name + '=' + escape( element.value );
			}
			gotOne++;
		} else if( element.type == 'radio' ) {
			if( element.checked ) {
				params += '&' + element.name + '=' + escape( element.value );
				gotOne++;
			}
		} else if( element.type == 'checkbox' ) {
			if( element.checked ) {
				params += '&' + element.name + "=" + escape( element.value );
				gotOne++;
			}
		} else if( element.type == 'select-multiple' ) {
			var options = element.options;
			var j;
			for( j=0; j!=options.length; j++ ) {
				if( options[j].selected && options[j].value != '') {
					params += '&' + element.name + '=' + escape( options[ j ].value );
					gotOne++;
				}
			}
		}
	}
	var ckRtn2 = checkParams( form, element.type, lastName, gotOne );
	if( ckRtn2 == 0 ) {
		return null;
	} else if (ckRtn2 == 2) {
		params += '&' + lastName + '=';
	}
	// Fix BISERVER-381
    if (params != null) {
      return params.substring(1);
    } else {
      return params;
    }
}


function closeMantleTab(){
  try{
    window.parent.closeTab('');
  } catch(e){
    alert("error closing tab: "+e);
  }
}
