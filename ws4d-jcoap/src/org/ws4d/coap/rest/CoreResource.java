/* Copyright 2016 University of Rostock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ws4d.coap.messages.CoapMediaType;

/**
 * Well-Known CoRE support (rfc6690 - ietf-core-link-format)
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class CoreResource extends BasicCoapResource {
	/* use the logger of the resource server */
	private final static Logger logger = Logger.getLogger(CoapResourceServer.class);
	private ResourceServer server = null;
	
	//parameter
	private final static String uriPath = "/.well-known/core";
	private final static CoapMediaType mediaType = CoapMediaType.link_format;
	private int lastSize = -1;

	public CoreResource(ResourceServer server) {
		super(uriPath, null, mediaType);
		this.setReadable(true);
		this.setWritable(false);
		this.setDeletable(false);
		this.setObservable(false);
		this.server = server;
	}

	@Override
	public byte[] getValue() {
		return buildCoreString(null).getBytes();
	}
	
	@Override
	public byte[] getValue(List<String> queries) {
		return buildCoreString(queries).getBytes();
	}
	
	private String buildCoreString(List<String> queries) {
		//set up filters
		Set<String> rtFilter = new HashSet<String>();
		Set<String> ifFilter = new HashSet<String>();
		Set<String> hrefFilter = new HashSet<String>();
		
		if(null != queries){
			// each query parameter can contain spaces (encoded as '%20') to separate individual values
			// parameter values need to be split to test against them later
			for (String query : queries) {
				if (query.startsWith("rt=")) for(String part : query.substring(3).split("%20")){rtFilter.add(part);}
				if (query.startsWith("if=")) for(String part : query.substring(3).split("%20")){ifFilter.add(part);}
				if (query.startsWith("href=")) for(String part : query.substring(5).split("%20")){hrefFilter.add(part);}
			}
		}
		
		Map<String, CoapResource> resources = this.server.getResources();
		// used to optimize string builder behavior - '+' in the loop would render less optimal
		StringBuilder returnString = new StringBuilder();
		boolean first = true;
		for (CoapResource resource : resources.values()) {
			if (	matchFilter(rtFilter, resource.getResourceType()) && 
					matchFilter(ifFilter, resource.getInterfaceDescription()) &&
					matchFilter(hrefFilter, resource.getPath())) {
				
				// add ',' if this is not the first entry
				if (!first) {
					returnString.append(",");
				} else {
					first = false;
				}
				returnString.append("<");
				returnString.append(resource.getPath());
				returnString.append(">");
				String tmp = null;
				if ((tmp = resource.getResourceType()) != null) {
					returnString.append(";rt=\"");
					returnString.append(tmp);
					returnString.append("\"");
				}
				if ((tmp = resource.getInterfaceDescription()) != null) {
					returnString.append(";if=\"");
					returnString.append(tmp);
					returnString.append("\"");
				}
				if (resource.getSizeEstimate() > 1024) { //only display sz when larger than MTU; 1024 is MTU approx.
					returnString.append(";sz=\"");
					returnString.append(resource.getSizeEstimate());
					returnString.append("\"");
				}
			}
		}
		String result = returnString.toString();
		this.lastSize = result.length();
		return result;
	}
	
	private static boolean matchFilter(Set<String> filterSet, String string){
		if(!filterSet.isEmpty()){
			
			//there is a filter, null can not match any filter
			if(null == string) return false; 
			String encodedString = "";
			Set<String> parts = new HashSet<String>();
			// comparison is to be made with URL encoding
			try {
				encodedString = URLEncoder.encode(string, "UTF-8"); 
			} catch (UnsupportedEncodingException e) {
				logger.error("CoreRessource.matchFilter.URLEncoder.encode("+string+", UTF-8)", e);
			}
			// gather individual space (encoded as '+') separated entries
			for(String part : encodedString.split("\\+")){parts.add(part);}
						
			for(String filter : filterSet){
				if(!filter.endsWith("*")){ // '*' at the end indicate prefix filter
					//if no prefix filter
					if(!parts.contains(filter))return false; //no match contained
				} else {
					//if prefix filter
					boolean match = false;
					for(String part : parts){ //any part matches the prefix-filter?
						if(part.startsWith(filter.substring(0, filter.length()-1))){
							match = true;
							break;
						}
					}
					if(!match) return false; //no part has matched prefix-filter
				}
			} //go on with next filter
		}
		return true; // met all filters otherwise we would already have had returned false
	}

	@Override
	public boolean post(byte[] data) {
		/* nothing happens in case of a post */
		return true;
	}

	@Override
	public int getSizeEstimate() {
		if (this.lastSize < 0) {
			// only the case on startup
			// otherwise the lastSize is set by the last call of buildCoreString
			// lastSize need to be set to 0 to prevent infinite recursion
			this.lastSize = 0;
			buildCoreString(null);
		}
		return this.lastSize;
	}
}