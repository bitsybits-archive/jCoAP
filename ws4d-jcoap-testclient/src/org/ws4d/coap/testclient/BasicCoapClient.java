package org.ws4d.coap.testclient;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.ws4d.coap.connection.DefaultCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelListener;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.messages.CoapHeader;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;

public class BasicCoapClient implements CoapChannelListener {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 61616;
    static int counter = 0;
    CoapChannelManager channelManager = null;
    CoapChannel clientChannel = null;

    public static void main(String[] args) {
        System.out.println("Start CoAP Server on port " + PORT);
        BasicCoapClient client = new BasicCoapClient();
        client.channelManager = DefaultCoapChannelManager.getInstance();
    }
    
    public void runTestClient(){
    	try {
			clientChannel = channelManager.connect(this, InetAddress.getByName(SERVER_ADDRESS), PORT);
			CoapMessage coapRequest = clientChannel.createRequest(true, MessageCode.GET);
			clientChannel.sendMessage(coapRequest);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void onLostConnection() {
        System.out.println("Connection lost...");
    }

    @Override
    public void onReceivedMessage(CoapMessage msg) {
        System.out.println("Received message: " + msg.getHeader().toStringShort());
        //TODO: close channel
    }
}
