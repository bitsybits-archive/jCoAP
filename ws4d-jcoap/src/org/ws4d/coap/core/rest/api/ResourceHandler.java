package org.ws4d.coap.core.rest.api;

import java.util.List;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.CoapData;

public abstract class ResourceHandler {

	public abstract CoapMediaType getMediaType();

	public abstract CoapData handleGet();

	public abstract CoapData handleGet(List<String> queryString);

	public abstract boolean handlePost(byte[] data);

	public abstract boolean handlePut(byte[] data);

	public abstract boolean handleDelete();

}
