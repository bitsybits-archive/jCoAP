package org.ws4d.coap.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.BasicCoapRequest.CoapRequestCode;

public class BasicCoapClient implements CoapClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = Constants.COAP_DEFAULT_PORT;
    static int counter = 0;
    CoapChannelManager channelManager = null;
    CoapChannel clientChannel = null;

    public static void main(String[] args) {
        System.out.println("Start CoAP Client");
        BasicCoapClient client = new BasicCoapClient();
        client.channelManager = BasicCoapChannelManager.getInstance();
        client.runTestClient();
    }
    
    public void runTestClient(){
    	try {
			clientChannel = channelManager.connect(this, InetAddress.getByName(SERVER_ADDRESS), PORT);
			CoapRequest coapRequest = clientChannel.createRequest(true, CoapRequestCode.GET);
//			coapRequest.setContentType(CoapMediaType.octet_stream);
//			coapRequest.setToken("ABCD".getBytes());
//			coapRequest.setUriHost("123.123.123.123");
//			coapRequest.setUriPort(1234);
//			coapRequest.setUriPath("/sub1/sub2/sub3/");
//			coapRequest.setUriQuery("a=1&b=2&c=3");
//			coapRequest.setProxyUri("http://proxy.org:1234/proxytest");
			clientChannel.sendMessage(coapRequest);
			System.out.println("Sent Request");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onConnectionFailed(CoapChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
	}

	@Override
	public void onResponse(CoapChannel channel, CoapResponse response) {
		System.out.println("Received response");
	}
}