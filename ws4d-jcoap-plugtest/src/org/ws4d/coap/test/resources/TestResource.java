package org.ws4d.coap.test.resources;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;

public class TestResource extends BasicCoapResource{
	
    private TestResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}
    
    public TestResource(){
    	this("/test", "TestResource Payload".getBytes(), CoapMediaType.text_plain);
    }
    
    @Override
    public String getResourceType() {
	return "TypeA";
    }
}
