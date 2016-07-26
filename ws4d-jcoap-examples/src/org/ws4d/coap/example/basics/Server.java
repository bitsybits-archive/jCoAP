/*
 * Copyright 2015 University of Rostock
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
 */

package org.ws4d.coap.example.basics;

import java.util.List;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.rest.CoapResourceServer;
import org.ws4d.coap.core.rest.MultiTypeResource;
import org.ws4d.coap.core.rest.api.ResourceHandler;
import org.ws4d.coap.core.tools.Encoder;
import org.ws4d.coap.example.basics.resources.ObservableResource;
import org.ws4d.coap.example.basics.resources.Window;


/**
 * This class demonstrates a CoAP server
 * 
 * @author Björn Konieczeck <bjoern.konieczeck@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class Server {

	/**
	 * @param args
	 */
	private static Server coapServer;
	private CoapResourceServer resourceServer;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		coapServer = new Server();
		coapServer.start();
	}

	public void start() {
		System.out.println("===Run Test Server ===");

		// create server
		if (this.resourceServer != null)	this.resourceServer.stop();
		this.resourceServer = new CoapResourceServer();

		// add additional resource while server is running
		this.resourceServer.createResource(new Window());
		
		// add a standard resource
		this.resourceServer.createResource(new BasicCoapResource("/noMatchCase","noMatchCase", CoapMediaType.text_plain)
				.setResourceType("Test interface"));
		this.resourceServer.createResource(new BasicCoapResource("/noMatchUnsufficient","noMatchUnsufficient", CoapMediaType.text_plain)
				.setResourceType("Test"));
		this.resourceServer.createResource(new BasicCoapResource("/noMatchUnsufficient2","noMatchUnsufficient2", CoapMediaType.text_plain)
				.setResourceType("Interface"));
		this.resourceServer.createResource(new BasicCoapResource("/PerfectMatch","PerfectMatch", CoapMediaType.text_plain)
				.setResourceType("Test Interface"));
		this.resourceServer.createResource(new BasicCoapResource("/Match+","Match+", CoapMediaType.text_plain)
				.setResourceType("Test Interface Foo"));
		this.resourceServer.createResource(new BasicCoapResource("/Match*+","Match*+", CoapMediaType.text_plain)
				.setResourceType("Test Intergalactic Foo"));
		this.resourceServer.createResource(new BasicCoapResource("/Matchöüäß-","Matchöüäß-", CoapMediaType.text_plain)
				.setResourceType("öüäß- Intergalactic Foo"));
		
		this.resourceServer.createResource(new BasicCoapResource("/sub/012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234",
				"subresource".getBytes(), CoapMediaType.text_plain));
		this.resourceServer.createResource(new BasicCoapResource("/ns:device/ns:service/operation/parameter",
				"firstValue", CoapMediaType.text_plain));
		
		ResourceHandler XMLhandler = new ResourceHandler(){
			@Override
			public CoapMediaType getMediaType() {
				return CoapMediaType.xml;
			}

			@Override
			public CoapData handleGet() {
				return new CoapData(Encoder.StringToByte("<XML>Content</XML>"), this.getMediaType());
			}

			@Override
			public CoapData handleGet(List<String> queryString) {
				return new CoapData(Encoder.StringToByte("<XML>Content</XML>"), this.getMediaType());
			}

			@Override
			public boolean handlePost(byte[] data) {
				return false;
			}

			@Override
			public boolean handlePut(byte[] data) {
				return false;
			}

			@Override
			public boolean handleDelete() {
				return true;
			}
			
		};
		ResourceHandler JSONhandler = new ResourceHandler(){
			@Override
			public CoapMediaType getMediaType() {
				return CoapMediaType.json;
			}

			@Override
			public CoapData handleGet() {
				return new CoapData(Encoder.StringToByte("JSON{content:content}"), this.getMediaType());
			}

			@Override
			public CoapData handleGet(List<String> queryString) {
				return new CoapData(Encoder.StringToByte("JSON{content:content}"), this.getMediaType());
			}

			@Override
			public boolean handlePost(byte[] data) {
				return false;
			}

			@Override
			public boolean handlePut(byte[] data) {
				return false;
			}

			@Override
			public boolean handleDelete() {
				return true;
			}
			
		};
		
		MultiTypeResource res = new MultiTypeResource("/multiType", XMLhandler);
		res.addResourceHandler(JSONhandler);
		this.resourceServer.createResource(res);
		

		// initialize observable resource
		ObservableResource temp = new ObservableResource();

		// make resource observable
		temp.setObservable(true);

		// add resource to server
		this.resourceServer.createResource(temp);
		temp.registerServerListener(this.resourceServer);

		// run the server
		try {
			this.resourceServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add additional resource while server is running
		this.resourceServer.createResource(new BasicCoapResource(
				"/added/at/runtime", "startValue",
				CoapMediaType.text_plain));

		// emulate data changes
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				//nothing
			}
			temp.changed();
		}
	}
}
