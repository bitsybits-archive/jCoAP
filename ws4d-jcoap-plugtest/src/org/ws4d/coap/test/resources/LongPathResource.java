package org.ws4d.coap.test.resources;

import java.util.Vector;

import org.ws4d.coap.rest.CoapResource;

public class LongPathResource implements CoapResource {
    @Override
	public String getMimeType() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public String getPath() {
	    return "/seg1/seg2/seg3";
	}

	@Override
	public String getShortName() {
	    return getPath();
	}

	@Override
	public byte[] getValue() {
	    return "Test".getBytes();
	}

	@Override
	public byte[] getValue(Vector<String> query) {
	    return null;
	}

	@Override
	public String getResourceType() {
	    return "LongPath";
	}
}
