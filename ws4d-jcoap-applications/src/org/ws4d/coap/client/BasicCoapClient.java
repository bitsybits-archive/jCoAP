package org.ws4d.coap.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.connection.BasicCoapClientChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapClientChannel;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.messages.CoapBlockOption;
import org.ws4d.coap.messages.CoapBlockOption.CoapBlockSize;
import org.ws4d.coap.messages.CoapRequestCode;

/**
 * @author	Christian Lerche <christian.lerche@uni-rostock.de>
 * 			Bjoern Konieczek <bjoern.konieczek@uni-rostock.de>
 */
public class BasicCoapClient implements CoapClient {
    private String SERVER_ADDRESS;
    private int PORT;
    
    static int counter = 0;
    private CoapChannelManager channelManager = null;
    private BasicCoapClientChannel clientChannel = null;
    private Random tokenGen = null;

    
    public BasicCoapClient(String server_addr, int port ){
    	super();
    	this.SERVER_ADDRESS = server_addr;
    	this.PORT = port;
    	this.channelManager = BasicCoapChannelManager.getInstance();
    	this.tokenGen = new Random();
    }
    
    public BasicCoapClient(){
    	super();
    	this.SERVER_ADDRESS = "localhost";
    	this.PORT = Constants.COAP_DEFAULT_PORT;
    	this.tokenGen = new Random();
    }
    
    public boolean connect(){
    	try {
    		clientChannel = (BasicCoapClientChannel) channelManager.connect(this, InetAddress.getByName(SERVER_ADDRESS), PORT);
    	} catch( UnknownHostException e ){
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    
    public CoapRequest createRequest( boolean reliable, CoapRequestCode reqCode ) {
    	return clientChannel.createRequest( reliable, reqCode );
    }
    
    public void sendRequest( CoapRequest request ){
    	if( request.getRequestCode() == CoapRequestCode.PUT || request.getRequestCode() == CoapRequestCode.POST ){
    		if( ( clientChannel.getMaxSendBlocksize() != null || request.getBlock1() != null ) ) {
    			request = clientChannel.addBlockContext( request);
    		}
    	} else if( request.getRequestCode() == CoapRequestCode.GET && ( request.getBlock2() == null && clientChannel.getMaxReceiveBlocksize() != null )) {
    		CoapBlockOption block2 = new CoapBlockOption( 0, false, clientChannel.getMaxReceiveBlocksize() );
    		request.setBlock2( block2 );
    	}
    		
    	clientChannel.sendMessage(request);
    }
    
    public void setReceiveBlockSize( CoapBlockSize size ){
    	if( clientChannel != null )
    		clientChannel.setMaxReceiveBlocksize( size );
    }
    
    public void setSendBlockSize( CoapBlockSize size ){
    	if( clientChannel != null )
    		clientChannel.setMaxSendBlocksize(size);
    }
    
    public byte[] generateRequestToken(int tokenLength ){
   		byte[] token = new byte[tokenLength];
    	tokenGen.nextBytes(token);
    	return token;
    }
    
	@Override
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
		System.out.println("Connection Failed");
	}

	@Override
	public void onResponse(CoapClientChannel channel, CoapResponse response) {
		System.out.println("Received response");
	}
}
