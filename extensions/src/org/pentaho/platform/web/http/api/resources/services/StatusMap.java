package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.web.http.api.resources.services.ExtendedAttributes.Status;

public class StatusMap {

	String value;
	Status status;
	
	public StatusMap( Status status, String value ) {
		this.status = status;
		this.value = value;
	}
	
	public String asXml() {
		StringBuffer xml = new StringBuffer();
		xml.append( "<status value=\"" ) //$NON-NLS-1$
		.append( value ).
		append( "\">" ); //$NON-NLS-1$
		
		if( status == Status.OK ) {
			xml.append( "ok" ); //$NON-NLS-1$
		} else if( status == Status.FAILURE ) {
			xml.append( "error" ); //$NON-NLS-1$
		} else if( status == Status.WARNING ) {
			xml.append( "warning" ); //$NON-NLS-1$
		}
		xml.append( "</status>\n" ); //$NON-NLS-1$

		return xml.toString();

	}

}
