package org.pentaho.platform.web.http.api.resources.services;


import java.util.ArrayList;
import java.util.List;

public class Choices {

	private List<Choice> choices = new ArrayList<Choice>();
	
	public List<Choice> getChoices() {
		return choices;
	}
	
	public void addChoice( String value, String title, boolean useMessages ) {
		Choice choice = new Choice();
		choice.setValue( value );
		if( useMessages) {
			choice.setDescriptionKey( title );
		} else {
			choice.setTitle( title );
		}
		choices.add( choice );
	}
	
	public String asXml() {
		StringBuffer xml = new StringBuffer();
		xml.append( "<choices>" ); //$NON-NLS-1$
		for( Choice choice: choices ) {
			xml.append( choice.asXml() );
		}
		xml.append( "</choices>\n" ); //$NON-NLS-1$

		return xml.toString();

	}
	
}
