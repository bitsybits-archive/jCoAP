package org.ws4d.coap.test.resources;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;

public class QueryResource extends BasicCoapResource {

	private QueryResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	public QueryResource() {
		this("/query", "QueryResource Payload".getBytes(),
				CoapMediaType.text_plain);
	}

	@Override
	public synchronized String getResourceType() {
		return "TypeB";
	}

}
