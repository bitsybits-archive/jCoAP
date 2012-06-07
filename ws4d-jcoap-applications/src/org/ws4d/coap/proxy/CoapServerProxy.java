/*
 * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
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
 * 
 * This work has been sponsored by Siemens Corporate Technology. 
 *
 */
package org.ws4d.coap.proxy;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.BasicCoapResponse;
import org.ws4d.coap.messages.CoapResponseCode;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

public class CoapServerProxy implements CoapServer{
	
    private static final int LOCAL_PORT = 5683;					//port on which the server is listening
    
    //coapOUTq_ receives a coap-response from mapper in case of coap-http
    private ArrayBlockingQueue<ProxyMessageContext> coapOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
    CoapChannelManager channelManager;
    
    //this class sends the response back to the client in case coap-http
    public class CoapSender extends Thread {
    	public void run() {
    		this.setName("CoapSender");
    		while (!Thread.interrupted()) {
    			try {
    					/*FIXME: response can be NULL, than generate Error*/
    					ProxyMessageContext context = coapOutQueue.take();	
    					/* TODO: make cast safe */
    					CoapServerChannel channel = (CoapServerChannel) context.getCoapRequest().getChannel();
    					/* we need to cast to allow an efficient header copy */
    					BasicCoapResponse clientResponse = (BasicCoapResponse) context.getCoapResponse();
    					BasicCoapResponse response = (BasicCoapResponse) channel.createResponse(context.getCoapRequest(), clientResponse.getResponseCode());
						/* copy header and payload */
						response.copyHeaderOptions(clientResponse);
						response.setPayload(clientResponse.getPayload());

						channel.sendMessage(response);
						channel.close();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    		}
    	}   	
    }
    
    //constructor of coapserver-class, initiates the jcoap-components and starts CoapSender
    public CoapServerProxy() {

        channelManager = BasicCoapChannelManager.getInstance();
        channelManager.createServerListener(this, LOCAL_PORT);
        CoapSender sender = new CoapSender();
        sender.start();
    }
    
    //interface-function for the message-queue
    public void receivedResponse(ProxyMessageContext context) {
    	try {
			coapOutQueue.put(context);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    @Override
    public CoapServer onAccept(CoapRequest request) {
        System.out.println("Accept connection...");
        /* accept every incomming connection */
        return this;
    }

    @Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
    	/* draft-08:
    	 *  CoAP distinguishes between requests to an origin server and a request
   			made through a proxy.  A proxy is a CoAP end-point that can be tasked
   			by CoAP clients to perform requests on their behalf.  This may be
   			useful, for example, when the request could otherwise not be made, or
   			to service the response from a cache in order to reduce response time
   			and network bandwidth or energy consumption.
   			
   			CoAP requests to a proxy are made as normal confirmable or non-
			confirmable requests to the proxy end-point, but specify the request
   			URI in a different way: The request URI in a proxy request is
   			specified as a string in the Proxy-Uri Option (see Section 5.10.3),
   			while the request URI in a request to an origin server is split into
   			the Uri-Host, Uri-Port, Uri-Path and Uri-Query Options (see
   			Section 5.10.2).
    	*/
    	URI proxyUri = null;
        
        //-------------------in this case we want a translation to http----------------------------

		try {
			proxyUri = new URI(request.getProxyUri());
		} catch (Exception e) {
			proxyUri = null;
		}

    	if (proxyUri == null){
    		/* PROXY URI MUST BE AVAILABLE */
    		System.out.println("Received a Non Proxy CoAP Request, send error");
    		/*FIXME What is the right error code for this case?*/
    		channel.sendMessage(channel.createResponse(request, CoapResponseCode.Not_Found_404));
    		channel.close();
    		return;    		
    	}
    	
    	/*check scheme, should we translate */
    	boolean translate;
    	if (proxyUri.getScheme().compareToIgnoreCase("http") == 0){
    		translate = true;
    	} else if (proxyUri.getScheme().compareToIgnoreCase("coap") == 0){
    		translate = false;
    	} else {
    		/*unknown scheme, TODO send error*/
    		System.out.println("Unknown Proxy Uri Scheme, send error");
    		/*FIXME What is the right error code for this case?*/
    		channel.sendMessage(channel.createResponse(request, CoapResponseCode.Not_Found_404));
    		channel.close();
    		return;
    	}
    	
    	/* parse URL */
    	try {
    		InetAddress remoteAddress = InetAddress.getByName(proxyUri.getHost());
    		int remotePort = proxyUri.getPort();
    		if (remotePort == -1){
    			remotePort = org.ws4d.coap.Constants.COAP_DEFAULT_PORT;
    		}
    		ProxyMessageContext context = new ProxyMessageContext(request, translate, remoteAddress, remotePort, proxyUri);
    		ProxyMapper.getInstance().putCoapRequest(context);
		} catch (UnknownHostException e) {
			/*bad proxy URI*/
    		System.out.println("Invalid Proxy Uri Scheme, send error");
    		/*FIXME What is the right error code for this case?*/
    		channel.sendMessage(channel.createResponse(request, CoapResponseCode.Not_Found_404));
    		channel.close();
			e.printStackTrace();
		}
    	
    }

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		// TODO Auto-generated method stub
		
	}
   
}

