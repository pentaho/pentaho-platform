/*
 * Copyright 2006 - 2012 Pentaho Corporation. All Rights Reserved.
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

/*
 * this method is called by gwt during init of the schedule/background param view
 */
window.initSchedulingParams = function(filepath, callback) {
	callback(true);
}

/*
 * this method is called by gwt to retrieve the schedule/background param vals
 */
window.getParams = function(suppressAlerts) {
	try {
		var id = null;
		
		if (typeof pentaho_formId !== 'undefined') {
			id = pentaho_formId;
		}		
		
		var params = new Array();
		var form = document.forms['form_'+id];
		
		if (form == null && document.forms.length > 0) {
			form = document.forms[0];
		}
		
		var elements = form.elements;
		var i;
	
		var gotOne = 0;
		var lastName = null;
		var element;
		var str;
		for( i=0; i < elements.length; i++ ) {
	
			if( elements[i].name != lastName ) {
				if( lastName != null && lastName.length > 0) {
					var ckRtn = checkParams( form, element.type, lastName, gotOne, suppressAlerts );
					if( ckRtn == 0 ) {
						if (suppressAlerts) {
							continue;
						} else {
							throw 'Parameter Check Failed.';
						}
					}
				}
				gotOne = 0;
				str = '';
			}
			element = elements[i];
			lastName = element.name;
			if( element.type == 'select-one') {
				if( element.selectedIndex > 0 ) {
					addToParams(params, element.name, element.value);
					gotOne++;
				}
			} else if( element.type == 'button') {
				gotOne++;
			} else if( element.type == 'text') {
				if( element.value != '' ) {
					addToParams(params, element.name, element.value);
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
					addToParams(params, element.name, element.value);
				}
				gotOne++;
			} else if( element.type == 'radio' ) {
				if( element.checked ) {
					addToParams(params, element.name, element.value);
					gotOne++;
				}
			} else if( element.type == 'checkbox' ) {
				if( element.checked ) {
					addToParams(params, element.name, element.value);
					gotOne++;
				}
			} else if( element.type == 'select-multiple' ) {
				var options = element.options;
				var j;
				for( j=0; j!=options.length; j++ ) {
					if( options[j].selected && options[j].value != '') {
						addToParams(params, element.name, element.value);
						gotOne++;
					}
				}
			}
		}

		var ckRtn2 = checkParams( form, element.type, lastName, gotOne, suppressAlerts );
		if( ckRtn2 == 0 ) {
			return 'Parameter Check Failed.';
		} else if (ckRtn2 == 2) {
			addToParams(params, lastName, '');
		}
	
		return params;
	} catch (e) {
		alert(e);
		throw e;
	}
}

function addToParams(params, key, value) {
	if (key in params) {
		var orig = params[key];
		if (typeof orig == "Array") {
			params[key].push(value);
		} else {
			params[key] = [orig, value];
		}
	} else {
		params[key] = value;
	}
}

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
			
/* this is an almost exact copy of ScheduleParamsWizardPanel.java getParams() in pentaho-gwt-widgets */
function getParamEntries(params) {
    var paramEntries = new Array();
    for (var key in params) {
      var type = null;
      var value = new Array();
      if (Object.prototype.toString.apply(params[key]) === '[object Array]') {
        var theArray = params[key];
        if (theArray.length > 0) {
           for(var i=0; i < theArray.length; i++) {
            if (typeof theArray[i] == 'number') {
              if (type == null) {
                type = "number[]";
              }
              value.push('' + theArray[i]);
            } else if (typeof theArray[i] == 'boolean') {
              if (type == null) {
                type = "boolean[]";
              }
              value.push(theArray[i] ? "true" : "false");
            } else if (typeof theArray[i] instanceof Date) {
              if (type == null) {
                type = "date[]";
              }
              value.push(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(theArray[i]));
            } else if (typeof theArray[i] == 'string') {
              if (type == null) {
                type = "string[]"
              }
              value.push(theArray[i]);
            } else if (theArray[i] == null) {
              value.push(null);
            }     
          }
        }
      } else if (typeof params[key] == 'number') {
        type = "number";
        value.push('' + params[key]);
      } else if (typeof params[key] == 'boolean') {
        type = "boolean";
        value.push(params[key] ? "true" : "false");
      } else if (params[key] instanceof Date) {
        type = "date";
        value.push(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(params[key]));
      } else if (typeof params[key] == 'string') {
        type = "string";
        value.push(params[key]);
      }
      if (type != null) {
        paramEntries.push({
          name: key,
          stringValue: value,
          type: type
        });
      }
    }
    return paramEntries;
}

/**
 * this method submits a application/json post to the server
 * it utilizes api calls in pentaho-ajax.js.
 */
function doPost( url, query, func) {	
	var http_request = null;
	// create an HTTP request object
	if (window.XMLHttpRequest) { // Mozilla, Safari,...
		http_request = new XMLHttpRequest();
	} else if (window.ActiveXObject) { // IE
		try {
			http_request = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				http_request = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) 
			{
				http_request = null;
			}
		}
	}
	if (!http_request) {
		throw new Error('Cannot create XMLHTTP instance');
	}
  
  // set the callback function
  http_request.onreadystatechange = function() { pentahoResponse(http_request, func); };

  // submit the request
  http_request.open('POST', url, true);
  http_request.setRequestHeader("Content-type", "application/json");
  http_request.setRequestHeader("Content-length", query.length);
  http_request.setRequestHeader("Connection", "close");
  http_request.send(query);
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

	if (background == true) {
		
		// this code submits a run in background ajax request 
		
		var params = window.getParams();
		var json = {};
		json.inputFile = params['path']; // createPath(params['solution'], params['path'], params['action']);
		
		// delete params['solution'];
		// delete params['path'];
		// delete params['action'];
		
		json.outputFile = null;
		json.simpleJobTrigger = {repeatInterval:0, repeatCount:0, startTime:null, endTime:null};
		json.jobParameters = getParamEntries(params);
		
		doPost('../../../api/scheduler/job', JSON.stringify(json), function() { 
			alert('This action has been successfully scheduled to run.  Check your workspace for the status of this job.');
		});

		return true;
	}
	
	var params = getParameters( id );
	if( params == null ) {
		return false;
	}
	// this is set in the xsl file
	if (!USEPOSTFORFORMS) {
		submitUrl += params;
		return executeAction(target, submitUrl);
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

function checkParams(form, type, lastName, gotOne, suppressAlerts ) {
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
		if (!suppressAlerts) {
		    var msg = pentaho_notOptionalMessage;
			var msg = convertHtmlEntitiesToCharacters(msg.replace("{0}", lastName));
			alert( msg );
		}
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
				var ckRtn = checkParams( form, element.type, lastName, gotOne, false );
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
	var ckRtn2 = checkParams( form, element.type, lastName, gotOne, false );
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
