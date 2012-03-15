package org.ws4d.coap.test.resources;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;

public class LongPathResource extends BasicCoapResource{
    private LongPathResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		// TODO Auto-generated constructor stub
	}
    
    public LongPathResource(){
    	this("/seg1/seg2/seg3", "LongPathResource Payload".getBytes(), CoapMediaType.text_plain);
    }
    
    @Override
    public String getResourceType() {
	return "TypeC";
    }

}
