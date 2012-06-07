/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */
package org.ws4d.coap.proxy;

import java.net.InetAddress;
import java.net.URI;
import java.text.SimpleDateFormat;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class ProxyMessageContext {
	/*unique for reqMessageID, remoteHost, remotePort*/

	/* is true if a translation was done (always true for incoming http requests)*/
	private boolean translate;  //translate from coap to http

	/*in case of incoming coap request */
	private CoapRequest coapRequest;  //the coapRequest of the origin client (maybe translated)
	
	/* in case of incoming http request */
	private HttpRequest httpRequest;	//the httpRequest of the origin client (maybe translated)
	NHttpResponseTrigger trigger;

	/* in case of a coap response */
	private CoapResponse coapResponse; //the coap response of the final server

	/* in case of a http response */
	private HttpResponse httpResponse; //the http response of the final server
	
	private ProxyResource resource; 

	private URI uri;
	
	private InetAddress remoteAddress;
	private int remotePort;
	
	private boolean cached = false;
	
	public ProxyMessageContext(CoapRequest request, boolean translate,
			InetAddress remoteAddress, int remotePort, URI uri) {

		this.coapRequest = request;
		this.translate = translate;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.uri = uri;

	}
	
	public ProxyMessageContext(HttpRequest request,
			InetAddress remoteAddress, int remotePort, URI uri, NHttpResponseTrigger trigger) {

		this.httpRequest = request;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.translate = true; //always translate http
		this.uri = uri;
		this.trigger = trigger;
		
		System.out.println("DEBUG: created HTTP Context");
	}
	
	public boolean isCoapRequest(){
		return coapRequest != null;
	}

	public boolean isHttpRequest(){
		return httpRequest != null;
	}
	
	public CoapRequest getCoapRequest() {
		return coapRequest;
	}
	
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}
	
	public CoapResponse getCoapResponse() {
		return coapResponse;
	}

	public void setCoapResponse(CoapResponse coapResponse) {
		this.coapResponse = coapResponse;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}
	
	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public boolean isTranslate() {
		return translate;
	}

	public void setTranslatedCoapRequest(CoapRequest request) {
		this.coapRequest = request;
	}

	public void setTranslatedHttpRequest(HttpRequest request) {
		this.httpRequest = request;
	}

	public URI getUri() {
		return uri;
	}

	public NHttpResponseTrigger getTrigger() {
		return trigger;
	}

	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}

	public ProxyResource getResource() {
		return null;
	}

	public void setResource(ProxyResource resource) {
		this.resource = resource;
	}

}
