/**
 * Client Application for Plugtest 2012, Paris, France
 * 
 * Execute with argument Identifier (e.g., TD_COAP_CORE_01)
 */
package org.ws4d.coap.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.BasicCoapSocketHandler;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class PlugtestClient implements CoapClient{
    CoapChannelManager channelManager = null;
    CoapClientChannel clientChannel = null;
    
    HashMap<String, List<String> > serverList;

    CoapRequest request = null; 
    private static Logger logger = Logger.getLogger(BasicCoapSocketHandler.class.getName());
    boolean exitAfterResponse = true;
    String serverAddress = null;
    int serverPort = 0;
    String filter = null;

	public static void main(String[] args) {
//		if (args.length > 4 || args.length < 4) {
//			System.err.println("illegal number of arguments");
//			System.exit(1);
//		}
		
		logger.setLevel(Level.WARNING);
		PlugtestClient client = new PlugtestClient();
		//client.start(args[0], Integer.parseInt(args[1]), args[2], args[3]);
		client.start("127.0.0.1", CoapConstants.COAP_DEFAULT_PORT, "TD_COAP_LINK_02", "");
		
	}
	
	
	public void start(String serverAddress, int serverPort, String testcase, String filter){
		System.out.println("===START=== (Run Test Client: " + testcase + ")");
		//String testId = testcase;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.filter = filter;
		this.serverList = new HashMap<>();
		
		init(false, CoapRequestCode.GET);
		this.request.setUriPath("/.well-known/core");
//		request.setUriPath("/test");
		
		System.out.println("QueryPath: " + this.request.getUriPath() );
		
/*		if (testId.equals("TD_COAP_CORE_01")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_02")) {
			init(true, CoapRequestCode.POST);
			request.setUriPath("/test");
			request.setPayload("Content of new resource /test");
			request.setContentType(CoapMediaType.text_plain);
		} 
		else if (testId.equals("TD_COAP_CORE_03")) {
			init(true, CoapRequestCode.PUT);
			request.setUriPath("/test");
			request.setPayload("Content of new resource /test");
			request.setContentType(CoapMediaType.text_plain);
		} 
		else if (testId.equals("TD_COAP_CORE_04")) {
			init(true, CoapRequestCode.DELETE);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_05")) {
			init(false, CoapRequestCode.GET);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_06")) {
			init(false, CoapRequestCode.POST);
			request.setUriPath("/test");
			request.setPayload("Content of new resource /test");
			request.setContentType(CoapMediaType.text_plain);
		} 
		else if (testId.equals("TD_COAP_CORE_07")) {
			init(false, CoapRequestCode.PUT);
			request.setUriPath("/test");
			request.setPayload("Content of new resource /test");
			request.setContentType(CoapMediaType.text_plain);
		}
		else if (testId.equals("TD_COAP_CORE_08")) {
			init(false, CoapRequestCode.DELETE);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_09")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/separate");
		} 
		else if (testId.equals("TD_COAP_CORE_10")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/test");
			request.setToken("AABBCCDD".getBytes());
		} 
		else if (testId.equals("TD_COAP_CORE_11")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_12")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/seg1/seg2/seg3");
		} 
		else if (testId.equals("TD_COAP_CORE_13")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/query");
			request.setUriQuery("first=1&second=2&third=3");
		} 
		else if (testId.equals("TD_COAP_CORE_14")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/test");
		} 
		else if (testId.equals("TD_COAP_CORE_15")) {
			init(true, CoapRequestCode.GET);
			request.setUriPath("/separate");
		} 
		else if (testId.equals("TD_COAP_CORE_16")) {jcoap-draft18/ws4d-jcoap-plugtest/src/org/ws4d/coap/test
			init(false, CoapRequestCode.GET);
			request.setUriPath("/separate");
		} 
		else if (testId.equals("TD_COAP_LINK_01")) {
			init(false, CoapRequestCode.GET);
			request.setUriPath("/.well-known/core");
		} 
		else if (testId.equals("TD_COAP_LINK_02")) {
			init(false, CoapRequestCode.GET);
			request.setUriPath("/.well-known/core");
			request.setUriQuery("rt=" + this.filter);
		} 
		else {
			System.out.println("===Failure=== (unknown test case)");
			System.exit(-1);
		}		*/
		run();
	}
    
    
	public void init(boolean reliable, CoapRequestCode requestCode) {
		this.channelManager = BasicCoapChannelManager.getInstance();
		this.channelManager.setMessageId(1000);
		
		try {
			this.clientChannel = this.channelManager.connect(this, InetAddress.getByName(this.serverAddress), this.serverPort);
			if (this.clientChannel == null){
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
		if(this.request.getPayload() != null){
			System.out.println("Send Request: " + this.request.toString() + " (" + new String(this.request.getPayload()) +")");
		}else {
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
		if (response.getPayload() != null){
			System.out.println("Response: " + response.toString() + " (" + new String(response.getPayload()) +")");
		} else {
			System.out.println("Response: " + response.toString());
		}
		if (this.exitAfterResponse){
			System.out.println("===END===");
			System.exit(0);
		}
	}

	public class WaitAndExit implements Runnable
	  {
	    public void run()
	    {
	    	try {
	    		Thread.sleep(1000);
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	    	System.out.println("===END===");
	    	System.exit(0);
	    }
	}

	@Override
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
		System.out.println("Received Multicast Response");
		
	}
}