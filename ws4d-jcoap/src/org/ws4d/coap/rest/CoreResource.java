
package org.ws4d.coap.rest;

import java.util.HashMap;

/**
 * Well-Known CoRE support (draft-ietf-core-link-format-05)
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 */
public class CoreResource implements Resource {
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
        StringBuilder returnString = new StringBuilder();
        for (String coreLine : coreStrings.values()) {
            returnString.append(coreLine);
            returnString.append(",");
        }
        return returnString.toString().getBytes();
    }

    public void registerResource(Resource resource) {
        if (resource != null) {
            StringBuilder coreLine = new StringBuilder();
            coreLine.append("<");
            coreLine.append(resource.getPath());
            coreLine.append(">");
            // coreLine.append(";ct=???");
            // coreLine.append(";rt=\"" + resource.getShortName() + "\"");
            // coreLine.append(";if=\"observations\"");
            coreStrings.put(resource, coreLine.toString());
        }
    }

    @Override
    public byte[] getValue(String query) {
	return getValue();
    }
}
