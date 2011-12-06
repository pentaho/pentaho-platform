package org.pentaho.platform.web.http.api.resources.services;

import java.util.ArrayList;
import java.util.List;

public class ExtendedAttributes {

	public enum Type { CONFIG, TEST, DIAGNOSTIC, METRIC, OPERATION, URL, IMG_URL, FORM };
	
	public enum Status { NONE, OK, WARNING, FAILURE };

	public enum Frequency { STATIC, VOLATILE };
	
	private Type type = Type.CONFIG;
	
	private Status status = Status.NONE;
	
	private Frequency frequency = Frequency.STATIC;
	
	private String title = null;
	
	private String comment = null;
	
	private String attrId;
	
	private String beanId;
	
	private List<StatusMap> statusMaps = new ArrayList<StatusMap>();

	/* for future use...

	private String help;
	
	private List<String> dependencyIds = new ArrayList<String>(); // TODO provide a way to define dependencies
	
	private String fixupId; // Id of the setting or operation that will fix up this setting

	private List<String> testIds = new ArrayList<String>(); // Ids of the operations that will test this setting
*/
	private Choices choices = null;
	
	private List<Parameter> params = new ArrayList<Parameter>();
	
	public ExtendedAttributes( String id ) {
		this.attrId = id;
	}
	
	public List<Parameter> getParameters() {
		return params;
	}

	public void addStatusMap( Status status, String value ) {
		addStatusMap( new StatusMap( status, value ) );
	}
	
	public void addStatusMap( StatusMap statusMap ) {
		statusMaps.add( statusMap );
	}
	
	public Status getStatusForValue( String value ) {
		for( StatusMap map: statusMaps ) {
			if( map.value.equals( value ) ) {
				return map.status;
			}
		}
		return null;
	}
	
	/* for future use...
	public void addDependency( String id ) {
		dependencyIds.add( id );
	}
	
	public void addTest( String id ) {
		testIds.add( id );
	}
	
	public List<String> getDependencies() {
		return dependencyIds;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencyIds = dependencies;
	}

	public String getFixupId() {
		return fixupId;
	}

	public void setFixupId(String fixupId) {
		this.fixupId = fixupId;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public List<String> getTestIds() {
		return testIds;
	}

	public void setTestIds(List<String> testIds) {
		this.testIds = testIds;
	}
*/
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String asXml() {
		StringBuffer xml = new StringBuffer();
		String string = "<ext id=\""; //$NON-NLS-1$
		xml.append( string )
		.append( beanId )
		.append( "." ) //$NON-NLS-1$
		.append( attrId )
		.append( "\" type=\"" ) //$NON-NLS-1$
		.append( type.toString().toLowerCase() )
		.append( "\" frequency=\"" ) //$NON-NLS-1$
		.append( frequency.toString().toLowerCase() )
		.append( "\" status=\"" ) //$NON-NLS-1$
		.append( status.toString().toLowerCase() )
		.append( "\">\n" ); //$NON-NLS-1$
		
		if( title != null ) {
			xml.append( "<title><![CDATA[" ); //$NON-NLS-1$
			xml.append( title );
			xml.append( "]]></title>\n"); //$NON-NLS-1$
		}
		if( comment != null ) {
			xml.append( "<comment><![CDATA[" ); //$NON-NLS-1$
			xml.append( comment );
			xml.append( "]]></comment>\n"); //$NON-NLS-1$
		}
		if( choices != null ) {
			xml.append( choices.asXml() );
		}
		if( params.size() > 0 ) {
			xml.append( "<params>" ); //$NON-NLS-1$
			int idx = 1;
			for( Parameter param: params ) {
				xml.append( param.asXml( idx ) );
				idx++;
			}
			xml.append( "</params>" ); //$NON-NLS-1$
		}
		
		for( StatusMap statusMap: statusMaps ) {
			xml.append( statusMap.asXml() );
		}
		
		xml.append( "</ext>\n" ); //$NON-NLS-1$

		return xml.toString();
	}

	public String getId() {
		return attrId;
	}

	public void setId(String id) {
		this.attrId = id;
	}

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	public Choices getChoices() {
		return choices;
	}

	public void setChoices(Choices choices) {
		this.choices = choices;
	}
	
	public void addParameter( Parameter parameter ) {
		params.add( parameter );
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
