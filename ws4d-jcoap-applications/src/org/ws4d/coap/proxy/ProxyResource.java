package org.ws4d.coap.proxy;

import org.apache.http.HttpResponse;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.rest.BasicCoapResource;

public class ProxyResource extends BasicCoapResource {
	
	private ProxyResourceKey key = null;
	

	public ProxyResource(String path, byte[] value, CoapMediaType mediaType) {
		super(path, value, mediaType);
		
	}
	
	public ProxyResource(CoapResponse response){
		this(null, null, null); 
		//TODO: implement 
	}

	public ProxyResource(HttpResponse response){
		this(null, null, null); 
		//TODO: implement 
	}

	public CoapResponse getCoapResponse(){
		return null;
	}

	public HttpResponse getHttpResponse(){
		return null;
	}
	
	public ProxyResourceKey getKey() {
		return key;
	}

	public void setKey(ProxyResourceKey key) {
		this.key = key;
	}
	
}
