package org.ws4d.coap.proxy;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;

public class ProxyResource extends BasicCoapResource {
	private ProxyResourceKey key = null;

	public ProxyResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
	}

	public ProxyResourceKey getKey() {
		return this.key;
	}

	public void setKey(ProxyResourceKey key) {
		this.key = key;
	}
}
