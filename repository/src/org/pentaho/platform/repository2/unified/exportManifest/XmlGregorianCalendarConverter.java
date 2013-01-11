package org.pentaho.platform.repository2.unified.exportManifest;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

	/**
	 * Utility class for converting between XMLGregorianCalendar and java.util.Date
	 */
	public class XmlGregorianCalendarConverter {  

	    /**
	     * Needed to create XMLGregorianCalendar instances
	     */
	    private static DatatypeFactory df = null;
	    static {
	        try {
	            df = DatatypeFactory.newInstance();
	        } catch (DatatypeConfigurationException e) {
	            throw new IllegalStateException(
	                "Exception while obtaining DatatypeFactory instance", e);
	        }
	    }  

	    /**
	     * Converts a java.util.Date into an instance of XMLGregorianCalendar
	     *
	     * @param date Instance of java.util.Date or a null reference
	     * @return XMLGregorianCalendar instance whose value is based upon the
	     *  value in the date parameter. If the date parameter is null then
	     *  this method will simply return null.
	     */
	    public static XMLGregorianCalendar asXMLGregorianCalendar(java.util.Date date) {
	        if (date == null) {
	            return null;
	        } else {
	            GregorianCalendar gc = new GregorianCalendar();
	            gc.setTimeInMillis(date.getTime());
	            return df.newXMLGregorianCalendar(gc);
	        }
	    }

	    /**
	     * Converts an XMLGregorianCalendar to an instance of java.util.Date
	     *
	     * @param xgc Instance of XMLGregorianCalendar or a null reference
	     * @return java.util.Date instance whose value is based upon the
	     *  value in the xgc parameter. If the xgc parameter is null then
	     *  this method will simply return null.
	     */
	    public static java.util.Date asDate(XMLGregorianCalendar xgc) {
	        if (xgc == null) {
	            return null;
	        } else {
	            return xgc.toGregorianCalendar().getTime();
	        }
	    }
	}
