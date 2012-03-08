package org.ws4d.coap.test.resources;

import java.util.Vector;

import org.ws4d.coap.rest.CoapResource;

public class DelayedResource implements CoapResource {
    @Override
    public String getMimeType() {
	return null;
    }

    @Override
    public String getPath() {
	return "/separate";
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
	return null;
    }

    @Override
    public String getResourceType() {
	return "Delayed";
    }
}
