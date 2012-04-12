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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ArrayBlockingQueue;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.ssl.SSLServerIOEventDispatch;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpRequestHandlerRegistry;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */

public class HttpServerNIO extends Thread{

	//the queue receives the responses from mapper-module
	static private ArrayBlockingQueue<ProxyMessageContext> httpOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
	
	static private HttpResponder httpResponder;
	
	static private int PORT = 8080;
	
	//if http-server should use ssl set it to true
	public static boolean SSLSERVER = false;
	
	//interface-function for other classes/modules
	public void receivedHttpResponse(ProxyMessageContext context) {
		try {
			httpOutQueue.put(context);
			
            synchronized (httpResponder) {
            	httpResponder.notify();
            }
            
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		
		this.setName("HTTP_NIO_Server");				
		
		//parameters for connection
        HttpParams params = new SyncBasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
                
        //needed by framework, don't need any processors except the connection-control
        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                new ResponseConnControl()
        });
        
        //create own service-handler with bytecontentlistener-class
        ModifiedAsyncNHttpServiceHandler handler = new ModifiedAsyncNHttpServiceHandler(
                httpproc, new DefaultHttpResponseFactory(),
                new DefaultConnectionReuseStrategy(), params);
        
        // Set up request handlers, use the same request-handler for all uris
        NHttpRequestHandlerRegistry reqistry = new NHttpRequestHandlerRegistry();
        reqistry.register("*", new ProxyHttpRequestHandler());
        handler.setHandlerResolver(reqistry);
        
        
		try {
			//create and start responder-thread
			HttpResponder responder = new HttpResponder();
			httpResponder = responder;
			responder.start();
			
			//ioreactor is used by nio-framework to listen and react to http connections
			//2 dispatcher-threads are used to do the work
			ListeningIOReactor ioReactor = new DefaultListeningIOReactor(2, params);
			
			//two cases are differentiated: ssl(https) or http
			if (SSLSERVER) {
				
				//ssl-server is experimental, but worked with cert "mySrvKeystore"
				SSLContext sslcontext = null;
				try {			
					
					ClassLoader cl = org.ws4d.coap.proxy.HttpServerNIO.class.getClassLoader();
					URL url = cl.getResource("mySrvKeystore.keystore");			//has to be in /PROJECTNAME/bin
					KeyStore keystore  = KeyStore.getInstance("jks");
					keystore.load(url.openStream(), "anne1306".toCharArray());	//stream cert in keystore-object
					KeyManagerFactory kmfactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm());
					kmfactory.init(keystore, "anne1306".toCharArray());
					KeyManager[] keymanagers = kmfactory.getKeyManagers(); 
					sslcontext = SSLContext.getInstance("TLS");
					sslcontext.init(keymanagers, null, null);
					//sslcontext is ready and used in sslioeventdispatch
	
				} catch (KeyStoreException e2) {
					e2.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (CertificateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnrecoverableKeyException e) {
					e.printStackTrace();
				} catch (KeyManagementException e) {
					e.printStackTrace();
				}
				
				//ioeventdispatch is used to handle the events from ioreactor
				SSLServerIOEventDispatch sslioeventdispatch = new SSLServerIOEventDispatch(handler,sslcontext ,params);
				
				ioReactor.listen(new InetSocketAddress(PORT));
				ioReactor.execute(sslioeventdispatch);
			}
			else {
				//this is the well-tested case, normal http-server without encryption
				//encryption disables easy debugging of packets in wireshark
				IOEventDispatch ioeventdispatch = new DefaultServerIOEventDispatch(handler, params);
				
				ioReactor.listen(new InetSocketAddress(PORT));
				ioReactor.execute(ioeventdispatch);
			}	
			
			
		} catch (IOReactorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	static class ProxyHttpRequestHandler implements NHttpRequestHandler  {


        public ProxyHttpRequestHandler() {
            super();
        }

		@Override
		public ConsumingNHttpEntity entityRequest(
				HttpEntityEnclosingRequest arg0, HttpContext arg1)
				throws HttpException, IOException {
			return null;
		}

		//handle() is called when a request is received
		//response is automatically generated by HttpProcessor, but use response from mapper
		//trigger is used for asynchronous response, see java-documentation
		@Override
		public void handle(final HttpRequest request, final HttpResponse response,
				final NHttpResponseTrigger trigger, HttpContext con)
				throws HttpException, IOException {
			
//				HttpRequestProxy reqX = new HttpRequestProxy(request,generateMsgID());	//create httprequestx to carry message-id  
			
			URI uri = Mapper.getHttpRequestUri(request);
			if (uri == null){
				trigger.submitResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "Bad Header: Host"));
			} else {

				InetAddress remoteAddress = InetAddress.getByName(uri.getHost());
				int port = uri.getPort();
				if (port == -1) {
					port = org.ws4d.coap.Constants.COAP_DEFAULT_PORT;
				}

				ProxyMessageContext context = new ProxyMessageContext(request, remoteAddress, 0, uri, trigger);
				Mapper.getInstance().putHttpRequest(context); // put request
																	// to mapper
																	// for
																	// processing/translating
			}
		}
    }
	
	//this class waits for a response and sends it when got one from mapper
	public static class HttpResponder extends Thread {
        	
        public void run() {
        	
        	this.setName("HttpResponder");

            while (!Thread.interrupted()) {
                try {
                	
                	synchronized(this) {
                        while (httpOutQueue.isEmpty())
    						try {
    							wait();
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}
                    }
                    
                	ProxyMessageContext context = httpOutQueue.take(); 	
                	HttpResponse httpResponse = context.getHttpResponse();
                	NHttpResponseTrigger trigger = context.getTrigger();
               		trigger.submitResponse(httpResponse);

                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }
	
}
