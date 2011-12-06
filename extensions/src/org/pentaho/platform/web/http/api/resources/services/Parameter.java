package org.pentaho.platform.web.http.api.resources.services;

public class Parameter {

	private String type = null;
	private String descriptionKey = null;
	private String title = null;
	private Choices choices = null;
	private String defaultValue = null;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescriptionKey() {
		return descriptionKey;
	}
	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Choices getChoices() {
		return choices;
	}
	public void setChoices(Choices choices) {
		this.choices = choices;
	}
	
	public String asXml( int idx ) {
		StringBuffer xml = new StringBuffer();
		xml.append( "<param" ); //$NON-NLS-1$
		xml.append( " name=\"p" ).append( idx ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		if( type != null ) {
			xml.append( " type=\"" ).append( type ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( descriptionKey != null ) {
			xml.append( " descKey=\"" ).append( descriptionKey ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if( title != null ) {
			xml.append( " title=\"" ).append( title ).append( "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( defaultValue != null ) {
			xml.append( " defaultValue=\"" ).append( defaultValue ).append( "\"" );  //$NON-NLS-1$//$NON-NLS-2$
		}
		xml.append( ">" ); //$NON-NLS-1$
		if( choices != null ) {
			xml.append( choices.asXml() );
		}
		
		xml.append( "</param>\n" ); //$NON-NLS-1$

		return xml.toString();

	}
}
