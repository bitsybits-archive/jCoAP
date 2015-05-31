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
import org.junit.Before;
import org.junit.BeforeClass;
import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.rest.CoapResourceServer;

/**
 * Tests for jCoAP.
 *
 * @author Bj�rn Butzin <bjoern.butzin[at]uni-rostock.de>
 */
public class InterfaceTest {

	private static CoapResourceServer resourceServer;
	private static CoapClientChannel clientChannel;
	private static CoapChannelManager channelManager;
	private static ClientDummy clientDummy;
	private static InetAddress inetAddress;

	private class ClientDummy implements CoapClient {
		@Override
		public void onResponse(CoapClientChannel channel, CoapResponse response) {
			// This is intended to be empty
		}
		
		@Override
		public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
			// This is intended to be empty
		}

		@Override
		public void onConnectionFailed(CoapClientChannel channel,
				boolean notReachable, boolean resetByServer) {
			// This is intended to be empty
		}
	}

	// @Test //indicates a test method
	// @Test(expected= IndexOutOfBoundsException.class) //indicates a test
	// method expecting an exception
	// @Test(timeout=1000) //indicates a test method that fails after 1000
	// milliseconds execution time
	// org.junit.Assert.* // compare result & expectation

	/*
	 * ########################################################################
	 * General Test preparations
	 * ########################################################################
	 */

	@BeforeClass
	public static void setUpClass() {
		try {
			inetAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			System.err.println(e.getLocalizedMessage());
		}

		// set up server
		if (resourceServer != null) {
			resourceServer.stop();
		}
		resourceServer = new CoapResourceServer();
		try {
			resourceServer.start();
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
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
		channelManager = BasicCoapChannelManager.getInstance();
		clientDummy = new ClientDummy();
		clientChannel = channelManager.connect(clientDummy, inetAddress,
				Constants.COAP_DEFAULT_PORT);
		if (clientChannel == null) {
			System.err.println("Connect failed.");
			System.exit(-1);
		}
	}

	@After
	public void tearDown() {
		// tear down client
		if (clientChannel != null) {
			clientChannel.close();
			channelManager = null;
		}
		clientDummy = null;

		// reset server
		//FIXME: causes ConcurrentModificationException
		for (String path : resourceServer.getResources().keySet()) {
			if(path!="/.well-known/core"){
				System.out.println(path);
				resourceServer.deleteResource(path);
			}
		}
	}

	/*
	 * ########################################################################
	 * Tests
	 * ########################################################################
	 * 
	 * Considerations:
	 * (/.well-known/core)
	 * (GET / OBSERVE, POST, PUT, DELETE)
	 * Blockwise Transfer
	 */
}