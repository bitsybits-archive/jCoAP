package org.ws4d.coap.test.resources;

import java.util.Vector;

import org.ws4d.coap.rest.CoapResource;

public class ObservableResource implements CoapResource {
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
	return null;
    }

    @Override
    public String getResourceType() {
	return "Temperature";
    }
}
