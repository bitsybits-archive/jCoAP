package org.ws4d.coap.test.resources;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;

public class LongPathResource extends BasicCoapResource {
	
	private LongPathResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	public LongPathResource() {
		this("/seg1/seg2/seg3", "LongPathResource Payload".getBytes(),
				CoapMediaType.text_plain);
	}

	@Override
	public synchronized String getResourceType() {
		return "TypeC";
	}

}
