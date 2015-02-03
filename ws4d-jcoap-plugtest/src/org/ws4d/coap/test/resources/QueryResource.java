package org.ws4d.coap.test.resources;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;

public class QueryResource extends BasicCoapResource {
	
    private QueryResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		// TODO Auto-generated constructor stub
	}
    
    public QueryResource(){
    	this("/query", "QueryResource Payload".getBytes(), CoapMediaType.text_plain);
    }

    @Override
    public String getResourceType() {
	return "TypeB";
    }


}
