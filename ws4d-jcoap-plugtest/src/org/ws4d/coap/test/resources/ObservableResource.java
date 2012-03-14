package org.ws4d.coap.test.resources;

import java.util.Vector;

import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResource;

public class ObservableResource extends BasicCoapResource {
    private ObservableResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	@Override
    public String getMimeType() {
	return null;
    }

    @Override
    public String getPath() {
	return "/obs";
    }

    @Override
    public String getShortName() {
	return getPath();
    }

    @Override
    public byte[] getValue() {
	// TODO should change temperature every 5s
	return "Payload".getBytes();
    }

    @Override
    public byte[] getValue(Vector<String> query) {
    	return getValue();
    }

    @Override
    public String getResourceType() {
	return "Temperature";
    }
    
	@Override
	public CoapMediaType getCoapMediaType() {
		return CoapMediaType.text_plain;
	}

}
