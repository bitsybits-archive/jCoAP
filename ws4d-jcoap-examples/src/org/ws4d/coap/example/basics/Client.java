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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.tools.Encoder;

/**
 * This class demonstrates a CoAP client
 * 
 * @author Björn Konieczeck <bjoern.konieczeck@uni-rostock.de>
 * @author Björn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class Client implements CoapClient {

	private static final boolean exitAfterResponse = false;
	public static void main(String[] args) {
		Client coapClient = new Client();

		// binding client to address and port
		if (args.length < 1)
			coapClient.start("127.0.0.1", CoapConstants.COAP_DEFAULT_PORT);
		else
			coapClient.start(args[0], CoapConstants.COAP_DEFAULT_PORT);
	}

	public void start(String serverAddress, int serverPort) {
		System.out.println("===START=== (Run Test Client)");

		String sAddress = serverAddress;
		int sPort = serverPort;

		CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();

		CoapClientChannel clientChannel = null;

		try {
			clientChannel = channelManager.connect(this, InetAddress.getByName(sAddress), sPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		CoapRequest request;

		if (null == clientChannel) {
			return;
		}

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/temperature");
		printRequest(request);
		clientChannel.sendMessage(request);
		

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/multiType");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/multiType");
		request.addAccept(CoapMediaType.exi);
		request.addAccept(CoapMediaType.json);
		request.addAccept(CoapMediaType.xml);
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/window");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.POST);
		request.setUriPath("/window");
		request.setContentType(CoapMediaType.text_plain);
		request.setPayload("true");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/window");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.POST);
		request.setUriPath("/newResource");
		request.setContentType(CoapMediaType.text_plain);
		request.setPayload("newValue");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/newResource");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.PUT);
		request.setUriPath("/newResource");
		request.setContentType(CoapMediaType.text_plain);
		request.setPayload("veryNewValue");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/newResource");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.DELETE);
		request.setUriPath("/newResource");
		request.setContentType(CoapMediaType.text_plain);
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/.well-known/core");
		printRequest(request);
		clientChannel.sendMessage(request);

		String longpath = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234";
		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/sub/" + longpath);
		printRequest(request);
		clientChannel.sendMessage(request);

		request = clientChannel.createRequest(true, CoapRequestCode.GET);
		request.setUriPath("/ns:device/ns:service/operation/parameter");
		printRequest(request);
		clientChannel.sendMessage(request);
	}

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
		System.exit(-1);
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		if (response.getPayload() != null) {
			System.out.println(
					"Response: " + response.toString() + " payload: " + Encoder.ByteToString(response.getPayload()));
		} else {
			System.out.println("Response: " + response.toString());
		}
		if (Client.exitAfterResponse) {
			System.out.println("===END===");
			System.exit(0);
		}
	}

	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		// TODO Auto-generated method stub
	}
	
	private static void printRequest(CoapRequest request){
		if(request.getPayload() != null){
			System.out.println("Request: "+request.toString() + " payload: " + Encoder.ByteToString(request.getPayload()) + " resource: " + request.getUriPath());
		} else {
			System.out.println("Request: "+request.toString() + " resource: " + request.getUriPath());
		}
	}
}