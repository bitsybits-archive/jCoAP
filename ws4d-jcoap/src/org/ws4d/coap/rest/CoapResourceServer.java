package org.ws4d.coap.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.connection.BasicCoapSocketHandler;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

public class CoapResourceServer extends AbstractResourceServer implements CoapServer {
    private  int port  = 0;
    private final static Logger logger = Logger.getLogger(CoapResourceServer.class); 

	@Override
	public void start() throws Exception {
		start(Constants.COAP_DEFAULT_PORT);
	}
	
	public void start(int port) throws Exception {
		super.start();
		CoapChannelManager channelManager = BasicCoapChannelManager
				.getInstance();
		this.port = port;
		channelManager.createServerListener(this, port);
	}

    @Override
    public void stop() {
    }

    public int getPort() {
    	return port;
    }

    @Override
    public URI getHostUri() {
	URI hostUri = null;
	try {
	    hostUri = new URI("coap://" + this.getLocalIpAddress() + ":"
		    + getPort());
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	}
	return hostUri;
    }

    @Override
    public CoapServer onAccept(CoapRequest request) {
    	return this;
    }
    
    @Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		CoapRequestCode requestCode = request.getRequestCode();
		String targetPath = request.getUriPath();

		if (requestCode == CoapRequestCode.GET) {
			//TODO make this cast safe (send internal server error if it is not a CoapResource)
			final CoapResource resource = (CoapResource) readResource(targetPath);

			if (resource != null) {
				// URI queries
				Vector<String> uriQueries = request.getUriQuery();
				final byte[] responseValue;
				if (uriQueries != null) {
					responseValue = resource.getValue(uriQueries);
				} else {
					responseValue = resource.getValue();
				}
				response = channel.createResponse(request,
						CoapResponseCode.Content_205, resource.getCoapMediaType());

				response.setPayload(responseValue);
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Not_Found_404);
			}
		} else if (requestCode == CoapRequestCode.DELETE) {
			if (delete(targetPath)){
				response = channel.createResponse(request,
						CoapResponseCode.Deleted_202);
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Forbidden_403);
			}
		} else if (requestCode == CoapRequestCode.POST) {
			//TODO make this cast safe (send internal server error if it is not a CoapResource)
			final CoapResource resource = (CoapResource) readResource(targetPath);
			resource.post(request.getPayload());
			//TODO: How to pass through the Request and the Response
	
		} else if (requestCode == CoapRequestCode.PUT) {
			CoapResource newResource = parseRequest(request);
			if (put(newResource)) {
				//TODO: distinguish between created and changed
				response = channel.createResponse(request,
						CoapResponseCode.Created_201);
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Forbidden_403);
			}
		} else {
			response = channel.createResponse(request,
					CoapResponseCode.Bad_Request_400);
			return;
		}
		channel.sendMessage(response);
	}

    private CoapResource parseRequest(CoapRequest request) {
    	CoapResource resource = new BasicCoapResource(request.getUriPath(), request.getPayload(), request.getContentType());
    	// TODO add content type
	return resource;
    }

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		logger.error("Separate response failed but server never used separate responses");
		
	}

}
