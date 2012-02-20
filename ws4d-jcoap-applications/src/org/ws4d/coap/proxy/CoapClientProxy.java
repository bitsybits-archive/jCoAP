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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ws4d.coap.connection.DefaultCoapChannelManager;
import org.ws4d.coap.connection.DefaultCoapSocketHandler;
import org.ws4d.coap.interfaces.CoapChannel;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapClient;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.messages.CoapHeaderOption;
import org.ws4d.coap.messages.CoapHeaderOptions.HeaderOptionNumber;
import org.ws4d.coap.tools.UriParser;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class CoapClientProxy {
	private static Logger logger = Logger.getLogger(DefaultCoapSocketHandler.class.getName());

	// queue is used to receive coap-requests from mapper
	private ArrayBlockingQueue<ProxyMessageContext> coapClientRequestQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
	private HashMap<CoapChannel, ProxyMessageContext> coapContextMap = new HashMap<CoapChannel, ProxyMessageContext>(100);

	private CoapRequestListenerThread coapRequestListener;

	public CoapClientProxy() {
		super();
		CoapRequestListenerThread coaprequestlistenerthread = new CoapRequestListenerThread();
		this.coapRequestListener = coaprequestlistenerthread;
		coaprequestlistenerthread.start();
	}

	// access-function for other classes to pass a message
	public void makeRequest(ProxyMessageContext context) {
		try {
			coapClientRequestQueue.put(context);

			synchronized (coapRequestListener) {
				coapRequestListener.notify();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
	private boolean checkRemoteAddress(ProxyMessageContext context) {
		CoapMessage request = context.getCoapRequest();

		/* Check remote Host */
		if (context.getRemoteAddress() == null) {
			/* no remote address set */
			InetAddress remoteAddress = null;

			/* retrieve Host from CoapHeader Option */
			if (request.getHeader().getOptionCount() > 0) {
				for (CoapHeaderOption option : request.getHeader()
						.getCoapHeaderOptions()) {
					if (option.getOptionNumber() == HeaderOptionNumber.Uri_Host) {
						String value = new String(option.getOptionValue());
						try {
							remoteAddress = InetAddress.getByName(value);
							context.setRemoteAddress(remoteAddress);
						} catch (UnknownHostException e) {
							remoteAddress = null;
						}
						break;
					}
				}
			}

			/* check if Host could be found */
			if (remoteAddress == null) {
				logger.log(Level.INFO,"Invalid Uri Host, request will be dropped!");
				return false;
			}
			
		}

		/* Check remote port */
		if (context.getRemotePort() == 0) {
			/* Coap Default Port */
			context.setRemotePort(org.ws4d.coap.Constants.COAP_DEFAULT_PORT);
			if (request.getHeader().getOptionCount() > 0) {
				for (CoapHeaderOption option : request.getHeader()
						.getCoapHeaderOptions()) {
					if (option.getOptionNumber() == HeaderOptionNumber.Uri_Port) {
						String value = new String(option.getOptionValue());
						/* set port from Coap Header */
						context.setRemotePort(Integer.parseInt(value));
						break;
					}
				}
			}

		}
		

		
		
		return true;
	}
		
		//this thread waits for a message in coapINq_ and sends it
		class CoapRequestListenerThread extends Thread implements CoapClient{
	        
	        public void run() {

			this.setName("CoapRequestListenerThread");

			// start jcoap-framework
			CoapChannelManager connectionManager = DefaultCoapChannelManager
					.getInstance();

			while (!Thread.interrupted()) {
				ProxyMessageContext context = null;
				try {

					synchronized (this) {
						while (coapClientRequestQueue.isEmpty())
							try {
								wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						context = coapClientRequestQueue.take(); // blocking
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} 


				if (checkRemoteAddress(context)){
					// create channel
					CoapChannel channel;
					channel = connectionManager.connect(this, context.getRemoteAddress(), context.getRemotePort());
					if (channel != null) {
						/* save the request in a hashmap to assign the response to the right request */
						coapContextMap.put(channel, context);
						// send message
						CoapMessage originRequest = context.getCoapRequest();
						CoapMessage request= channel.createRequest(originRequest.isReliable(), originRequest.getMessageCode());
						request.copyHeaderOptions(originRequest); 

						if (!context.isTranslate()){
							/* CoAP to CoAP */
							/* check path: if this is a coap-coap proxy request than the proxy uri needs to be translated to path options
							 * and the proxy uri needs to be removed as this is no longer a proxy request */
							String[] pathElements = UriParser.getPathElements(context.getUri().getPath());
							for (String pathElement : pathElements) {
								request.getHeader().addOption(HeaderOptionNumber.Uri_Path, pathElement.getBytes());
							}
							request.getHeader().getCoapHeaderOptions().removeOption(HeaderOptionNumber.Proxy_Uri);
					}
						request.setPayload(originRequest.getPayload());
						channel.sendMessage(request);
					}
				} else {
					/* could not determine the final destination */
					System.out.println("Error: unknown host: " + context.getRemoteAddress().getHostName());
				}
			}
		}
	        
		@Override
		public void onResponse(CoapChannel channel, CoapMessage response) {
			ProxyMessageContext context = coapContextMap.get(channel);
			channel.close();
			if (context != null){
				context.setCoapResponse(response);
				Mapper.getInstance().putCoapResponse(context);
			}
		}

		@Override
		public void onConnectionFailed(CoapChannel channel,
				boolean notReachable, boolean resetByServer) {
			ProxyMessageContext context = coapContextMap.get(channel);
			channel.close();
			if (context != null){
				System.out.println("Error: Coap Client Connection failed!");
				context.setCoapResponse(null); //null indicates no response
				Mapper.getInstance().putCoapResponse(context);
			}
		}
	}
}
