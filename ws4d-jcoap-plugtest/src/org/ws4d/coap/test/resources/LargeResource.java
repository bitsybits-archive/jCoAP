package org.ws4d.coap.test.resources;

import java.util.Vector;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResource;

public class LargeResource extends BasicCoapResource {
    private LargeResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		// TODO Auto-generated constructor stub
	}

	@Override
    public String getMimeType() {
	return null;
    }

    @Override
    public String getPath() {
	return "/large";
    }

    @Override
    public String getShortName() {
	return getPath();
    }

    @Override
    public byte[] getValue() {
	return "Payload".getBytes();
    }

    @Override
    public byte[] getValue(Vector<String> query) {
    	return getValue();
    }

    @Override
    public String getResourceType() {
	return "TestResourceType";
    }
    
	@Override
	public CoapMediaType getCoapMediaType() {
		return CoapMediaType.text_plain;
	}

}
