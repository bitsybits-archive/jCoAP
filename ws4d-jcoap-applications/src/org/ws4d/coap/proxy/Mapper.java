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
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.entity.ConsumingNHttpEntityTemplate;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.messages.CoapHeader;
import org.ws4d.coap.messages.CoapHeaderOption;
import org.ws4d.coap.messages.CoapHeaderOptions.HeaderOptionNumber;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapMessageCode.MessageCode;
import org.ws4d.coap.messages.CoapPacketType;
import org.ws4d.coap.messages.DefaultCoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Andy Seidel <andy.seidel@uni-rostock.de>
 */
public class Mapper {
	
	static final int DEFAULT_MAX_AGE = 60000; //Max Age Default in ms 

	private static ArrayBlockingQueue<ProxyMessageContext> httpInQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);		
	private static ArrayBlockingQueue<ProxyMessageContext> httpOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
	private static ArrayBlockingQueue<ProxyMessageContext> coapInQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
	private static ArrayBlockingQueue<ProxyMessageContext> coapOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
	
	//introduce other needed classes for communication
	private CoapClientProxy coapClient;
	private CoapServerProxy coapServer;
	private HttpServerNIO httpServerNIO;
	private HttpClientNIO httpClientNIO;
	private static ProxyCache cache;	
	private static HttpRequestHandler httpRequestHandler;
	private static CoapRequestHandler coapRequestHandler;
	private static HttpResponseHandler httpResponseHandler;
	private static CoapResponseHandler coapResponseHandler;
	
	private static Mapper instance;

    public synchronized static Mapper getInstance() {
        if (instance == null) {
            instance = new Mapper();
        }
        return instance;
    }

    private Mapper() {
		HttpRequestHandler httprequesthandler = new HttpRequestHandler();
		httpRequestHandler = httprequesthandler;
		httprequesthandler.start();
		
		CoapResponseHandler coapresponsehandler = new CoapResponseHandler();
		coapResponseHandler = coapresponsehandler;
		coapresponsehandler.start();
		
		HttpResponseHandler httpresponsehandler = new HttpResponseHandler();
		httpResponseHandler = httpresponsehandler;
		httpresponsehandler.start();
		
		CoapRequestHandler crh = new CoapRequestHandler();
		coapRequestHandler = crh;
		crh.start();

		cache = new ProxyCache();
    }
    
    public void setCacheTime(int cacheTime){
    	cache.setCacheTime(cacheTime);
    }
	
	//this class handles incoming http-requests; used in cases http-coap, http-http
	class HttpRequestHandler extends Thread {
			
	        public void run() {
	        	
	        	this.setName("HttpRequestHandler");
	        		        	
	            while (!Thread.interrupted()) {
	                try {
	                	
	                	//wait until there is nothing to do
	                	synchronized(this) {
	                        while (httpInQueue.isEmpty())
	    						try {
	    							wait();
	    						} catch (InterruptedException e) {
	    							e.printStackTrace();
	    						}
	                    }	                	
	                	
	                	ProxyMessageContext context = httpInQueue.take(); 				//blocking operation

					// at this point response is not in cache and should be
					// mapped to coap-request
					// do not translate methods: OPTIONS,TRACE,CONNECT
					// in that case answer "Not Implemented"
					if (HttpRequestMethodSupported(context.getHttpRequest())) {
						// perform request-transformation
						/* try to get from cache */
						
						CoapMessage response = cache.getCoapRes(context.getUri());
						if (response != null){
							/* answer from cache */
							context.setCoapResponse(response);
							context.setFromCache(true); //avoid "rechaching"
							putCoapResponse(context);
							System.out.println("served from cache");
						} else {
							/* not cached */
							CoapMessage request = httpRequestToCoapRequest(context.getHttpRequest());
							
							if (request != null) {
								context.setTranslatedCoapRequest(request);
								coapClient.makeRequest(context);
							} else {
								//if something went wrong while translation
								System.out.println("Error: request transformation error, serving error-message to httpserver");
								context.setHttpResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"));
								httpServerNIO.receivedHttpResponse(context);
							}
						}

					} else {
						// if method is unsupported answer with error-code
						HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_IMPLEMENTED, "Not Implemented");
						context.setHttpResponse(httpResponse);
						httpServerNIO.receivedHttpResponse(context);
					}
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                } 
	            }
	        }
	    }
	
	//this class handles incoming coap-requests; used in case coap-http
	class CoapRequestHandler extends Thread {
			
	        public void run() {
	        	this.setName("CoapRequestHandler");
	            while (!Thread.interrupted()) {
	                try {
	                	
	                	//wait until there is nothing to do
	                	synchronized(this) {
	                        while (coapInQueue.isEmpty())
	    						try {
	    							wait();
	    						} catch (InterruptedException e) {
	    							e.printStackTrace();
	    						}
	                    }
	                    
	                	ProxyMessageContext context = coapInQueue.take(); 

	                	if (context.isTranslate()){
	                		/* coap to http */
		                	HttpResponse response = cache.getHttpRes(context.getUri());
							if (response != null){
								/* answer from cache */
								context.setHttpResponse(response);
								context.setFromCache(true); //avoid "rechaching"
								putHttpResponse(context);
								System.out.println("served from cache");
							} else {	                		
								HttpRequest httpRequest = coapRequestToHttpRequest(context);
		                		context.setTranslatedHttpRequest(httpRequest);
		                		httpClientNIO.makeHttpRequest(context);       //send to http-client
							}
	                	} else {
	                		/* coap to coap */
		                	CoapMessage response = cache.getCoapRes(context.getUri());
							if (response != null){
								/* answer from cache */
								context.setCoapResponse(response);
								context.setFromCache(true); //avoid "rechaching"
								putCoapResponse(context);
								System.out.println("served from cache");
							} else {
								coapClient.makeRequest(context);				//send to coap-client
							}	                		
	                	}
	                } catch (InterruptedException ex) {
	                    break;
	                }
	            }
	        }
	    }
	
	//this class handles coap-responses; used in case http-coap
	class CoapResponseHandler extends Thread {
        
        public void run() {
        	
        	this.setName("CoapResponseHandler");
        	
            while (!Thread.interrupted()) {
                try {
                	
                	synchronized(this) {
                        while (coapOutQueue.isEmpty())
    						try {
    							wait();
    						} catch (InterruptedException e) {    							
    							e.printStackTrace();
    						}
                    }
                    
                	ProxyMessageContext context = coapOutQueue.take(); 						//blocking	operation 
                	if(!context.isFromCache()){
                		/* don't cache already cached elements (loop)*/
                		cache.put(context);
                	}
                	
                	if (context.isTranslate()){
                		/* translate to HTTP*/
                		HttpResponse response;
                		if (context.getCoapResponse() != null){
                			/* received no response TODO: implement a better error handling*/
                			response = coapResponseToHttpResponse(context);	    //translate
                			if (response == null){                				//error in translation-process, send back error-message
                				response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
                			}
                		} else {
            				response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Client not reachable");
                		}
                		
                		context.setHttpResponse(response);
                		httpServerNIO.receivedHttpResponse(context);
                	} else {
                		/* just coap to coap */
                		coapServer.receivedResponse(context);
                	}
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }
	
	class HttpResponseHandler extends Thread {
        
        public void run() {
        	
        	this.setName("HttpResponseHandler");
        	
            while (!Thread.interrupted()) {
                try {
                	
                	//wait until there is nothing to do
                	synchronized(this) {
                        while (httpOutQueue.isEmpty())
    						try {
    							wait();
    						} catch (InterruptedException e) {
    							e.printStackTrace();
    						}
                    }
                	
                   
                	ProxyMessageContext context = httpOutQueue.take(); 		//blocking operation
					CoapMessage coapResponse = httpResponseToCoapResponse(context); // translate
                	
					if (coapResponse != null) {
//						if (cacheEnabled) {
//							String msgID = new String("" + context.getMsgID());
//							LinkedList<String> uri = requestToStringlist(req);
//							if (uri != null) {
//								cache.put(uri, context);
//							} else
//								System.out
//										.println("Error: uri is null --> nothing cached");
//						}
					}


					context.setCoapResponse(coapResponse);
					coapServer.receivedResponse(context);
                	
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
	}     
	
	//in case of translation from coap-response to http-response
	//message-code-translation depends on coap-responsecode and http-request-method
	public static void setHttpMsgCode(CoapMessage coapresponse, String request_method, HttpResponse httpresponse) {
		
		MessageCode msgcode = coapresponse.getMessageCode();
		
		switch(msgcode) {
			case OK_200: {
				httpresponse.setStatusCode(HttpStatus.SC_OK);
				httpresponse.setReasonPhrase("Ok");
				break;
			}
			case Created_201: {
				if (request_method.contains("post") || request_method.contains("put")) {				
					httpresponse.setStatusCode(HttpStatus.SC_CREATED);
					httpresponse.setReasonPhrase("Created");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpresponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpresponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Deleted_202: {
				if (request_method.contains("delete")) {				
					httpresponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
					httpresponse.setReasonPhrase("No Content");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpresponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpresponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Valid_203: {
				httpresponse.setStatusCode(HttpStatus.SC_NOT_MODIFIED);
				httpresponse.setReasonPhrase("Not Modified");
				break;
			}
			case Changed_204: {
				if (request_method.contains("post") || request_method.contains("put")) {				
					httpresponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
					httpresponse.setReasonPhrase("No Content");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpresponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpresponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Content_205: {
				if (request_method.contains("get")) {
					httpresponse.setStatusCode(HttpStatus.SC_OK);
					httpresponse.setReasonPhrase("OK");
				}
				else {
					System.out.println("wrong msgCode for request-method!");
					httpresponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
					httpresponse.setReasonPhrase("Method Failure");
				}
				break;
			}
			case Bad_Request_400: {
				httpresponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpresponse.setReasonPhrase("Bad Request");
				break;
			}
			case Unauthorized_401: {
				httpresponse.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
				httpresponse.setReasonPhrase("Unauthorized");
				break;
			}
			case Bad_Option_402: {
				httpresponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpresponse.setReasonPhrase("Bad Option");
				break;
			}
			case Forbidden_403: {
				httpresponse.setStatusCode(HttpStatus.SC_FORBIDDEN);
				httpresponse.setReasonPhrase("Forbidden");
				break;
			}
			case Not_Found_404: {
				httpresponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
				httpresponse.setReasonPhrase("Not Found");
				break;
			}
			case Method_Not_Allowed_405: {
				httpresponse.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				httpresponse.setReasonPhrase("Method Not Allowed");
				break;
			}
			case Precondition_Failed_412: {
				httpresponse.setStatusCode(HttpStatus.SC_PRECONDITION_FAILED);
				httpresponse.setReasonPhrase("Precondition Failed");
				break;
			}
			case Request_Entity_To_Large_413: {
				httpresponse.setStatusCode(HttpStatus.SC_REQUEST_TOO_LONG);
				httpresponse.setReasonPhrase("Request Too Long : Request entity too large");
				break;
			}
			case Unsupported_Media_Type_415: {
				httpresponse.setStatusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
				httpresponse.setReasonPhrase("Unsupported Media Type");
				break;
			}
			case Internal_Server_Error_500: {
				httpresponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				httpresponse.setReasonPhrase("Internal Server Error");
				break;
			}
			case Not_Implemented_501: {
				httpresponse.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
				httpresponse.setReasonPhrase("Not Implemented");
				break;
			}
			case Bad_Gateway_502: {
				httpresponse.setStatusCode(HttpStatus.SC_BAD_GATEWAY);
				httpresponse.setReasonPhrase("Bad Gateway");
				break;
			}
			case Service_Unavailable_503: {
				httpresponse.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
				httpresponse.setReasonPhrase("Service Unavailable");
				break;
			}
			case Gateway_Timeout_504: {
				httpresponse.setStatusCode(HttpStatus.SC_GATEWAY_TIMEOUT);
				httpresponse.setReasonPhrase("Gateway Timeout");
				break;
			}
			case Proxying_Not_Supported_505: {
				httpresponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				httpresponse.setReasonPhrase("Internal Server Error : Proxying not supported");
				break;
			}
			case UNKNOWN: {
				httpresponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpresponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
				break;
			}
			default: {
				httpresponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
				httpresponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
				break;
			}
		}		
	}

	//sets the coap-response code under use of http-status code; used in case coap-http
	public static void setCoapMsgCode(CoapMessage coapResponse, String request_method, HttpResponse httpresponse) {
		
		//TODO: add cases in which http-code is the same, but coap-code is different, look at response-code-mapping-table
		switch(httpresponse.getStatusLine().getStatusCode()) {
			case HttpStatus.SC_CREATED:
			{
				coapResponse.setMessageCode(MessageCode.Created_201);
				break;
			}
			case HttpStatus.SC_NO_CONTENT:				
			{
				if (request_method.toLowerCase() == "delete") {
					coapResponse.setMessageCode(MessageCode.Deleted_202);
					break;
				}
				else {
					coapResponse.setMessageCode(MessageCode.Changed_204);
					break;
				}				
			}
			case HttpStatus.SC_NOT_MODIFIED:
			{
				coapResponse.setMessageCode(MessageCode.Valid_203);
				break;
			}
			case HttpStatus.SC_OK:
			{
				coapResponse.setMessageCode(MessageCode.Content_205);
				break;
			}
			case HttpStatus.SC_UNAUTHORIZED:
			{
				coapResponse.setMessageCode(MessageCode.Unauthorized_401);
				break;
			}
			case HttpStatus.SC_FORBIDDEN:
			{
				coapResponse.setMessageCode(MessageCode.Forbidden_403);
				break;
			}
			case HttpStatus.SC_NOT_FOUND:
			{
				coapResponse.setMessageCode(MessageCode.Not_Found_404);
				break;
			}
			case HttpStatus.SC_METHOD_NOT_ALLOWED:
			{
				coapResponse.setMessageCode(MessageCode.Method_Not_Allowed_405);
				break;
			}
			case HttpStatus.SC_PRECONDITION_FAILED:
			{
				coapResponse.setMessageCode(MessageCode.Precondition_Failed_412);
				break;
			}
			case HttpStatus.SC_REQUEST_TOO_LONG:
			{
				coapResponse.setMessageCode(MessageCode.Request_Entity_To_Large_413);
				break;
			}
			case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
			{
				coapResponse.setMessageCode(MessageCode.Unsupported_Media_Type_415);
				break;
			}
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
			{
				coapResponse.setMessageCode(MessageCode.Internal_Server_Error_500);
				break;
			}
			case HttpStatus.SC_NOT_IMPLEMENTED:
			{
				coapResponse.setMessageCode(MessageCode.Not_Implemented_501);
				break;
			}
			case HttpStatus.SC_BAD_GATEWAY:
			{
				coapResponse.setMessageCode(MessageCode.Bad_Gateway_502);
				break;
			}
			case HttpStatus.SC_SERVICE_UNAVAILABLE:
			{
				coapResponse.setMessageCode(MessageCode.Service_Unavailable_503);
				break;
			}
			case HttpStatus.SC_GATEWAY_TIMEOUT:
			{
				coapResponse.setMessageCode(MessageCode.Gateway_Timeout_504);
				break;
			}
			default:
			{
				coapResponse.setMessageCode(MessageCode.Bad_Request_400);
				break;
			}
		}
	}
	
	//translating-functions in case of http-coap
	public static CoapMessage httpRequestToCoapRequest(HttpRequest request) {		

		//analyze method:
		MessageCode mcode = GetCoapMessageCode(request);		//special handling of "head" is performed in that function
		if (mcode != null) {
			switch (mcode) {
			case GET: 
			{
				return handleHttpGETrequest(request);
			}
			case PUT: 
			{
				return handleHttpPUTrequest(request);
			}
			case POST: 
			{
				return handleHttpPOSTrequest(request);
			}
			case DELETE: 
			{
				return handleHttpDELETErequest(request);
			}
			default:
				return null;
			}
		}
		else {
			System.out.println("Error in Mapper-Thread: Message-Code is invalid");
			return null;			
		}				
	}
	
	public static HttpResponse coapResponseToHttpResponse(ProxyMessageContext context) {
		CoapMessage coapResponse = context.getCoapResponse();

		//create a response-object, set http version and assume a default state of ok
		HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		
		// differ between methods is necessary to set the right status-code
		String request_method = context.getHttpRequest().getRequestLine().getMethod();

		if (request_method.toLowerCase().contains("get"))
			httpResponse = handleCoapGETresponse(coapResponse, httpResponse);
		else if (request_method.toLowerCase().contains("put"))
			httpResponse = handleCoapPUTresponse(coapResponse, httpResponse);
		else if (request_method.toLowerCase().contains("post"))
			httpResponse = handleCoapPOSTresponse(coapResponse, httpResponse);
		else if (request_method.toLowerCase().contains("delete"))
			httpResponse = handleCoapDELETEresponse(coapResponse, httpResponse);
		else {
			System.out
					.println("error in translating coap-response to http-response!");
			httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					HttpStatus.SC_INTERNAL_SERVER_ERROR,
					"Internal Server Error");
		}

		return httpResponse;
	}

	//translating-functions in case of coap-http
	public static HttpRequest coapRequestToHttpRequest(ProxyMessageContext context) {
		CoapMessage request = context.getCoapRequest();
		HttpRequest httpRequest = null;
		MessageCode code = request.getMessageCode();
		switch (code) {
			case GET:
			{
				httpRequest = handleCoapGETrequest(request, context.getUri().toString());
				break;
			}
			case PUT:
			{
				httpRequest = handleCoapPUTrequest(request, context.getUri().toString());
				break;
			}
			case POST:
			{
				httpRequest = handleCoapPOSTrequest(request, context.getUri().toString());
				break;
			}
			case DELETE:
			{
				httpRequest = handleCoapDELETErequest(request, context.getUri().toString());
				break;
			}
			default:
			{
				break;
			}
		}
		return httpRequest;
	}
	
	public static CoapMessage httpResponseToCoapResponse(ProxyMessageContext context) {
		

		CoapMessage coapResponse = new DefaultCoapMessage(CoapPacketType.ACK, MessageCode.Content_205, 0);
		
		//get the coap-request to extract the method-code
		String method = "get";
		CoapMessage request = context.getCoapRequest();
		if (request != null) {
			method = request.getMessageCode().toString();
		}
		
		//set the response-code according to response-code-mapping-table
		setCoapMsgCode(coapResponse, method, context.getHttpResponse());

		//TODO: translate header-options
		
		//assume in this case a string-entity
		//TODO: add more entity-types
		CoapHeader header = coapResponse.getHeader();
		header.addOption(1, new String(""+0).getBytes());		
		
		String entity = "";
		try {
			entity = EntityUtils.toString(context.getHttpResponse().getEntity());
			
		} catch (org.apache.http.ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		coapResponse.setPayload(entity);

		return coapResponse;
	}
		
	//mediatype-mapping:
	public static LinkedList<Integer> httpmediatypeTOcoapmediatype(String mediatype) {
		
		LinkedList<Integer> mediatypes = new LinkedList<Integer>();
		String[] type_subtype = mediatype.split(",");
		for (String value : type_subtype) {
			if (value.toLowerCase().contains("text") && value.toLowerCase().contains("plain")) {
				mediatypes.add(CoapMediaType.text_plain);
			}
			else if (value.toLowerCase().contains("application")) {		//value is for example "application/xml;q=0.9"
				String[] subtypes = value.toLowerCase().split("/");
				String subtype = "";
				
				if (subtypes.length == 2) {
					subtype = subtypes[1];								//subtype is for example now "xml;q=0.9"
				}
				else {
					System.out.println("Error in reading Mediatypes!");
				}
				
				//extract the subtype-name and remove the quality identifiers:
				String[] subname = subtype.split(";");
				String name ="";
				
				if (subname.length > 0) {
					name = subname[0];									//name is for example "xml"
				}
				else {
					System.out.println("Error in reading Mediatypes!");
				}
				
				if (name.contentEquals("link-format")) {
					mediatypes.add(CoapMediaType.link_format);
				}
				if (name.contentEquals("xml")) {
					mediatypes.add(CoapMediaType.xml);
				}
				if (name.contentEquals("octet-stream")) {
					mediatypes.add(CoapMediaType.octet_stream);
				}
				if (name.contentEquals("exi")) {
					mediatypes.add(CoapMediaType.exi);
				}
				if (name.contentEquals("json")) {
					mediatypes.add(CoapMediaType.json);
				}
			}
		}
		//if there is the value "*/*", then all media-types are accepted, mediatypes may then be empty
		return mediatypes;
	}
		
	//translate request-header-options in case of http-coap
	public static CoapMessage headerTranslateHttpToCoap(HttpRequest request, CoapMessage coapRequest) {
		CoapHeader header = coapRequest.getHeader();
		URI uri = null;		
		
		//construct uri for later use
		try {
			uri = new URI(request.getRequestLine().getUri());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		
		//Content-Type is in response only
		
		//Max-Age is in response only

		//Proxy-Uri doesn't matter for this purpose
		
		//ETag:
		if (request.containsHeader("Etag")) {
			Header[] headers = request.getHeaders("Etag");
			if (headers.length > 0) {
				for (int i=0; i < headers.length; i++) {
					String header_value = headers[i].getValue();
					CoapHeaderOption option_etag = new CoapHeaderOption(HeaderOptionNumber.Etag, header_value.getBytes());
					header.addOption(option_etag);
				}
			}
		}
		
		//Uri-Host:		
		//don't needs to be there
		
		
		//Location-Path is in response-only
		//Location-Query is in response-only
		
		//Uri-Path:
		//this is the implementation according to coap-rfc section 6.4		
		//first check if uri is absolute and that it has no fragment
		if (uri.isAbsolute() && uri.getFragment() == null) {
			String[] paths = uri.getPath().split("/");
			for (String path : paths) {
				if (!path.isEmpty()) {
					CoapHeaderOption option_uri_path = new CoapHeaderOption(HeaderOptionNumber.Uri_Path, path.getBytes());	
					header.addOption(option_uri_path);
				}
			}
		}
		else {
			System.out.println("error: uri has wrong format, adding no uri-path-header!");
		}
		
		//Token is the same number as msgID, not needed now
		//in future development it should be generated here
		
		//Accept: possible values are numeric media-types
		if (request.containsHeader("Accept")) {
			Header[] headers = request.getHeaders("Accept");
			if (headers.length > 0) {
				for (int i=0; i < headers.length; i++) {
					String header_value = headers[i].getValue();
					for (Integer mediatype : httpmediatypeTOcoapmediatype(header_value)) {
						CoapHeaderOption option_accept = new CoapHeaderOption(HeaderOptionNumber.Accept, new String(""+mediatype).getBytes());
						header.addOption(option_accept);
					}
				}
			}
		}
		
		//if-match:
		if (request.containsHeader("If-Match")) {
			Header[] headers = request.getHeaders("If-Match");
			if (headers.length > 0) {
				for (int i=0; i < headers.length; i++) {
					String header_value = headers[i].getValue();
					CoapHeaderOption option_ifmatch = new CoapHeaderOption(HeaderOptionNumber.If_Match, header_value.getBytes());
					header.addOption(option_ifmatch );
				}
			}
		}
		
		//Uri-Query:
		//this is the implementation according to coap-rfc section 6.4		
		//first check if uri is absolute and that it has no fragment
		if (uri.isAbsolute() && uri.getFragment() == null) {
			if (uri.getQuery() != null) {						//only add options if there are some
				String[] querys = uri.getQuery().split("&");
				for (String query : querys) {
					CoapHeaderOption option_uri_query = new CoapHeaderOption(HeaderOptionNumber.Uri_Query, query.getBytes());	
					header.addOption(option_uri_query);
				}
			}
		}
		else {
			System.out.println("error: uri has wrong format, adding no uri-query-header!");
		}
		
		
		//If-None-Match:
		if (request.containsHeader("If-None-Match")) {
			Header[] headers = request.getHeaders("If-None-Match");
			if (headers.length > 0) {
				if (headers.length > 1) {
					System.out.println("multiple headers in request, ignoring all except the first");
				}
				String header_value = headers[0].getValue();
				CoapHeaderOption option_ifnonematch = new CoapHeaderOption(HeaderOptionNumber.If_None_Match, header_value.getBytes());
				header.addOption(option_ifnonematch);
			}
		}
				
		return coapRequest;
	}
		
	//translate response-header-options in case of http-coap
	public static HttpResponse headerTranslateCoapToHttp(CoapMessage coapResponse, HttpResponse httpResponse) {
		
		long max_age = DEFAULT_MAX_AGE;		
		
		//investigate all coap-headers and set corresponding http-headers
		for (CoapHeaderOption option: coapResponse.getHeader().getCoapHeaderOptions()) {
			switch (option.getOptionNumber()) {
			case HeaderOptionNumber.Content_Type:
			{
				int type = -1;
				try {
					byte[] array = option.getOptionValue();
					if (array.length != 1) {
						System.out.println("Error: Option Content-Type is empty!");
						httpResponse.addHeader("Content-Type", "text/plain");
						break;
					}
					type = (int)option.getOptionValue()[0];
				}
				catch (NumberFormatException ex) {
					System.out.println("Error: Option Content-Type contains no integer!");
				}
				switch (type) {
				case CoapMediaType.text_plain:
				{
					httpResponse.addHeader("Content-Type", "text/plain");
					break;
				}
				case CoapMediaType.link_format:
				{
					httpResponse.addHeader("Content-Type", "application/link-format");
					break;
				}
				case CoapMediaType.json:
				{
					httpResponse.addHeader("Content-Type", "application/json");
					break;
				}
				case CoapMediaType.exi:
				{
					httpResponse.addHeader("Content-Type", "application/exi");
					break;
				}
				case CoapMediaType.octet_stream:
				{
					httpResponse.addHeader("Content-Type", "application/octet-stream");
					break;
				}
				case CoapMediaType.xml:
				{
					httpResponse.addHeader("Content-Type", "application/xml");
					break;
				}
				case CoapMediaType.UNKNOWN:
				{
					httpResponse.addHeader("Content-Type", "text/plain");
					break;
				}
				default:
					httpResponse.addHeader("Content-Type", "text/plain");
					break;
				}
					
				break;
			}
			case HeaderOptionNumber.Max_Age:
			{
				//max-age is later mapped to "expires" http header field
				max_age = new Integer(new String(option.getOptionValue()))*1000;	//milliseconds
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE)
					httpResponse.addHeader("Retry-After", new String(option.getOptionValue()));
				break;
			}
			case HeaderOptionNumber.Etag:
			{
				String etag = new String(option.getOptionValue());
				httpResponse.addHeader("Etag", etag);
				break;
			}
			case HeaderOptionNumber.Location_Path:
			{
				break;
			}
			case HeaderOptionNumber.Location_Query:
			{
				break;
			}
			case HeaderOptionNumber.Token:
			{
				break;
			}
			case HeaderOptionNumber.Uri_Query:
			{
				//TODO: add query(s) to http-uri
				break;
			}
			default:
				System.out.println("Error: Unknown Coap-Header!");
				break;
			}
		}
		
		//generate content-length-header
		if (httpResponse.getEntity() != null)
			httpResponse.addHeader("Content-length", "" + httpResponse.getEntity().getContentLength());
		
		//set creation-date for Caching:
		httpResponse.addHeader("Date", "" + formatDate(new GregorianCalendar().getTime()));
		
		//expires-option is option-value (default is 60 secs) + current_date
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(calendar.getTimeInMillis()+max_age);
		String date = formatDate(calendar.getTime());
		httpResponse.addHeader("Expires", date);

		return httpResponse;
	}
	
	//these functions are used to differentiate between method-codes
	//special methods need special handling
	public static CoapMessage handleHttpGETrequest(HttpRequest request) {
		
		//create CoapMessage
		/* FIXME: the request should be created by the corresponding channel*/
		CoapMessage coaprequest = new DefaultCoapMessage(CoapPacketType.CON, MessageCode.GET, 0);
		//Translate Headers
		coaprequest = headerTranslateHttpToCoap(request, coaprequest);
		
		return coaprequest;
	}
	
	
	public static CoapMessage handleHttpPUTrequest(HttpRequest request) {

		//create CoapMessage
		/* FIXME: the request should be created by the corresponding channel*/
		CoapMessage coaprequest = new DefaultCoapMessage(CoapPacketType.CON, MessageCode.PUT, 0);
		//Translate Headers
		coaprequest = headerTranslateHttpToCoap(request,coaprequest);
		
		//pass-through the payload
		BasicHttpEntityEnclosingRequest entirequest = (BasicHttpEntityEnclosingRequest) request;		
		ConsumingNHttpEntityTemplate entity = (ConsumingNHttpEntityTemplate) entirequest.getEntity();
		ByteContentListener liste = (ByteContentListener)entity.getContentListener();
		try {
			byte[] data = liste.getContent();
			coaprequest.setPayload(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
						
		return coaprequest;
	}	
	public static CoapMessage handleHttpPOSTrequest(HttpRequest request) {
		
		//create CoapMessage
		/* FIXME: the request should be created by the corresponding channel*/
		CoapMessage coaprequest = new DefaultCoapMessage(CoapPacketType.CON, MessageCode.POST, 0);
		//Translate Headers
		coaprequest = headerTranslateHttpToCoap(request,coaprequest);
		
		//pass-through the payload
		BasicHttpEntityEnclosingRequest entirequest = (BasicHttpEntityEnclosingRequest) request;		
		ConsumingNHttpEntityTemplate entity = (ConsumingNHttpEntityTemplate) entirequest.getEntity();	//(ConsumingNHttpEntityTemplate)
		ByteContentListener liste = (ByteContentListener)entity.getContentListener();
		try {
			byte[] data = liste.getContent();
			coaprequest.setPayload(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return coaprequest;
	}
	
	
	public static CoapMessage handleHttpDELETErequest(HttpRequest request) {
		
		//create CoapMessage
		/* FIXME: the request should be created by the corresponding channel*/
		CoapMessage coaprequest = new DefaultCoapMessage(CoapPacketType.CON, MessageCode.DELETE, 0);
		//Translate Headers
		coaprequest = headerTranslateHttpToCoap(request,coaprequest);
		return coaprequest;
	}
		
	//functions translate coap-response to http-response in case of http-coap
	public static HttpResponse handleCoapGETresponse(CoapMessage coapResponse, HttpResponse httpResponse) {		
						
		//set status code and reason phrase
		setHttpMsgCode(coapResponse, "get", httpResponse);
		
		//pass-through the payload, if we do not answer a head-request
		boolean isMethodHead = false; 
		if (isMethodHead) {
			//FIXME: don't add the payload in case of a HEAD HTTP request
		}
		else {
			NStringEntity entity;
			try {
				
				entity = new NStringEntity(new String(coapResponse.getPayload()),"UTF-8");
				entity.setContentType("text/plain");
				
				httpResponse.setEntity(entity);
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		//set Headers
		httpResponse = headerTranslateCoapToHttp(coapResponse,httpResponse);		
		return httpResponse;		
	}
	public static HttpResponse handleCoapPUTresponse(CoapMessage response, HttpResponse httpResponse) {		
		
		//set status code and reason phrase
		setHttpMsgCode(response, "put", httpResponse);
		
		//set Headers
		httpResponse = headerTranslateCoapToHttp(response, httpResponse);
					
		return httpResponse;		
	}
	public static HttpResponse handleCoapPOSTresponse(CoapMessage response, HttpResponse httpResponse) {		
		
		//set status code and reason phrase
		setHttpMsgCode(response, "post", httpResponse);
		
		//set Headers
		httpResponse = headerTranslateCoapToHttp(response, httpResponse);
					
		return  httpResponse;		
	}
	
	public static HttpResponse handleCoapDELETEresponse(CoapMessage response, HttpResponse httpResponse) {		
		//set status code and reason phrase
		setHttpMsgCode(response, "delete", httpResponse);
		//set Headers
		httpResponse = headerTranslateCoapToHttp(response, httpResponse);
		return httpResponse;		
	}
		
	//translation-functions in case of coap-http
	public static HttpRequest handleCoapGETrequest(CoapMessage request, String uri) {
		
		//create HttpRequest
		HttpRequest httprequest = new BasicHttpRequest("GET", uri);		
		
		//TODO:translate header options from coap-request to http-request
		
		return httprequest;
	}
	public static HttpRequest handleCoapPUTrequest(CoapMessage request, String uri) {
		
		//create HttpRequest
		BasicHttpEntityEnclosingRequest httprequest = new BasicHttpEntityEnclosingRequest("PUT", uri);	
		
		//TODO:translate header options from coap-request to http-request
		
		//Payload:
		NStringEntity entity;
		try {
			entity = new NStringEntity(""+request.getPayload());
			httprequest.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
						
		return httprequest;
	}
	public static HttpRequest handleCoapPOSTrequest(CoapMessage request, String uri) {
	
		//create HttpRequest
		BasicHttpEntityEnclosingRequest httprequest = new BasicHttpEntityEnclosingRequest("POST", uri);
		
		//TODO:translate header options from coap-request to http-request
		
		//Payload:
		NStringEntity entity;
		try {
			entity = new NStringEntity(""+request.getPayload());
			httprequest.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return httprequest;
	}
	public static HttpRequest handleCoapDELETErequest(CoapMessage request, String uri) {
	
		//create HttpRequest
		HttpRequest httprequest = new BasicHttpRequest("DELETE", uri);		
		
		//TODO:translate header options from coap-request to http-request
		
		return httprequest;
	}
	
//	//the mode is used to indicate for which case the proxy is listening to
//	//mode is unneccessary when proxy is listening to all cases, then there are more threads neccessary
//	public void setMode(Integer modenumber) {
//		mode = modenumber;
//	}
	
	//setter-functions to introduce other threads
	public void setHttpServer(HttpServerNIO server) {
		httpServerNIO = server;
	}
	public void setHttpClient(HttpClientNIO client) {
		httpClientNIO = client;
	}
	public void setCoapServer(CoapServerProxy server) {
		coapServer = server;
	}
	public void setCoapClient(CoapClientProxy client) {
		coapClient = client;
	}
	
	//these functions offer other classes the ability to pass a message to the mapper; queue-interface-functions
	public void putCoapResponse(ProxyMessageContext context) {		
		try {
			coapOutQueue.put(context);
			
            synchronized (coapResponseHandler) {
            	coapResponseHandler.notify();
            }
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void putCoapRequest(ProxyMessageContext context) {		
		try {
			coapInQueue.put(context);
			
            synchronized (coapRequestHandler) {
            	coapRequestHandler.notify();
            }
            
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void putHttpRequest(ProxyMessageContext context) {		
		try {
			httpInQueue.put(context);
			
            synchronized (httpRequestHandler) {
            	httpRequestHandler.notify();
            }
            
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void putHttpResponse(ProxyMessageContext context) {
		try {
			httpOutQueue.put(context);
			
            synchronized (httpResponseHandler) {
            	httpResponseHandler.notify();
            }
            
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//get the coap-message-code from a http-request
	public static MessageCode GetCoapMessageCode(HttpRequest request) {
		MessageCode code;
		String method;
		
		method = request.getRequestLine().getMethod().toLowerCase();
		
		//only get, put, post, head and delete are accepted
		if (method.contentEquals("get")) {
			code = MessageCode.GET;
			return code;
		}
		if (method.contentEquals("put")) {
			code = MessageCode.PUT;
			return code;
		}
		if (method.contentEquals("post")) {
			code = MessageCode.POST;
			return code;
		}
		if (method.contentEquals("delete")) {
			code = MessageCode.DELETE;
			return code;
		}
		//if we have a head request, coap should handle it as a get, but without any message-body
		if (method.contentEquals("head")) {
			code = MessageCode.GET;
			return code;
		}

		return null;
	}
	
	//exclude methods from processing:OPTIONS/TRACE/CONNECT
	public static boolean HttpRequestMethodSupported(HttpRequest request) {
		
		String method = request.getRequestLine().getMethod().toLowerCase();
		
		if (method.contentEquals("options") || method.contentEquals("trace") || method.contentEquals("connect"))		
			return false;

		return true;
	}
		
	//makes a date to a string; http-header-values (expires, date...) must be a string in most cases
    public static String formatDate(Date date) {
    	
    	final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    	
    	if (date == null) {
            throw new IllegalArgumentException("date is null");
        }

        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());				//CEST
        String ret = formatter.format(date);
        return ret;
        
    }
    
	public static boolean isIPv4Address(InetAddress addr) {
		try {
			@SuppressWarnings("unused") //just to check if casting fails
			Inet4Address addr4 = (Inet4Address) addr;
			return true;
		} catch (ClassCastException ex) {
			return false;
		}
	}

	public static boolean isIPv6Address(InetAddress addr) {
		try {
			@SuppressWarnings("unused") //just to check if casting fails
			Inet6Address addr6 = (Inet6Address) addr;
			return true;
		} catch (ClassCastException ex) {
			return false;
		}
	}
}
