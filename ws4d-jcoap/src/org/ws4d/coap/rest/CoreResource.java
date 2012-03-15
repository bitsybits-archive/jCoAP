
package org.ws4d.coap.rest;

import java.util.HashMap;
import java.util.Vector;

import org.ws4d.coap.messages.CoapMediaType;

/**
 * Well-Known CoRE support (draft-ietf-core-link-format-05)
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 */
public class CoreResource implements CoapResource {
    private final static String uriPath = "/.well-known/core";
    private HashMap<Resource, String> coreStrings = new HashMap<Resource, String>();

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public String getPath() {
        return uriPath;
    }

    @Override
    public String getShortName() {
        return getPath();
    }

    @Override
    public byte[] getValue() {
        return buildCoreString(null).getBytes();
    }

    public void registerResource(CoapResource resource) {
        if (resource != null) {
            StringBuilder coreLine = new StringBuilder();
            coreLine.append("<");
            coreLine.append(resource.getPath());
            coreLine.append(">");
            // coreLine.append(";ct=???");
            coreLine.append(";rt=\"" + resource.getResourceType() + "\"");
            // coreLine.append(";if=\"observations\"");
            coreStrings.put(resource, coreLine.toString());
        }
    }
    
    private String buildCoreString(String resourceType) {
	StringBuilder returnString = new StringBuilder();
        for (String coreLine : coreStrings.values()) {
            if (resourceType==null || coreLine.contains(resourceType)) {
        	returnString.append(coreLine);
        	returnString.append(",");
            }
        }
        returnString.deleteCharAt(returnString.length()-1);
        return returnString.toString();
    }

    @Override
    public byte[] getValue(Vector<String> queries) {
	for (String query : queries) {
	    if (query.startsWith("rt=")) return buildCoreString(query.substring(3)).getBytes();
	}
	return getValue();
    }

	@Override
	public String getResourceType() {
		// TODO implement
		return null;
	}

	@Override
	public CoapMediaType getCoapMediaType() {
		return CoapMediaType.link_format;
	}
}
