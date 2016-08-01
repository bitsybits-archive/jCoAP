package org.ws4d.coap.test;

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

/**
 * Client Application for Plugtest 2012, Paris, France Execute with argument
 * Identifier (e.g., TD_COAP_CORE_01)
 *
 * @author Nico Laum
 * @author Christian Lerche
 */
public class PlugtestClient implements CoapClient {

	private CoapChannelManager channelManager = null;
	private CoapClientChannel clientChannel = null;
	private CoapRequest request = null;
	private String ip = null;
	private int port = 0;

	public static void main(String[] args) {
		PlugtestClient client = new PlugtestClient();
		client.start("127.0.0.1", CoapConstants.COAP_DEFAULT_PORT, "TD_COAP_LINK_02", "");
	}

	public void start(String serverAddress, int serverPort, String testcase, String filter) {

		this.ip = serverAddress;
		this.port = serverPort;

		init(false, CoapRequestCode.GET);
		this.request.setUriPath("/.well-known/core");
		System.out.println("QueryPath: " + this.request.getUriPath());

		//reliable GET
		if (testcase.equals("TD_COAP_CORE_01")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/test");
			
		//reliable POST
		} else if (testcase.equals("TD_COAP_CORE_02")) {
			init(true, CoapRequestCode.POST);
			this.request.setUriPath("/test");
			this.request.setPayload("Content of new resource /test");
			this.request.setContentType(CoapMediaType.text_plain);
			
		//reliable PUT
		} else if (testcase.equals("TD_COAP_CORE_03")) {
			init(true, CoapRequestCode.PUT);
			this.request.setUriPath("/test");
			this.request.setPayload("Content of new resource /test");
			this.request.setContentType(CoapMediaType.text_plain);
			
		//reliable DELETE
		} else if (testcase.equals("TD_COAP_CORE_04")) {
			init(true, CoapRequestCode.DELETE);
			this.request.setUriPath("/test");
			
		//UNreliable GET
		} else if (testcase.equals("TD_COAP_CORE_05")) {
			init(false, CoapRequestCode.GET);
			this.request.setUriPath("/test");
			
		//UNreliable POST
		} else if (testcase.equals("TD_COAP_CORE_06")) {
			init(false, CoapRequestCode.POST);
			this.request.setUriPath("/test");
			this.request.setPayload("Content of new resource /test");
			this.request.setContentType(CoapMediaType.text_plain);
			
		//UNreliable PUT
		} else if (testcase.equals("TD_COAP_CORE_07")) {
			init(false, CoapRequestCode.PUT);
			this.request.setUriPath("/test");
			this.request.setPayload("Content of new resource /test");
			this.request.setContentType(CoapMediaType.text_plain);
			
		//UNreliable DELETE
		} else if (testcase.equals("TD_COAP_CORE_08")) {
			init(false, CoapRequestCode.DELETE);
			this.request.setUriPath("/test");
			
		
		} else if (testcase.equals("TD_COAP_CORE_09")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/separate");
			
		} else if (testcase.equals("TD_COAP_CORE_10")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/test");
			this.request.setToken("AABBCCDD".getBytes());
			
		} else if (testcase.equals("TD_COAP_CORE_11")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/test");
			
		} else if (testcase.equals("TD_COAP_CORE_12")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/seg1/seg2/seg3");
			
		} else if (testcase.equals("TD_COAP_CORE_13")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/query");
			this.request.setUriQuery("first=1&second=2&third=3");
			
		} else if (testcase.equals("TD_COAP_CORE_14")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/test");
			
		} else if (testcase.equals("TD_COAP_CORE_15")) {
			init(true, CoapRequestCode.GET);
			this.request.setUriPath("/separate");
			
		} else if (testcase.equals("TD_COAP_CORE_16")) { // jcoap-draft18/ws4d-jcoap-plugtest/src/org/ws4d/coap/test
			init(false, CoapRequestCode.GET);
			this.request.setUriPath("/separate");
			
		} else if (testcase.equals("TD_COAP_LINK_01")) {
			init(false, CoapRequestCode.GET);
			this.request.setUriPath("/.well-known/core");
			
		} else if (testcase.equals("TD_COAP_LINK_02")) {
			init(false, CoapRequestCode.GET);
			this.request.setUriPath("/.well-known/core");
			this.request.setUriQuery("rt=" + filter);
		} else {
			System.out.println("===Failure=== (unknown test case)");
			System.exit(-1);
		}
		run();
	}

	public void init(boolean reliable, CoapRequestCode requestCode) {
		this.channelManager = BasicCoapChannelManager.getInstance();
		this.channelManager.setMessageId(1000);

		try {
			this.clientChannel = this.channelManager.connect(this, InetAddress.getByName(this.ip), this.port);
			if (this.clientChannel == null) {
				System.out.println("Connect failed.");
				System.exit(-1);
			}
			this.request = this.clientChannel.createRequest(reliable, requestCode);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void run() {
		if (this.request.getPayload() != null) {
			System.out.println(
					"Send Request: " + this.request.toString() + " (" + new String(this.request.getPayload()) + ")");
		} else {
			System.out.println("Send Request: " + this.request.toString());
		}
		this.clientChannel.sendMessage(this.request);
	}

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
		System.exit(-1);
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		if (response.getPayload() != null) {
			System.out.println("Response: " + response.toString() + " (" + new String(response.getPayload()) + ")");
		} else {
			System.out.println("Response: " + response.toString());
		}
	}

	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		System.out.println("Received Multicast Response");

	}
}