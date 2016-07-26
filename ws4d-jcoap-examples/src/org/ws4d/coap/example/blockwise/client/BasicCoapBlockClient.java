package org.ws4d.coap.example.blockwise.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class BasicCoapBlockClient implements CoapClient {
    private static final String SERVER_ADDRESS = "129.132.15.80";
    private static final int PORT = CoapConstants.COAP_DEFAULT_PORT;
    static int counter = 0;
    CoapChannelManager channelManager = null;
    CoapClientChannel clientChannel = null;

    public static void main(String[] args) {
        System.out.println("Start CoAP Client");
        BasicCoapBlockClient client = new BasicCoapBlockClient();
        client.channelManager = BasicCoapChannelManager.getInstance();
        client.runTestClient();
    }
    
    public void runTestClient(){
    	
    	
    	try {
			this.clientChannel = this.channelManager.connect(this, InetAddress.getByName(SERVER_ADDRESS), PORT);
			CoapRequest coapRequest = this.clientChannel.createRequest(true, CoapRequestCode.GET);
			coapRequest.setUriPath("/large");
			this.clientChannel.setMaxReceiveBlocksize(CoapBlockSize.BLOCK_64);
			this.clientChannel.sendMessage(coapRequest);
			System.out.println("Sent Request");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		System.out.println("Received response");
		System.out.println(response.toString());
		System.out.println(new String(response.getPayload()));
	}
	
	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		System.out.println("Received response");
	}
}
