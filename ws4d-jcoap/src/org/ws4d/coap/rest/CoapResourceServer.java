package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.Constants;
import org.ws4d.coap.connection.BasicCoapChannelManager;
import org.ws4d.coap.connection.BasicCoapSocketHandler;
import org.ws4d.coap.interfaces.CoapChannelManager;
import org.ws4d.coap.interfaces.CoapMessage;
import org.ws4d.coap.interfaces.CoapRequest;
import org.ws4d.coap.interfaces.CoapResponse;
import org.ws4d.coap.interfaces.CoapServer;
import org.ws4d.coap.interfaces.CoapServerChannel;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapRequestCode;
import org.ws4d.coap.messages.CoapResponseCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class CoapResourceServer implements CoapServer, ResourceServer {
    private  int port  = 0;
    private final static Logger logger = Logger.getLogger(CoapResourceServer.class); 
    
    protected HashMap<String, Resource> resources = new HashMap<String, Resource>();
    
    protected HashMap<String, byte[]> etags = new HashMap<String, byte[]>();
    
    private CoreResource coreResource = new CoreResource(this);

    public CoapResourceServer(){
    	logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    	logger.setLevel(Level.WARN);
    }
    
    public HashMap<String, Resource> getResources(){
    	return resources;
    }
    
    
    private void addResource(Resource resource){
    	resource.registerServerListener(this);
	    resources.put(resource.getPath(), resource);
	    coreResource.registerResource(resource);
    }
   
    @Override
    public boolean createResource(Resource resource) {
	if (resource==null) return false;
	if (!resources.containsKey(resource.getPath())) {
		addResource(resource);
		generateEtag(resource);
	    logger.info("created ressource: " + resource.getPath());
	    return true;
	} else
	    return false;
    }

    @Override
    public boolean updateResource(Resource resource, CoapRequest request ) {    	
    	
		if (resource==null)
			return false;
		
		if ( resources.containsKey(resource.getPath()) ) {
			( (BasicCoapResource) resource ).setValue( request.getPayload() );
			generateEtag(resource);
			logger.info("updated ressource: " + resource.getPath());
		    return true;
		} else
		    return false;
    }
    
    @Override
    public boolean deleteResource(String path) {
		if (null != resources.remove(path)) {
			etags.remove(path);
			logger.info("deleted ressource: " + path);
			return true;
		} else
			return false;
	}
    
    @Override
    public final Resource readResource(String path) {
    	logger.info("read ressource: " + path);
    	return resources.get(path);
    }
    


	/*corresponding to the coap spec the put is an update or create (or error)*/
//	public CoapResponseCode CoapResponseCode(Resource resource) {
//		Resource res = readResource(resource.getPath());
//		//TODO: check results
//		if (res == null){
//			createResource(resource);
//			return CoapResponseCode.Created_201;
//		} else if( res == coreResource ){
//			return CoapResponseCode.Forbidden_403;
//		} else {
//			updateResource(resource );
//			return CoapResponseCode.Changed_204;
//		}
//	}

    @Override
	public void start() throws Exception {
		start(Constants.COAP_DEFAULT_PORT);
	}
	
	public void start(int port) throws Exception {
		resources.put(coreResource.getPath(), coreResource);
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
	public void resourceChanged(Resource resource) {
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
		
		System.out.println("[REQUEST] " + "Type: " + requestCode + " | " + "Resource: " + targetPath);
		
		//TODO make this cast safe (send internal server error if it is not a CoapResource)
		CoapResource resource = (CoapResource) readResource(targetPath);
		
		/* TODO: check return values of create, read, update and delete --> no changes!
		 * TODO: implement forbidden --> maybe also done!*/

		switch (requestCode) {
		
		case GET:
			if (resource != null ) {
				eTagMatch = checkEtagMatch(request.getETag(), etags.get(targetPath) );
				if( eTagMatch != -1 )  {
					response = channel.createResponse( request, CoapResponseCode.Valid_203, CoapMediaType.text_plain );
					response.setETag( request.getETag().get(eTagMatch) );
				} else {
					// URI queries
					Vector<String> uriQueries = request.getUriQuery();
					final byte[] responseValue;
					if (uriQueries != null) {
						responseValue = resource.getValue(uriQueries);
					} else {
						responseValue = resource.getValue();
					}
					
					if( request.getBlock2() != null || channel.getMaxSendBlocksize() != null ) {
						response = channel.addBlockContext( request, responseValue );
					} else {
						response = channel.createResponse(request, CoapResponseCode.Content_205, resource.getCoapMediaType());
						response.setPayload(responseValue);
					}
					
					if (request.getObserveOption() != null){
						/*client wants to observe this resource*/
						if (resource.addObserver(request)){
							/* successfully added observer */
							response.setObserveOption(resource.getObserveSequenceNumber());
						}
					}
				}
				
			} else {
				response = channel.createResponse(request, CoapResponseCode.Not_Found_404);
			}
			break;
			
			
			
		case DELETE:
			/* CoAP: "A 2.02 (Deleted) response SHOULD be sent on
            success or in case the resource did not exist before the request.*/
			if( resource != coreResource ) {
				deleteResource(targetPath);
				response = channel.createResponse(request, CoapResponseCode.Deleted_202);
			} else {
				response = channel.createResponse(request, CoapResponseCode.Forbidden_403 );
			}
			break;
			
			
		case POST:
			if( resource != coreResource ) {
				if (resource != null){
					resource.post(request.getPayload());
					response = channel.createResponse(request, CoapResponseCode.Changed_204);
				} else {
					/* if the resource does not exist, a new resource will be created */
					createResource(parseRequest(request));
					response = channel.createResponse(request, CoapResponseCode.Created_201);
				}
			} else {
				response = channel.createResponse(request, CoapResponseCode.Forbidden_403);
			}
			
			break;
			
			
		case PUT:
			if( resource != coreResource ) {
				if (resource == null){
					/* create*/
					if( request.getIfMatchOption() == null ) {
						createResource(parseRequest(request));
						response = channel.createResponse(request,CoapResponseCode.Created_201);
					} else {
						response = channel.createResponse(request, CoapResponseCode.Precondition_Failed_412 );
					}
				} else {
					/*update*/
					if( request.getIfMatchOption() == null || checkEtagMatch( request.getIfMatchOption(), etags.get(targetPath) ) != -1 ) {
						updateResource( resource, request);
						response = channel.createResponse(request, CoapResponseCode.Changed_204);
					} else if( request.getIfNoneMatchOption() || checkEtagMatch( request.getIfMatchOption(), etags.get(targetPath) ) == -1 ) {
						response = channel.createResponse( request, CoapResponseCode.Precondition_Failed_412 );
					}
				}
			} else {
				response = channel.createResponse( request, CoapResponseCode.Forbidden_403 );
			}
			break;

		default:
			response = channel.createResponse(request,
					CoapResponseCode.Bad_Request_400);
			break;
		}
		channel.sendMessage(response);
	}

	private CoapResource parseRequest(CoapRequest request) {
		CoapResource resource = new BasicCoapResource(request.getUriPath(),
				request.getPayload(), request.getContentType());
		// TODO add content type
		return resource;
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
		logger.error("Separate response failed but server never used separate responses");
		
	}
	
	@Override
	public void onReset( CoapRequest lastRequest ) {
		CoapResource resource = (CoapResource) readResource(lastRequest.getUriPath());
		if( resource != null ) {
			resource.removeObserver( lastRequest.getChannel() );
		}
		System.out.println("[INFO] Reset Message Received!");
	}

	protected String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}
	
	protected void generateEtag( Resource resource ) {
		etags.put( resource.getPath(), ("" + resource.hashCode()).getBytes() );
	}
	
	protected int checkEtagMatch( Vector<byte[]> reqEtags, byte[] resEtag ) {
		if( reqEtags != null ) {
			int index = 0;
			for(; index < reqEtags.size(); index++ ){
				if( reqEtags.get(index).equals( resEtag ) )
					return index;
			}
		}
		return -1;
	}

}
