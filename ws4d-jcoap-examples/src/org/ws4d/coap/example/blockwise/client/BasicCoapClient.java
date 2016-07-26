package org.ws4d.coap.example.blockwise.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.BasicCoapClientChannel;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.CoapBlockOption;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

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
    	this.PORT = CoapConstants.COAP_DEFAULT_PORT;
    	this.tokenGen = new Random();
    }
    
    public boolean connect(){
    	try {
    		this.clientChannel = (BasicCoapClientChannel) this.channelManager.connect(this, InetAddress.getByName(this.SERVER_ADDRESS), this.PORT);
    	} catch( UnknownHostException e ){
    		e.printStackTrace();
    		return false;
    	}
    	
    	return true;
    }
    
    public boolean connect( String server_addr, int port ){
    	this.SERVER_ADDRESS = server_addr;
    	this.PORT = port;
    	return this.connect();
    }
    
    
    public CoapRequest createRequest( boolean reliable, CoapRequestCode reqCode ) {
    	return this.clientChannel.createRequest( reliable, reqCode );
    }
    
    public void sendRequest( CoapRequest request ){
    	CoapRequest req = request;
    	if( req.getRequestCode() == CoapRequestCode.PUT || req.getRequestCode() == CoapRequestCode.POST ){
    		if( ( this.clientChannel.getMaxSendBlocksize() != null || req.getBlock1() != null ) ) {
    			req = this.clientChannel.addBlockContext( req);
    		}
    	} else if( req.getRequestCode() == CoapRequestCode.GET && ( req.getBlock2() == null && this.clientChannel.getMaxReceiveBlocksize() != null )) {
    		CoapBlockOption block2 = new CoapBlockOption( 0, false, this.clientChannel.getMaxReceiveBlocksize() );
    		req.setBlock2( block2 );
    	}
    		
    	this.clientChannel.sendMessage(req);
    }
    
    public void setReceiveBlockSize( CoapBlockSize size ){
    	if( this.clientChannel != null )
    		this.clientChannel.setMaxReceiveBlocksize( size );
    }
    
    public void setSendBlockSize( CoapBlockSize size ){
    	if( this.clientChannel != null )
    		this.clientChannel.setMaxSendBlocksize(size);
    }
    
    public byte[] generateRequestToken(int tokenLength ){
   		byte[] token = new byte[tokenLength];
    	this.tokenGen.nextBytes(token);
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
	
	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		System.out.println("Received response");
	}
}
