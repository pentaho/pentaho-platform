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
// add the subscription specific ignored hidden fields used by getParameters
pentaho_ignoreFields.push('subscribe-name');
pentaho_ignoreFields.push('subscribe');
pentaho_ignoreFields.push('pentaho-subscriptionmessage');
pentaho_ignoreFields.push('subscribe-id');
pentaho_ignoreFields.push('_PENTAHO_ADDITIONAL_PARAMS_');
pentaho_ignoreFields.push('run_as_background_yes');
pentaho_ignoreFields.push('run_as_background_no');

pentaho_ignoreIndexOfFields.push('schedule-');

/* this function is currently not called */
function doParameterFormDisplay( id ) {
	// This is the initialization function that should get called on form startup	
	// Check for subscription action message...

	var form = document.forms['form_'+id];
	var subsMsgObj = form.elements['pentaho-subscriptionmessage'];
	if (subsMsgObj != null) {
		try {
			var sourceMsg = subsMsgObj.value;
			if (sourceMsg.length > 0) {
				var sourceMessages = new Array("The subscription could not be created",
											   "Subscription saved/created",
											   "Subscription could not be deleted",
											   "Subscription deleted",
											   "Successfully deleted",
											   "archived report could not be found",
											   "The subscription does not exist",
											   "The content item does not exist",
											   "The archived item was deleted",
											   "The output could not be created",
											   "The archived item could not be created",
											   "The archived item was saved. To see the new archived item in your web browser, you need to close this window, and refresh your portal window.",
											   "You must login before saving",
											   'The subscription named `junk` already exists.');
				var destinationMessages = new Array("The report could not be created",
													"The report was saved.",
													"The saved report could not be deleted",
													"The saved report was deleted",
													"Successfully deleted",
													"The archived report could not be found",
													"The saved report does not exist",
													"The content item does not exist",
													"The archived item was deleted",
													"The output could not be created",
													"The archived item could not be created",
													"The archived item was saved. To see the new archived item in your web browser, you need to close this window, and refresh your portal window.",
													"You must login before saving",
													'The saved report named `junk` already exists.');
				var destMsg = sourceMsg;
				for (i=0; i < sourceMessages.length; i++) {
					if (sourceMessages[i].indexOf('`') > 0 ) {
						var srcAray = sourceMsg.split('`');
						var destAray = destinationMessages[i].split('`');
						try {
							destMsg = destAray[0] + '`' + srcAray[1] + '`' + destAray[2];
						} catch (ignored) {
							destMsg = sourceMsg;
						}
					} else if (sourceMessages[i] == sourceMsg) {
						destMsg = destinationMessages[i];
						break;
					}
				}
				alert(destMsg);
				// subsMsgObj.value = '';
			}
		} catch (ignored) {
		}
	}
}

function doClearEditingFields(form1, form2) {

	var theForms = new Array(form1, form2);	
	var theFields = new Array('editing', 'subscribe-title', 'destination', 'subscribe-id', 'subscribe', 'subscribe-name');
	for (i=0; i < theForms.length; i++) {
		if ( theForms[i] != null ) {
			for (j=0; j < theFields.length; j++) {
				var tmp = theForms[i].elements[theFields[j]];
				if (tmp != null) {
					if (theFields[j] == 'subscribe') {
						// not sure if this does anything.
						tmp = 'none';
					} else {
						tmp.value = '';
					}
				}
			}
		}
	}
}

function showSubscribe( id ) {
	try {
		document.getElementById('subscribe-div'+id ).style.display='block';
		document.getElementById('run2button'+id).style.display='none';
		document.getElementById('cancel1button'+id).style.display='none';
		document.getElementById('subscribe1button'+id).style.display='none';
		document.getElementById('subs1div'+id).style.display='none';
	} catch (e) {}
}

/* Changes by Marc to highlight the scheduling options */
function doCancelScheduling( id, cancelEditing) {
	try {
		document.getElementById('subscribe-div'+id ).style.display='none';
		document.getElementById('run3div'+id).style.display='block';
		document.getElementById('divSchMsg'+id).style.display='block';
		document.getElementById('run2div'+id).style.display='block';
		document.getElementById('run2button'+id).style.display='block';
		document.getElementById('cancel1button'+id).style.display='block';
		document.getElementById('subscribe1button'+id).style.display='block';
		document.getElementById('subs1div'+id).style.display='block';
		showRun( id ); // Set display to what it was when run was clicked.
		var form = document.forms['form_'+id];
		var form2 = document.forms['save_form_'+id];
		doClearEditingFields(form, form2);
		if (USEPOSTFORFORMS) {
			if (cancelEditing) {
				form2.target = '';
				form2.submit();
			}
		}
	} catch (e) {}
}

function doCancel( id ) {
	try {

		if( document.getElementById( 'subscribe-div'+id ) ) {
			document.getElementById( 'subscribe-div'+id ).style.display='none';
		}
		if( document.getElementById('run2button'+id) ) {
			document.getElementById('run2button'+id).style.display='block';
		}
		if( document.getElementById('cancel1button'+id) ) {
			document.getElementById('cancel1button'+id).style.display='block';
		}
		if( document.getElementById('subscribe1button'+id) ) {
			document.getElementById('subscribe1button'+id).style.display='block';
		}
		if( document.getElementById('run1div'+id) ) {
			document.getElementById('run1div'+id).style.display='block';
		}
		if( document.getElementById('run2div'+id) ) {
			document.getElementById('run2div'+id).style.display='none';
		}
		if( document.getElementById('run3div'+id) ) {
			document.getElementById('run3div'+id).style.display='none';
		}
		if( document.getElementById('divSchMsg'+id) ) {
			document.getElementById('divSchMsg'+id).style.display='none';
		}
		if( document.getElementById('subs1div'+id) ) {
			document.getElementById('subs1div'+id).style.display='block';
		}
		var form = document.forms['form_'+id];
		var form2 = document.forms['save_form_'+id];
		doClearEditingFields(form, form2);
	} catch (e) {}
}

//BISERVER-2409
//Take care of appending ? and ampersand in the url at appropriate locations
function modifyURL(url, appendStr) {
	// Basic assumption is appendStr is a string that does not start with ampersand (&)
	// If it does remove it.
	if (appendStr.substring(0,1)=="&") {
		appendStr = append.substring(1);
	}

	if ( (url.substring(url.length-1)=="?") || (url.substring(url.length-1)=="&") ) {
		// the url ends with a question mark (?) or an ampersand (&), so simply append the appendStr
		url += appendStr;
	} else if (url.indexOf('?') != -1) {
		// There is a question mark in the url, so append the url with & and appendStr
		url += "&" + appendStr;
	} else {
		// if the url does not have a question mark appended to it and it does not contain a question mark 
		// then append ? and the appendStr
		url += "?" + appendStr;
	}
	return url;
}

function doSave( id, url, createNew ) {
	var submitUrl = null;

	if (!USEPOSTFORFORMS) {
		submitUrl = modifyURL(url, 'subscribe=save');
	} else {
		submitUrl = url;
	}
	var formCheck = false;
	try {
		formCheck = doFormCheck(id);
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

	submitUrl = modifyURL(submitUrl, params);	
	var form = document.forms['save_form_'+id];
	var name = form.elements['subscribe-name'].value;
	if( name == '' ) {
		alert( 'You must enter a name for this subscription' );
		return false;	
	}
	// get the schedules
	var n;
	var hasSchedules = false;
	for( n=0; n<form.elements.length; n++ ) {
		var element = form.elements[ n ];
		if( (element.type == 'checkbox') && (element.id.indexOf( 'schedule-' ) == 0 ) && (element.checked == true) ) {
			hasSchedules = true;
			if (!USEPOSTFORFORMS) {
				submitUrl += '&'+element.id+'=true';
			} else {
				break;
			}
		}
	}
	/*
	TODO need to replace this with generic code
	*/
	var element = document.getElementById( 'monthly-schedule-selection-'+id );
	if( (element != null) && (element.selectedIndex > 0) ) {
		submitUrl += '&'+element.value+'=true';
		hasSchedules = true;
	}
	
 	var destination = form.elements['destination'];
      if (destination != null) {
        destination = destination.value;
      }
/*
	if( destination == '' && hasSchedules ) {
		alert( 'You must enter a destination email address' );
		return false;
	}
*/
	if( (element != null) && !hasSchedules ) {
		if (!confirm('You have not selected any days for this to be delivered. Do you want to continue saving this?')) {
			return false;
		}
	}

	var form1 = document.forms['form_'+id];
	if( !createNew ) {
		var id = form1.elements['subscribe-id'].value;
		submitUrl += '&subscribe-id='+ id;	
	} else {	
		var currentNamesDropDown = document.getElementById('subscription'+id);
		if (currentNamesDropDown != null) {
			for (i=0; i < currentNamesDropDown.length; i++) {
				var optObj = currentNamesDropDown.options[i];
				if (name == optObj.text) {
					alert('The saved report "' + name + '" already exists. Please choose another name.');
					return false;
				}
			}
		}
	}

	if (!USEPOSTFORFORMS) {
		submitUrl += '&subscribe-name='+escape(name);		
		submitUrl += '&destination='+escape(destination);
		document.location.href=submitUrl;
		return false;
	} else {
		// This is a workaround for getting form elements from the parameter page
		// into the postdata on the save_formXXXXX. You normally can't get data from
		// one form into the postdata on another form. But, since we're building up the
		// URL which was used for a get, we just put that URL in the postdata on the form.
		// The HttpRequestParameterProvider and PortletRequestParameterProvider will 
		// then look for this value in the request, parse out each value and add them
		// to the available input parameters map.
		form.elements['_PENTAHO_ADDITIONAL_PARAMS_'].value = submitUrl;

		form.elements['subscribe'].value = 'save';
		// document.location.href=submitUrl;
		// return false;
		form.target = '';
		form.action = url;
		form.submit();
		try {
			eval(mthName);
		} catch (ignored) {
		}
	}
}

function doSubscribed(id, actionUrl, displayUrl ) {
	var submitUrl = '';
	var action= document.getElementById('subscription-action'+id).value;
	var target='REPORTWINDOW';
	var options = '';
	var form = document.forms['save_form_'+id];
	if (!USEPOSTFORFORMS) {

		if( action == 'run' ) {
			submitUrl += actionUrl + 'subscribe=run';
		} else if( action == 'archive' ) {
			submitUrl += actionUrl + 'subscribe=archive';
			target = '_blank';
			options = 'toolbar=no,menubar=no,width=500,height=150,status=no';
		} else if( action == 'edit' ) {
			submitUrl += displayUrl + 'subscribe=edit';
			target='';
		} else if( action == 'delete' ) {
			submitUrl += actionUrl + 'subscribe=delete';
			target='';
		}

		var name= document.getElementById('subscription'+id).value;
		submitUrl += '&subscribe-name='+escape(name);
		if( target == '' ) {
			document.location.href=submitUrl;
		} else {
			window.open( submitUrl, target, options );
		}
		return false;
	} else {
		form.elements['_PENTAHO_ADDITIONAL_PARAMS_'].value = submitUrl;
	
		if( action == 'run' ) {
			form.action = 'ViewAction';
			form.elements['subscribe'].value='run';
		} else if( action == 'archive' ) {
			target = '_blank';
			options = 'toolbar=no,menubar=no,width=500,height=150,status=no';
			form.action='ViewAction';
			form.elements['subscribe'].value='archive';
		} else if( action == 'edit' ) {
			form.action = displayUrl;
			form.elements['subscribe'].value='edit';
			target='';
		} else if( action == 'delete' ) {
			var confirmMsg = 'Deleting this saved report will delete all archived reports this schedule was used to generate. Do you want to continue?';
			if( !confirm( confirmMsg ) ) {
				return false;
			}
			form.elements['subscribe'].value='delete';
			form.action=actionUrl;
			target='';
		}
	
		var name= document.getElementById('subscription'+id).value;
		form.elements['subscribe-name'].value = name;
		form.target = target;
		form.submit();
		try {
			// Any after submit cleanup...
			var form1 = document.forms['form_'+id];
			doClearEditingFields(form, form1);
			var mthName = "doAfterSubmit" + id + "();";
			eval(mthName);
		} catch (ignored) {
		}

	}	
}

function doSubscribedArchive( id, actionUrl ) {
	if (!USEPOSTFORFORMS) {
 		var submitUrl = '';
		var action= document.getElementById('subscription-archive-action'+id).value;
		var target='REPORTWINDOW';
		
		if( action == 'view' ) {
			// ----------------------------------------------------------
			// change this URL to point to another machine if required...
			// ----------------------------------------------------------
	
			submitUrl += 'ViewAction?subscribe=archived';
		}
		else 
		if( action == 'delete' ) {
			submitUrl += actionUrl + '&subscribe=delete-archived';
			target='';
		}
	
		var name= document.getElementById('subscription-archive'+id).value;
		submitUrl += '&subscribe-name='+escape(name);
		if( target == '' ) {
			document.location.href=submitUrl;
		} else {
			window.open( submitUrl, target, '' );
		}
		 return false;
	} else {
		var form = document.forms['save_form_'+id];
		form.elements['_PENTAHO_ADDITIONAL_PARAMS_'].value = submitUrl; // Clear out old postdata
		var action= document.getElementById('subscription-archive-action'+id).value;
		var formAction = 'ViewAction';
		var target='REPORTWINDOW';

		if( action == 'view' ) {
			// ----------------------------------------------------------
			// change this URL to point to another machine if required...
			// ----------------------------------------------------------
	
			form.elements['subscribe'].value='archived';
		} else if( action == 'delete' ) {
			formAction = actionUrl;		
			form.elements['subscribe'].value='delete-archived';
			target='';
		}
	
		var name= document.getElementById('subscription-archive'+id).value;
		form.elements['subscribe-name'].value = name;
		// submitUrl += '&subscribe-name='+escape(name);
		form.action = formAction;
		form.target = target;
		form.submit();
		try {
			// Any after submit cleanup...
			var mthName = "doAfterSubmit" + id + "();";
			eval(mthName);
		} catch (ignored) {
		}
	}
}

function showRun( id ) {
	try {	
		document.getElementById('run1div'+id).style.display='none';
		document.getElementById('run2div'+id).style.display='block';
		document.getElementById('run3div'+id).style.display='block';
		document.getElementById('divSchMsg'+id).style.display='block';
		document.getElementById('subs1div'+id).style.display='none';
		document.getElementById('run2button'+id).style.display='block';
		document.getElementById('cancel1button'+id).style.display='block';
	} catch (e) {
	}
	return false;
}

function rptnmlimit(field, maxlen) {
	if (field.value.length > maxlen) {
		alert("Report name can't exceed "+maxlen+" characters");
	}
	if (field.value.length > maxlen) {
		field.value = field.value.substring(0, maxlen);
	}
}
