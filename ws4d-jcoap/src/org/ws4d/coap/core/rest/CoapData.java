package org.ws4d.coap.core.rest;

import org.ws4d.coap.core.enumerations.CoapMediaType;

public class CoapData {

	private final byte[] data;
	private final CoapMediaType type;

	public CoapData(byte[] dataPayload, CoapMediaType dataType) {
		if (null == dataPayload || null == dataType) {
			throw new IllegalArgumentException();
		}
		this.data = dataPayload;
		this.type = dataType;
	}

	public byte[] getPayload() {
		return this.data;
	}

	public CoapMediaType getMediaType() {
		return this.type;
	}
}
