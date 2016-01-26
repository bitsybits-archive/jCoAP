/* Copyright 2015 University of Rostock
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
 *****************************************************************************/

package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Björn Konieczek <bjoern.konieczek@uni-rostock.de>
 */
public class CoapResourceServer implements CoapServer, ResourceServer {
	private final static Logger logger = Logger.getLogger(CoapResourceServer.class);
	private int port = 0;
	private Map<String, byte[]> etags = new HashMap<String, byte[]>();
	private Map<String, CoapResource> resources = new HashMap<String, CoapResource>();
	private CoreResource coreResource = new CoreResource(this);
	
	private boolean allowCreate = true;
	private boolean allowDelete = true;	

	public CoapResourceServer() {
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		logger.setLevel(Level.WARN);
	}

	public Map<String, CoapResource> getResources() {
		return this.resources;
	}

	private void addResource(CoapResource resource) {
		resource.registerServerListener(this);
		this.resources.put(resource.getPath(), resource);
		this.coreResource.changed();
	}

	@Override
	public boolean createResource(CoapResource resource) {
		if (null != resource && !this.resources.containsKey(resource.getPath())) {
			addResource(resource);
			generateEtag(resource);
			logger.info("created ressource: " + resource.getPath());
			this.coreResource.changed();
			return true;
		}
		return false;
	}

	@Override
	public boolean updateResource(CoapResource resource, CoapRequest request) {
		if (null != resource && this.resources.containsKey(resource.getPath())) {
			((BasicCoapResource) resource).setValue(request.getPayload());
			generateEtag(resource);
			logger.info("updated ressource: " + resource.getPath());
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteResource(String path) {
		if (null != this.resources.remove(path)) {
			this.etags.remove(path);
			logger.info("deleted ressource: " + path);
			this.coreResource.changed();
			return true;
		}
		return false;
	}

	@Override
	public final CoapResource readResource(String path) {
		logger.info("read ressource: " + path);
		return this.resources.get(path);
	}

	/*
	 * corresponding to the coap spec the put is an update or create (or error)
	 */
	// public CoapResponseCode CoapResponseCode(Resource resource) {
	// Resource res = readResource(resource.getPath());
	// //TODO: check results
	// if (res == null){
	// createResource(resource);
	// return CoapResponseCode.Created_201;
	// } else if( res == coreResource ){
	// return CoapResponseCode.Forbidden_403;
	// } else {
	// updateResource(resource );
	// return CoapResponseCode.Changed_204;
	// }
	// }

	@Override
	public void start() throws Exception {
		start(Constants.COAP_DEFAULT_PORT);
	}

	public void start(int port) throws Exception {
		this.resources.put(this.coreResource.getPath(), this.coreResource);
		CoapChannelManager channelManager = BasicCoapChannelManager.getInstance();
		this.port = port;
		channelManager.createServerListener(this, this.port);
	}

	@Override
	public void stop() {
		// TODO implement stop method of the CoAP resource server
	}

	public int getPort() {
		return this.port;
	}

	@Override
	public URI getHostUri() {
		URI hostUri = null;
		try {
			hostUri = new URI("coap://" + getLocalIpAddress() + ":" + getPort());
		} catch (URISyntaxException e) {
			logger.warn("getHostUri() could not create valid URI from local IP address", e);
			e.printStackTrace();
		}
		return hostUri;
	}

	@Override
	public void resourceChanged(CoapResource resource) {
		logger.info("Resource changed: " + resource.getPath());
	}

	@Override
	public CoapServer onAccept(CoapRequest request) {
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		CoapResponse response = null;
		CoapRequestCode requestCode = request.getRequestCode();
		String targetPath = request.getUriPath();
		int eTagMatch = -1;

		CoapResource resource = readResource(targetPath);

		 // TODO: check return values of create, read, update and delete --> no changes! 
		 // TODO: implement forbidden --> maybe also done!

		switch (requestCode) {
		case GET:
			if (resource != null) {
				eTagMatch = checkEtagMatch(request.getETag(), this.etags.get(targetPath));
				if (eTagMatch != -1) {
					response = channel.createResponse(request, CoapResponseCode.Valid_203, CoapMediaType.text_plain);
					response.setETag(request.getETag().get(eTagMatch));
				} else if (!resource.isReadable()){
					response = channel.createResponse(request, CoapResponseCode.Forbidden_403);
				} else {
					// URI queries
					Vector<String> uriQueries = request.getUriQuery();
					final byte[] responseValue;
					if (uriQueries != null) {
						responseValue = resource.getValue(uriQueries);
					} else {
						responseValue = resource.getValue();
					}

					if (request.getBlock2() != null || channel.getMaxSendBlocksize() != null) {
						response = channel.addBlockContext(request, responseValue);
					} else {
						response = channel.createResponse(request, CoapResponseCode.Content_205, resource.getCoapMediaType());
						response.setPayload(responseValue);
					}
					if (null != request.getObserveOption() && resource.addObserver(request)) {
						response.setObserveOption(resource.getObserveSequenceNumber());
					}
				}
			} else {
				response = channel.createResponse(request, CoapResponseCode.Not_Found_404);
			}
			break;
		case DELETE:
			if (null != resource && !resource.isDeletable()) {
				response = channel.createResponse(request, CoapResponseCode.Forbidden_403);
			} else {
				deleteResource(targetPath);
				response = channel.createResponse(request, CoapResponseCode.Deleted_202);
			}
			break;
		case POST:
			if (resource != this.coreResource) {
				if (resource != null) {
					resource.post(request.getPayload());
					response = channel.createResponse(request, CoapResponseCode.Changed_204);
				} else {
					// if the resource does not exist -> create
					createResource(parseRequest(request));
					response = channel.createResponse(request, CoapResponseCode.Created_201);
				}
			} else {
				response = channel.createResponse(request, CoapResponseCode.Forbidden_403);
			}
			break;
		case PUT:
			if (resource != this.coreResource) {
				if (resource == null) {
					/* create */
					if (request.getIfMatchOption() == null) {
						createResource(parseRequest(request));
						response = channel.createResponse(request, CoapResponseCode.Created_201);
					} else {
						response = channel.createResponse(request, CoapResponseCode.Precondition_Failed_412);
					}
				} else {
					/* update */
					if (request.getIfMatchOption() == null
							|| checkEtagMatch(request.getIfMatchOption(), this.etags.get(targetPath)) != -1) {
						updateResource(resource, request);
						response = channel.createResponse(request, CoapResponseCode.Changed_204);
					} else if (request.getIfNoneMatchOption()
							|| checkEtagMatch(request.getIfMatchOption(), this.etags.get(targetPath)) == -1) {
						response = channel.createResponse(request, CoapResponseCode.Precondition_Failed_412);
					}
				}
			} else {
				response = channel.createResponse(request, CoapResponseCode.Forbidden_403);
			}
			break;
		default:
			response = channel.createResponse(request, CoapResponseCode.Bad_Request_400);
			break;
		}
		channel.sendMessage(response);
	}

	private static CoapResource parseRequest(CoapRequest request) {
		CoapResource resource = new BasicCoapResource(request.getUriPath(), request.getPayload(),request.getContentType());
		return resource;
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		logger.error("Separate response failed but server never used separate responses");
	}

	@Override
	public void onReset(CoapRequest lastRequest) {
		CoapResource resource = readResource(lastRequest.getUriPath());
		if (resource != null) {
			resource.removeObserver(lastRequest.getChannel());
		}
		logger.info("Reset Message Received!");
	}

	private static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			logger.error("Can't obtain network Interface", ex);
		}
		return null;
	}

	private void generateEtag(CoapResource resource) {
		this.etags.put(resource.getPath(), ("" + resource.hashCode()).getBytes());
	}

	private static int checkEtagMatch(List<byte[]> reqEtags, byte[] resEtag) {
		if (reqEtags != null) {
			for (int index = 0 ; index < reqEtags.size(); index++) {
				if (reqEtags.get(index).equals(resEtag))
					return index;
			}
		}
		return -1;
	}
}