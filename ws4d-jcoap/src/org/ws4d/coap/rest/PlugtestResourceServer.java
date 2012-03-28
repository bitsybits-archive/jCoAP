package org.ws4d.coap.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

public class PlugtestResourceServer extends AbstractPlugtestResourceServer implements CoapServer {
    private static final int PORT = Constants.COAP_DEFAULT_PORT;

    @Override
    public void start() throws Exception {
	super.start();
	CoapChannelManager channelManager = BasicCoapChannelManager
		.getInstance();
	channelManager.createServerListener(this, PORT);
    }

    @Override
    public void stop() {
    }

    public int getPort() {
	return PORT;
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
	public void handleRequest(CoapServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		CoapRequestCode requestCode = request.getRequestCode();
		if (requestCode == CoapRequestCode.GET) {
			// create response with value from responsible Resource object
			String targetPath = request.getUriPath();
			//TODO make this cast safe
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
			String targetPath = request.getUriPath();
			deleteResource(targetPath);
			response = channel.createResponse(request,
					CoapResponseCode.Deleted_202);

		} else if (requestCode == CoapRequestCode.POST) {
			CoapResource resource = parseRequest(request);
			if (createResource(resource)) {
				response = channel.createResponse(request,
						CoapResponseCode.Created_201);
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Bad_Request_400);
			}

		} else if (requestCode == CoapRequestCode.PUT) {
			CoapResource resource = parseRequest(request);
			if (updateResource(resource)) {
				response = channel.createResponse(request,
						CoapResponseCode.Changed_204);
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Bad_Request_400);
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
	public void separateResponseFailed(CoapServerChannel channel) {
		// TODO Auto-generated method stub
		
	}

}
