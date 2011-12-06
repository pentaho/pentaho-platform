package org.pentaho.platform.web.http.api.resources.services;

public class Choice {

	private String descriptionKey = null;
	private String title = null;
	private String value = null;
	
	public String getDescriptionKey() {
		return descriptionKey;
	}
	public void setDescriptionKey(String descKey) {
		this.descriptionKey = descKey;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String asXml() {
		StringBuffer xml = new StringBuffer();
		xml.append( "<choice" ); //$NON-NLS-1$
		if( descriptionKey != null ) {
			xml.append( " descKey=\"" ).append( descriptionKey ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( title != null ) {
			xml.append( " title=\"" ).append( title ).append( "\"" );  //$NON-NLS-1$//$NON-NLS-2$
		}
		xml.append( ">" ); //$NON-NLS-1$
		xml.append( value );
		
		xml.append( "</choice>\n" ); //$NON-NLS-1$

		return xml.toString();

	}

}
