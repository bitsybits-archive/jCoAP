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

package org.ws4d.coap.core.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.BasicCoapResource;
import org.ws4d.coap.core.rest.CoapResourceServer;
import org.ws4d.coap.core.rest.api.CoapResource;
import org.ws4d.coap.core.tools.Encoder;

/**
 * @author Björn Butzin <bjoern.butzin[at]uni-rostock.de>
 */
public class PlugTest {

	private static CoapResourceServer resourceServer = null;
	private static CoapClientChannel clientChannel = null;
	private static ClientDummy client = null;
	public static CoapResponse receivedResponse;

	/*
	 * ########################################################################
	 * General Test preparations
	 * ########################################################################
	 */
	@BeforeClass
	public static void setUpClass() {
		// empty
	}

	@AfterClass
	public static void tearDownClass() {
		// tear down server
		if (resourceServer != null) {
			resourceServer.stop();
			resourceServer = null;
		}
		if (clientChannel != null) {
			clientChannel.close();
			clientChannel = null;
		}
	}

	@Before
	public void setUp() {
		// set up client
		receivedResponse = null;
		if (null == client) {
			client = new ClientDummy();
		}
		if (null == clientChannel) {
			try {
				clientChannel = BasicCoapChannelManager.getInstance().connect(client,
						InetAddress.getByName("127.0.0.1"), CoapConstants.COAP_DEFAULT_PORT);
			} catch (UnknownHostException e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(-1);
			}
		}
		// set up server
		if (resourceServer == null) {
			resourceServer = new CoapResourceServer();
			try {
				resourceServer.start();
			} catch (Exception e) {
				System.err.println(e.getLocalizedMessage());
				System.exit(-1);
			}
		}
	}

	@After
	public void tearDown() {
		if (resourceServer != null) {
			resourceServer.stop();
		}
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
	 */

	@Test
	public void thisAlwaysPasses() {
		// validate test fixtures
	}
	
	/*
	 * RELIABLE / UNRELIABLE
	 * existing/nonexisting
	 * allowed/disallowed
	 * GET POST PUT DELETE
	 * 
	 * -> 2*2*2*5 = 40 Tests
	 */
	
	//RELIABLE GET EXISTING ALLOWED
	@Test
	public void ReliableGet() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/resource");
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals("content",Encoder.ByteToString(receivedResponse.getPayload()));
	}
	
	//UNRELIABLE GET EXISTING ALLOWED
	@Test
	public void UnreliableGet() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.GET);
		request.setUriPath("/resource");
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals("content",Encoder.ByteToString(receivedResponse.getPayload()));
	}
	
	//RELIABLE PUT EXISTING ALLOWED
	@Test
	public void ReliablePut() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.PUT);
		request.setUriPath("/resource");
		request.setPayload("1".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Changed_204,receivedResponse.getResponseCode());
	}
	
	//UNRELIABLE PUT EXISTING ALLOWED
	@Test
	public void UnreliablePut() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.PUT);
		request.setUriPath("/resource");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Changed_204,receivedResponse.getResponseCode());
	}
	
	//RELIABLE PUT NONEXISTING ALLOWED
	@Test
	public void ReliablePutNonExisting() throws InterruptedException{
		resourceServer.allowRemoteResourceCreation(true);
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.PUT);
		request.setUriPath("/resource1");
		request.setPayload("1".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Created_201,receivedResponse.getResponseCode());
	}
	
	//UNRELIABLE PUT NONEXISTING ALLOWED
	@Test
	public void UnreliablePutNonExisting() throws InterruptedException{
		resourceServer.allowRemoteResourceCreation(true);
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.PUT);
		request.setUriPath("/resource2");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Created_201,receivedResponse.getResponseCode());
	}
	
	//RELIABLE PUT NONEXISTING NOT-ALLOWED
	@Test
	public void ReliablePutNonExistingCreationNotAllowed() throws InterruptedException{
		resourceServer.allowRemoteResourceCreation(false);
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.PUT);
		request.setUriPath("/resource3");
		request.setPayload("1".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Forbidden_403,receivedResponse.getResponseCode());
	}
	
	//UNRELIABLE PUT NONEXISTING NOT-ALLOWED
	@Test
	public void UnreliablePutNonExistingCreationNotAllowed() throws InterruptedException{
		resourceServer.allowRemoteResourceCreation(false);
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.PUT);
		request.setUriPath("/resource4");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Forbidden_403,receivedResponse.getResponseCode());
	}
	
	// RELIABLE DELETE EXISTING ALLOWED
	@Test
	public void ReliableDelete() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.DELETE);
		request.setUriPath("/resource");
		request.setPayload("1".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Deleted_202,receivedResponse.getResponseCode());
	}
	
	// UNRELIABLE DELETE EXISTING ALLOWED
	@Test
	public void UnreliableDelete() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.DELETE);
		request.setUriPath("/resource");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Deleted_202,receivedResponse.getResponseCode());
	}
	
	// RELIABLE DELETE NONEXISTING ALLOWED
	@Test
	public void DeleteNonExisting() throws InterruptedException{
		resourceServer.createResource(new BasicCoapResource("/resource", "content", CoapMediaType.text_plain));
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.DELETE);
		request.setUriPath("/resource3");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Deleted_202,receivedResponse.getResponseCode());
	}
	
	// RELIABLE DELETE EXISTING NOT-ALLOWED
	@Test
	public void DeleteNotAllowed() throws InterruptedException{
		CoapResource res = new BasicCoapResource("/resource", "content", CoapMediaType.text_plain);
		res.setDeletable(false);
		resourceServer.createResource(res);
		CoapRequest request = clientChannel.createRequest(false, CoapRequestCode.DELETE);
		request.setUriPath("/resource");
		request.setPayload("2".getBytes());
		clientChannel.sendMessage(request);
		while (null == receivedResponse)Thread.sleep(10);
		Assert.assertEquals(CoapResponseCode.Forbidden_403,receivedResponse.getResponseCode());
	}
	
	// TIMEOUT -> Test
	
	// RELIABLE GET PUT POST with separate Response -> 1 Test
	
	// /.well-known/core
	
	@Test
	public void wellKnownFull() throws InterruptedException {
		resourceServer.createResource(new BasicCoapResource("/resource1", "content1", CoapMediaType.text_plain)
				.setResourceType("resource1Type")
				.setInterfaceDescription("resource1Description"));

		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		clientChannel.sendMessage(request);

		while (null == receivedResponse)Thread.sleep(10);

		Assert.assertEquals("</.well-known/core>,</resource1>;rt=\"resource1Type\";if=\"resource1Description\"",
				Encoder.ByteToString(receivedResponse.getPayload()));
	}

	@Test
	public void wellKnownRT() throws InterruptedException {
		resourceServer.createResource(new BasicCoapResource("/resource1", "content1", CoapMediaType.text_plain)
				.setResourceType("resource1Type")
				.setInterfaceDescription("resource1Description"));
		resourceServer.createResource(new BasicCoapResource("/resource2", "content2", CoapMediaType.text_plain)
				.setResourceType("resource2Type")
				.setInterfaceDescription("resource2Description"));

		CoapRequest request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		request.setUriQuery("rt=resource1Type");
		clientChannel.sendMessage(request);

		while (null == receivedResponse)Thread.sleep(10);

		Assert.assertEquals("</resource1>;rt=\"resource1Type\";if=\"resource1Description\"",
				Encoder.ByteToString(receivedResponse.getPayload()));
	}
	
	// URI-query
	
	
	
	// Path length
	@Test(expected = Exception.class)
	public void invalidNameTooLongResource() {
		String resourcename = "";
		for (int i = 0; i < 256; i++) {
			resourcename += 'a';
		}
		CoapResource res = new BasicCoapResource("/" + resourcename, "", CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPathTooLongResource() {
		String resourcename = "";
		for (int i = 0; i < 256; i++) {
			resourcename += 'a';
		}
		CoapResource res = new BasicCoapResource("/1/2/3/4/5/6/7/8/9/0/1/2/3/4/5/6/7/8/9/" + resourcename, "",
				CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	/*
	 * ########################################################################
	 * valid resources
	 * ########################################################################
	 */

	@Test
	public void validPathShortestResources() {
		// An empty path component is equivalent to an absolute path of "/"
		CoapResource res = new BasicCoapResource("/test", "", CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test
	public void validPathRootResources() {
		// An empty path component is equivalent to an absolute path of "/"
		CoapResource res = new BasicCoapResource("", "", CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test
	public void validNameLongestResources() {
		String resourcename = "";
		for (int i = 0; i < 255; i++) {
			resourcename += 'a';
		}
		CoapResource res = new BasicCoapResource("/" + resourcename, "", CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}

	@Test
	public void validPathLongestResources() {
		CoapResource res = new BasicCoapResource("/1/2/3/4/5", "", CoapMediaType.text_plain);
		resourceServer.createResource(res);
	}
}


class ClientDummy implements CoapClient {

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
		PlugTest.receivedResponse = response;
	}
}