/* Copyright 2015 University of Rostock
 
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
 *****************************************************************************/

package org.ws4d.coap.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.rest.BasicCoapResource;
import org.ws4d.coap.rest.CoapResourceServer;
import org.ws4d.coap.tools.Encoder;
/**
 * @author Björn Butzin <bjoern.butzin[at]uni-rostock.de>
 */
public class InterfaceTest {

	private static CoapResourceServer resourceServer;
	private static CoapClientChannel clientChannel;
	private ClientDummy client;
	//private static CoapChannelManager channelManager;
	//private static InetAddress inetAddress;

	static CoapResponse receivedResponse = null;

	/*
	 * ########################################################################
	 * General Test preparations
	 * ########################################################################
	 */
	@BeforeClass
	public static void setUpClass() {
		// set up server
		if (resourceServer != null) {
			resourceServer.stop();
		}
		resourceServer = new CoapResourceServer();
	}

	@AfterClass
	public static void tearDownClass() {
		// tear down server
		if (resourceServer != null) {
			resourceServer.stop();
			resourceServer = null;
		}
	}

	@Before
	public void setUp() {
		// set up client
		receivedResponse = null;
		this.client = new ClientDummy();
		try {
			clientChannel = BasicCoapChannelManager.getInstance()
					.connect(this.client, InetAddress.getByName("127.0.0.1"), Constants.COAP_DEFAULT_PORT);
		} catch (UnknownHostException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
		// set up server
		try {
			resourceServer.start();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	@After
	public void tearDown() {
		// tear down client
		if (clientChannel != null) {
			clientChannel.close();
			clientChannel = null;
		}
		this.client = null;
		// reset server
		resourceServer.stop();
	}

	/*
	 * ########################################################################
	 * Tests
	 * 
	 * @Test
	 * @Test(expected= IndexOutOfBoundsException.class)
	 * @Test(timeout=1000) - fails after 1000 milliseconds
	 * org.junit.Assert.* - compare result & expectation
	 * 
	 * ########################################################################
	 * 
	 * (/.well-known/core)
	 * 		full query
	 * 		query filter
	 * 			rt - single multiple
	 * 			if - single multiple
	 * 			href - single multiple
	 * 			combinations
	 * 	encoding
	 * 		äöüß + % 
	 * 	GET
	 * 		without eTag
	 * 		with eTag
	 * 			matching
	 * 			non matching
	 * 	POST
	 * 	PUT
	 * 		If-Match
	 * 		If-Non-Match
	 * 	DELETE
	 */
	
	@Test
	public void wellKnownFull() throws InterruptedException {
		resourceServer.createResource(new BasicCoapResource("/resource1","content1", CoapMediaType.text_plain)
				.setResourceType("resource1Type")
				.setInterfaceDescription("resource1Description"));
		
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		clientChannel.sendMessage(request);
		
		while(null == receivedResponse) Thread.sleep(10);
	
		Assert.assertEquals("</.well-known/core>,</resource1>;rt=\"resource1Type\";if=\"resource1Description\"", Encoder.ByteToString(receivedResponse.getPayload()));
	}
	
	@Test
	public void wellKnownRT() throws InterruptedException {
		resourceServer.createResource(new BasicCoapResource("/resource1","content1", CoapMediaType.text_plain)
				.setResourceType("resource1Type")
				.setInterfaceDescription("resource1Description"));
		resourceServer.createResource(new BasicCoapResource("/resource2","content2", CoapMediaType.text_plain)
				.setResourceType("resource2Type")
				.setInterfaceDescription("resource2Description"));
		
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core?rt=resource1Type");
		clientChannel.sendMessage(request);
		
		while(null == receivedResponse) Thread.sleep(10);
	
		Assert.assertEquals("</resource1>;rt=\"resource1Type\";if=\"resource1Description\"", Encoder.ByteToString(receivedResponse.getPayload()));
	}
	
	
	/*
	 * ########################################################################
	 * Client Dummy Class
	 * ########################################################################
	 */
	
	private class ClientDummy implements CoapClient {

		public ClientDummy() {
			// This is intended to be empty
		}


		public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress,
				int srcPort) {
			// This is intended to be empty
		}


		public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
			// This is intended to be empty
		}

		public void onResponse(CoapClientChannel channel, CoapResponse response) {
			InterfaceTest.receivedResponse = response;
		}
	}
}