package org.ws4d.coap.rest;

import java.util.List;

import org.ws4d.coap.messages.CoapMediaType;

public class CoapWellKnownResource extends BasicCoapResource {
	private List<String> resources;	
	
	public CoapWellKnownResource( String path, byte[] value, CoapMediaType mediaType ) {
		super(path, value, mediaType);
	}

}
