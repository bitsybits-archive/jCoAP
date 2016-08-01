package org.ws4d.coap.test.resources;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;

public class TestResource extends BasicCoapResource {

	private TestResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	public TestResource() {
		this("/test", "TestResource Payload".getBytes(),
				CoapMediaType.text_plain);
	}

	@Override
	public synchronized String getResourceType() {
		return "TypeA";
	}
}
