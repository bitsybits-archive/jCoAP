package org.ws4d.coap.test.resources;

import org.ws4d.coap.rest.CoapResource;

public class LargeResource implements CoapResource {
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
    public byte[] getValue(String query) {
	return null;
    }

    @Override
    public String getResourceType() {
	return "TestResourceType";
    }
}
