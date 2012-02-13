/*
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
 * Copyright 2012 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.http.api.resources;

import java.util.ArrayList;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

	private JAXBContext context;
	@SuppressWarnings("rawtypes")
	private ArrayList<Class> types = new ArrayList<Class>();
	private ArrayList<String> arrays = new ArrayList<String>();
	
	public JAXBContextResolver() throws Exception {
		types.add(ArrayList.class);
		types.add(JaxbList.class);
		arrays.add("list");
		arrays.add("values");
		JSONConfiguration config = JSONConfiguration.mapped().rootUnwrapping(true).arrays(arrays.toArray(new String[]{})).build();
		context = new JSONJAXBContext(config, types.toArray(new Class[] {}));
	}

	public JAXBContext getContext(Class<?> objectType) {
		if (types.contains(objectType)) {
			return context;
		}
		arrays.add(objectType.getSimpleName().toLowerCase());
		types.add(objectType);
		try {
			JSONConfiguration config = JSONConfiguration.mapped().rootUnwrapping(true).arrays(arrays.toArray(new String[]{})).build();
			context = new JSONJAXBContext(config, types.toArray(new Class[] {}));
			return getContext(objectType);
		} catch (JAXBException e) {
		}
		return null;
	}
	
}